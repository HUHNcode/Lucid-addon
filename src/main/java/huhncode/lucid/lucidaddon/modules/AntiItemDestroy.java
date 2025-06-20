package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.BlockPos;

public class AntiItemDestroy extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Zeitpunkt, bis zu dem das Brechen von Kristallen blockiert ist
    private long blockCrystalBreakingUntil = 0;
    private BlockPos deathLocation = null; // Speichert den Todesort des Spielers

    public AntiItemDestroy() {
        super(LucidAddon.CATEGORY, "AntiItemDestroy", "Blocks crystal and anchor interactions for a short time after a player's death.\n" +
                "WARNING: This module could lead to a massive disadvantage when fighting with more than one player");
    }

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Duration in milliseconds for which crystal breaking is blocked after a player's death.")
            .defaultValue(3000) // Standardmäßig 5 Sekunden
            .min(0)
            .sliderMax(10000) // Slider bis 10 Sekunden
            .build());

    private final Setting<Integer> deathRadius = sgGeneral.add(new IntSetting.Builder()
        .name("death-radius")
        .description("The radius in blocks around the crystal/anchor within which a player must die to trigger the block.")
        .defaultValue(13)
        .min(0) // 0 deaktiviert die Radiusprüfung
        .sliderMax(50)
        .build()
    );


    @EventHandler
    private void onEntityDeath(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 3 && mc.world != null && mc.player != null) { // Status 3 = Entität stirbt
                Entity entity = packet.getEntity(mc.world);
                if (entity instanceof PlayerEntity && entity != mc.player) {
                    // Speichere den Todesort und aktiviere die Blockade
                    // Die eigentliche Radiusprüfung findet beim Interaktionsversuch statt.
                    deathLocation = entity.getBlockPos();
                    blockCrystalBreakingUntil = System.currentTimeMillis() + delay.get();
                    ChatUtils.info(String.format("Player %s died at %s. Interactions might be blocked for %d ms within %d blocks.",
                        entity.getName().getString(), deathLocation.toShortString(), delay.get(), deathRadius.get()));
                }
            }
        }
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (event.entity instanceof EndCrystalEntity) {
            if (shouldBlockInteraction(event.entity.getBlockPos())) {
                ChatUtils.info("Crystal breaking is currently blocked due to nearby player death.");
                event.cancel();
            }
        }
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (mc.world != null && event.result != null && mc.world.getBlockState(event.result.getBlockPos()).getBlock() == Blocks.RESPAWN_ANCHOR) {
            if (shouldBlockInteraction(event.result.getBlockPos())) {
                ChatUtils.info("Interaction with Respawn Anchor is currently blocked due to nearby player death.");
                event.cancel();
            }
        }
    }

    private boolean shouldBlockInteraction(BlockPos interactionPos) {
        if (System.currentTimeMillis() >= blockCrystalBreakingUntil) return false; // Zeit abgelaufen
        if (deathLocation == null) return false; // Kein kürzlicher Tod registriert

        int radius = deathRadius.get();
        if (radius <= 0) return true; // Radiusprüfung deaktiviert, blockiere, wenn Zeit noch nicht abgelaufen

        // Prüfe, ob die Interaktionsposition innerhalb des Todesradius liegt
        double distanceSq = deathLocation.getSquaredDistance(interactionPos);
        boolean shouldBlock = distanceSq <= radius * radius;
        if (!shouldBlock) deathLocation = null; // Setze Todesort zurück, wenn außerhalb des Radius für diesen Interaktionsversuch
        return shouldBlock;
    }
}
