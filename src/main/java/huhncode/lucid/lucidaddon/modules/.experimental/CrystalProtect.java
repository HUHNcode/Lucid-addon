package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

public class CrystalProtect extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgConditions = settings.createGroup("Conditions");

    // Settings
    private final Setting<Double> blockDuration = sgGeneral.add(new DoubleSetting.Builder()
        .name("block-duration")
        .description("How long to block crystal breaking after a kill (in seconds).")
        .defaultValue(1.0)
        .min(0.1)
        .max(10.0)
        .build()
    );

    private final Setting<Boolean> onlyInEnd = sgConditions.add(new BoolSetting.Builder()
        .name("only-in-end")
        .description("Only activate in the End dimension.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> onlyPvP = sgConditions.add(new BoolSetting.Builder()
        .name("only-pvp")
        .description("Only activate for player kills.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showIndicator = sgGeneral.add(new BoolSetting.Builder()
        .name("show-indicator")
        .description("Show visual feedback when crystal breaking is blocked.")
        .defaultValue(true)
        .build()
    );

    // State
    private long blockUntil = 0;
    private boolean wasBlocking = false;

    public CrystalProtect() {
        super(LucidAddon.CATEGORY, "Crystal Protect", "Blocks crystal breaking after killing a player to protect their items.");
    }

    @Override
    public void onActivate() {
        blockUntil = 0;
        wasBlocking = false;
    }

    @Override
    public void onDeactivate() {
        blockUntil = 0;
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        blockUntil = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        // Check if we should still be blocking
        boolean shouldBlock = System.currentTimeMillis() < blockUntil;
        
        if (wasBlocking && !shouldBlock) {
            info("Crystal breaking protection has ended.");
        }
        
        wasBlocking = shouldBlock;
    }

    public void onPlayerKill(PlayerEntity killedPlayer) {
        if (!isActive()) return;
        
        // Check conditions
        if (onlyInEnd.get() && !mc.world.getRegistryKey().getValue().getPath().equals("the_end")) return;
        if (onlyPvP.get() && !(killedPlayer instanceof PlayerEntity)) return;
        
        // Calculate duration in milliseconds
        long durationMs = (long)(blockDuration.get() * 1000);
        blockUntil = System.currentTimeMillis() + durationMs;
        
        if (showIndicator.get()) {
            info(String.format("Crystal breaking blocked for %.1f seconds after killing %s.", blockDuration.get(), killedPlayer.getEntityName()));
        }
    }

    public boolean shouldBlockCrystalBreaking() {
        if (!isActive()) return false;
        
        // Check if we're currently blocking
        if (System.currentTimeMillis() < blockUntil) {
            if (showIndicator.get() && !wasBlocking) {
                info("Blocking crystal breaking to protect items...");
            }
            return true;
        }
        
        return false;
    }
}