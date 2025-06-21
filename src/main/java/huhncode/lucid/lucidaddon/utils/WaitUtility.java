package huhncode.lucid.lucidaddon.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;

public class WaitUtility {
    private static final MinecraftClient mc = MeteorClient.mc;

    /**
     * Pauses execution on a new thread and then runs a task on the main game thread.
     * This prevents the game from freezing.
     *
     * @param milliseconds The time to wait in milliseconds.
     * @param onComplete   The task to execute on the main thread after the delay.
     */
    public static void sleep(long milliseconds, Runnable onComplete) {
        new Thread(() -> {
            try {
                Thread.sleep(milliseconds);
                mc.execute(onComplete);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}