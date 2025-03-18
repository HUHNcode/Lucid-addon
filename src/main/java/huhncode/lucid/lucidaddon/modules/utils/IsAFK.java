package huhncode.lucid.lucidaddon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class IsAFK {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static Vec3d lastPos = Vec3d.ZERO;
    private static long lastMoveTime = System.currentTimeMillis();

    public static boolean isAFK(int afkTimeMs) { // <-- Dynamische Zeit als Parameter
        if (mc.player == null) return false;

        Vec3d currentPos = mc.player.getPos();

        // Hat sich der Spieler bewegt?
        if (!currentPos.equals(lastPos)) {
            lastPos = currentPos;
            lastMoveTime = System.currentTimeMillis();
        }

        // Überprüfen, ob die AFK-Zeit überschritten wurde
        return System.currentTimeMillis() - lastMoveTime > afkTimeMs;
    }
}
