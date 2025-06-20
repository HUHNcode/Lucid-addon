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
        // Prüfen, ob sich der Spieler bewegt hat
        if (!currentPos.equals(lastPos)) {
            lastPos = currentPos;
            lastMoveTime = System.currentTimeMillis();
        }
        // AFK, wenn die Zeit seit der letzten Bewegung überschritten wurde
        return System.currentTimeMillis() - lastMoveTime > afkTimeMs;
    }
    
    // Setzt den AFK-Zustand zurück (wird beim Join verwendet)
    public static void reset() {
        if (mc.player != null) {
            lastPos = mc.player.getPos();
            lastMoveTime = System.currentTimeMillis();
        }
    }
}
