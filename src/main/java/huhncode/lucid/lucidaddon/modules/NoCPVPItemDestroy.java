package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.BlockPos;

public class NoCPVPItemDestroy extends Module {
    private long blockEndTime = 0;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();



    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("The delay in seconds after a player dies before allowing crystal or anchor use.")
        .defaultValue(3)
        .min(1)
        .max(10)
        .sliderMax(10)
        .build());

    public NoCPVPItemDestroy() {
        super(LucidAddon.CATEGORY, "No-CPVP-item-destroy", "Blocks crystal and anchor use for a short time after a player dies nearby, to prevent item destruction.");
    }

    @EventHandler
    private void onEntityDeath(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet && packet.getStatus() == 3) {
            Entity entity = packet.getEntity(mc.world);
            if (entity instanceof PlayerEntity && entity != mc.player) {
                if (mc.player.distanceTo(entity) <= 20) {
                    blockEndTime = System.currentTimeMillis() + (delay.get() * 1000);
                }
            }
        }
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (System.currentTimeMillis() < blockEndTime) {
            Entity entity = event.entity;
            if (entity.getType() == EntityType.END_CRYSTAL) {
                event.cancel();
                warning("Crystal break blocked (cooldown active)");
            }
        }
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (System.currentTimeMillis() < blockEndTime) {
            BlockPos pos = event.result.getBlockPos();
            if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR) {
                event.cancel();
                warning("Anchor use blocked (cooldown active)");
            }
        }
    }
}
