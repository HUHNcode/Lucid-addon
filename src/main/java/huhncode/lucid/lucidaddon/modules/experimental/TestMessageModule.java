package huhncode.lucid.lucidaddon.modules;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import huhncode.lucid.lucidaddon.LucidAddon;

public class TestMessageModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> enabled = sgGeneral.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Whether the module is enabled.")
        .defaultValue(true)
        .build()
    );

    private Thread messageThread;

    public TestMessageModule() {
        super(LucidAddon.CATEGORY, "TestMessage", "Sends 'test' message every 5 seconds.");
    }

    @Override
    public void onActivate() {
        messageThread = new Thread(() -> {
            while (isActive()) {
                ChatUtils.sendPlayerMsg("test");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        messageThread.start();
    }

    @Override
    public void onDeactivate() {
        if (messageThread != null) {
            messageThread.interrupt();
            messageThread = null;
        }
    }
}