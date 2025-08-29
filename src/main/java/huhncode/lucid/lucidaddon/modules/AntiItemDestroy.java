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

    // Time until which crystal breaking is blocked
    private long blockCrystalBreakingUntil = 0;
    private BlockPos deathLocation = null; // Stores the player's death location

    public AntiItemDestroy() {
        super(LucidAddon.CATEGORY, "AntiItemDestroy", "Blocks crystal and anchor interactions for a short time after a player's death.\n" +
                "WARNING: This module could lead to a massive disadvantage when fighting with more than one player");
    }

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Duration in milliseconds for which crystal breaking is blocked after a player's death.")
            .defaultValue(3000) // Default 5 seconds
            .min(0)
            .sliderMax(10000) // Slider up to 10 seconds
            .build());

    private final Setting<Integer> deathRadius = sgGeneral.add(new IntSetting.Builder()
        .name("death-radius")
        .description("The radius in blocks around the crystal/anchor within which a player must die to trigger the block.")
        .defaultValue(13)
        .min(0) // 0 disables the radius check
        .sliderMax(50)
        .build()
    );


    @EventHandler
    private void onEntityDeath(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 3 && mc.world != null && mc.player != null) { // Status 3 = entity dies
                Entity entity = packet.getEntity(mc.world);
                if (entity instanceof PlayerEntity && entity != mc.player) {
                    // Save the death location and activate the block
                    // The actual radius check happens during the interaction attempt.
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
        if (System.currentTimeMillis() >= blockCrystalBreakingUntil) return false; // Time expired
        if (deathLocation == null) return false; // No recent death registered

        int radius = deathRadius.get();
        if (radius <= 0) return true; // Radius check disabled, block if time has not yet expired

        // Check if the interaction position is within the death radius
        double distanceSq = deathLocation.getSquaredDistance(interactionPos);
        boolean shouldBlock = distanceSq <= radius * radius;
        if (!shouldBlock) deathLocation = null; // Reset death location if outside the radius for this interaction attempt
        return shouldBlock;
    }
}
