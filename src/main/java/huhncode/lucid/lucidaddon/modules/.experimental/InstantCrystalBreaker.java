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
    
    // Einstellungen für das Delay
    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay in Millisekunden für den Angriff.")
            .defaultValue(50)
            .min(0)
            .max(500)
            .build());
    
    private final List<QueuedAttack> attackQueue = new LinkedList<>();

    public InstantCrystalBreaker() {
        super(LucidAddon.CATEGORY, "End Crystal Delay", "Delay attacks on End Crystals.");
    }

    // Fängt die Pakete ab, die an den Server gesendet werden
    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerInteractEntityC2SPacket packet) {
            // Wenn der angegriffene Gegenstand ein Ender Crystal ist
            if (isEndCrystal(packet.getEntityId())) {
                event.cancel(); // Paket blockieren

                // Füge den Angriff zur Warteschlange hinzu
                attackQueue.add(new QueuedAttack(packet, System.currentTimeMillis()));
                removeEndCrystal(packet.getEntityId());
                spawnFakeEndCrystal(packet.getEntityId());
            }
        }
    }

    // Entferne den echten Ender Crystal clientseitig
    private void removeEndCrystal(int entityId) {
        if (mc.world != null && mc.world.getEntityById(entityId) instanceof EndCrystalEntity crystal) {
            crystal.remove(Entity.RemovalReason.KILLED);
        }
    }

    // Erzeuge einen Fake-Ender Crystal
    private void spawnFakeEndCrystal(int entityId) {
        EndCrystalEntity fakeCrystal = new EndCrystalEntity(mc.world);
        fakeCrystal.setPosition(new Vec3f(mc.world.getEntityById(entityId).getX(), mc.world.getEntityById(entityId).getY(), mc.world.getEntityById(entityId).getZ()));
        fakeCrystal.setCustomName(new LiteralText("FAKE_CRYSTAL"));
        mc.world.addEntity(entityId + 1000, fakeCrystal); // Sicherstellen, dass ID-Kollision verhindert wird
    }

    // Bearbeiten Sie die Warteschlange im Tick-Event
    @EventHandler
    private void onTick(TickEvent.Post event) {
        long currentTime = System.currentTimeMillis();
        attackQueue.removeIf(queuedAttack -> {
            if (currentTime - queuedAttack.timestamp >= delaySetting.get()) {
                // Senden des Attack-Pakets an den Server
                mc.getNetworkHandler().sendPacket(queuedAttack.packet);
                return true; // Entfernen aus der Queue
            }
            return false; // Behalte in der Warteschlange
        });
    }

    // Überprüfen, ob die angegriffene Entity ein Ender Crystal ist
    private boolean isEndCrystal(int entityId) {
        return mc.world.getEntityById(entityId) instanceof EndCrystalEntity;
    }

    // Klasse für wartende Angriffe
    private static class QueuedAttack {
        final PlayerInteractEntityC2SPacket packet; 
        final long timestamp;

        QueuedAttack(PlayerInteractEntityC2SPacket packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }
}