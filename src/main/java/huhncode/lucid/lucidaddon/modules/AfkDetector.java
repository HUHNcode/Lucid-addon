package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class AfkDetector extends Module {
    private static final int AFK_TIME_MS = 5000; // 5 Sekunden
    private long lastActiveTime;
    private boolean isAfk = false;
    private Vec3d lastPosition;

    public AfkDetector() {
        super(LucidAddon.CATEGORY, "afk-detection", "Detects when you go AFK and sends debug messages.");
    }

    @Override
    public void onActivate() {
        lastActiveTime = System.currentTimeMillis();
        lastPosition = getPlayerPos();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Prüft Bewegung
        Vec3d currentPosition = getPlayerPos();
        if (!currentPosition.equals(lastPosition)) {
            lastActiveTime = System.currentTimeMillis();
            lastPosition = currentPosition;
            setAfkState(false);
        }

        // Prüft AFK-Status
        if (System.currentTimeMillis() - lastActiveTime > AFK_TIME_MS) {
            setAfkState(true);
        }
    }

    private void setAfkState(boolean afk) {
        if (this.isAfk != afk) {
            this.isAfk = afk;
            info(afk ? "Player is now AFK." : "Player is no longer AFK.");
        }
    }

    private Vec3d getPlayerPos() {
        return mc.player.getPos();
    }
}
