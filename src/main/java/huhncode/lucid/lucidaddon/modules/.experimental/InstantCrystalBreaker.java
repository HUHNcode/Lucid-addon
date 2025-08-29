package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.event.events.PacketEvent;
import meteordevelopment.meteorclient.event.events.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;

import java.util.LinkedList;
import java.util.List;

public class InstantCrystalBreaker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    // Settings for the delay
    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay in milliseconds for the attack.")
            .defaultValue(50)
            .min(0)
            .max(500)
            .build());
    
    private final List<QueuedAttack> attackQueue = new LinkedList<>();

    public InstantCrystalBreaker() {
        super(LucidAddon.CATEGORY, "End Crystal Delay", "Delay attacks on End Crystals.");
    }

    // Intercepts packets sent to the server
    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerInteractEntityC2SPacket packet) {
            // If the attacked item is an End Crystal
            if (isEndCrystal(packet.())) {
                event.cancel(); // Block packet

                // Add the attack to the queue
                attackQueue.add(new QueuedAttack(packet, System.currentTimeMillis()));
                removeEndCrystal(packet.getEntityId());
                spawnFakeEndCrystal(packet.getEntityId());
            }
        }
    }

    // Remove the real End Crystal client-side
    private void removeEndCrystal(int entityId) {
        if (mc.world != null && mc.world.getEntityById(entityId) instanceof EndCrystalEntity crystal) {
            crystal.remove(Entity.RemovalReason.KILLED);
        }
    }

    // Create a fake End Crystal
    private void spawnFakeEndCrystal(int entityId) {
        EndCrystalEntity fakeCrystal = new EndCrystalEntity(mc.world);
        fakeCrystal.setPosition(new Vec3f(mc.world.getEntityById(entityId).getX(), mc.world.getEntityById(entityId).getY(), mc.world.getEntityById(entityId).getZ()));
        fakeCrystal.setCustomName(new LiteralText("FAKE_CRYSTAL"));
        mc.world.addEntity(entityId + 1000, fakeCrystal); // Ensure ID collision is prevented
    }

    // Process the queue in the tick event
    @EventHandler
    private void onTick(TickEvent.Post event) {
        long currentTime = System.currentTimeMillis();
        attackQueue.removeIf(queuedAttack -> {
            if (currentTime - queuedAttack.timestamp >= delaySetting.get()) {
                // Send the attack packet to the server
                mc.getNetworkHandler().sendPacket(queuedAttack.packet);
                return true; // Remove from the queue
            }
            return false; // Keep in the queue
        });
    }

    // Check if the attacked entity is an End Crystal
    private boolean isEndCrystal(int entityId) {
        return mc.world.getEntityById(entityId) instanceof EndCrystalEntity;
    }

    // Class for queued attacks
    private static class QueuedAttack {
        final PlayerInteractEntityC2SPacket packet; 
        final long timestamp;

        QueuedAttack(PlayerInteractEntityC2SPacket packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }
}