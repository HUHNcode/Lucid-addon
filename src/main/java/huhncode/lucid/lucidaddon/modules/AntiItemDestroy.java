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

public class AntiItemDestroy extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Zeitpunkt, bis zu dem das Brechen von Kristallen blockiert ist
    private long blockCrystalBreakingUntil = 0;

    public AntiItemDestroy() {
        super(LucidAddon.CATEGORY, "AntiItemDestroy", "Blocks crystal andancor interactions for a short time after a player's death.");
    }

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Duration in milliseconds for which crystal breaking is blocked after a player's death.")
            .defaultValue(3000) // Standardmäßig 5 Sekunden
            .min(0)
            .sliderMax(10000) // Slider bis 10 Sekunden
            .build());


    @EventHandler
    private void onEntityDeath(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 3 && mc.world != null && mc.player != null) { // Status 3 = Entität stirbt
                Entity entity = packet.getEntity(mc.world);
                if (entity instanceof PlayerEntity && entity != mc.player) {
                    // Ein anderer Spieler ist gestorben
                    ChatUtils.info(String.format("Player %s died. Crystals and Anchors will be blocked for %d ms.", entity.getName().getString(), delay.get()));
                    blockCrystalBreakingUntil = System.currentTimeMillis() + delay.get();
                }
            }
        }
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (event.entity instanceof EndCrystalEntity) {
            if (System.currentTimeMillis() < blockCrystalBreakingUntil) {
                ChatUtils.info("Crystal breaking is currently blocked.");
                event.cancel();
            }
        }
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        // Überprüfe, ob der interagierte Block ein Respawn Anchor ist
        if (mc.world != null && event.result != null && mc.world.getBlockState(event.result.getBlockPos()).getBlock() == Blocks.RESPAWN_ANCHOR) {
            if (System.currentTimeMillis() < blockCrystalBreakingUntil) {
                ChatUtils.info("Interaction with Respawn Anchor is currently blocked.");
                event.cancel();
            }
        }
    }

}
