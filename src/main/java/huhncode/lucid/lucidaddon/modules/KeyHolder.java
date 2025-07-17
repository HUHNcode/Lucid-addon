package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.Rotations;



import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import org.lwjgl.glfw.GLFW;

public class KeyHolder extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public KeyHolder() {
        super(LucidAddon.CATEGORY, "Key Listener", "Listens for a specific key press while the module is active.");
    }

    private final Setting<Boolean> isBlocked = sgGeneral.add(new BoolSetting.Builder()
        .name("block")
        .description("Whether the key listener blocks the key press.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> BlockMouse = sgGeneral.add(new BoolSetting.Builder()
        .name("block-mouse")
        .description("Blocks chaning the head rotation")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> LogMouse = sgGeneral.add(new BoolSetting.Builder()
        .name("log-mouse")
        .description("Logs mouse movements.")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    private void onKey(KeyEvent event) {
        ChatUtils.info("Key pressed: " + GLFW.glfwGetKeyName(event.key, 0));
        if (isBlocked.get()) {
            event.cancel();

        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        ChatUtils.info("Mouse button pressed: " + event.button);
        if (isBlocked.get()) {
            event.cancel();
        }
    }

    private float lastYaw;
    private float lastPitch;

    @Override
    public void onActivate() {
        if (mc.player != null) {
            lastYaw = mc.player.getYaw();
            lastPitch = mc.player.getPitch();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && LogMouse.get()) {
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();

            if (yaw != lastYaw || pitch != lastPitch) {
                
                ChatUtils.info(String.format("Look changed: dYaw=%.2f, dPitch=%.2f", yaw - lastYaw, pitch - lastPitch));
                
                if (BlockMouse.get()) {
                    ChatUtils.info("Set Cam Rotation");
                    mc.player.setYaw(lastYaw);
                    mc.player.setPitch(lastPitch);
                    
                    
                }
            }
        }
    }

}