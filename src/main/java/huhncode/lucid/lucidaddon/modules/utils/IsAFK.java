package huhncode.lucid.lucidaddon.modules.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class IsAFK {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static Vec3d lastPos = Vec3d.ZERO;
    private static long lastMoveTime = System.currentTimeMillis();

    public static boolean isAFK(int afkTimeMs) {
        if (mc.player == null) return false;

        Vec3d currentPos = mc.player.getPos();
        // Check if the player has moved
        if (!currentPos.equals(lastPos)) {
            lastPos = currentPos;
            lastMoveTime = System.currentTimeMillis();
        }
        // AFK if the time since the last movement has been exceeded
        return System.currentTimeMillis() - lastMoveTime > afkTimeMs;
    }
    
    // Resets the AFK state (used on join)
    public static void reset() {
        if (mc.player != null) {
            lastPos = mc.player.getPos();
            lastMoveTime = System.currentTimeMillis();
        }
    }
}
