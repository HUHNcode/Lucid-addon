package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import huhncode.lucid.lucidaddon.utils.IsAFK;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;

public class AfkLog extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> afkTime = sgGeneral.add(new IntSetting.Builder()
        .name("AFK Delay")
        .description("How long do you have to be AFK before logging out?")
        .defaultValue(60)  // Standard: 1 Minute
        .min(5)            // Minimum: 5 Sekunden
        .max(300)          // Maximum: 5 Minuten
        .sliderMax(300)    // Slider-Maximum
        .build()
    );

    private final Setting<Boolean> toggleAutoReconnect = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-auto-reconnect")
        .description("Disable AutoReconnect after an AFK logout.")
        .defaultValue(true)
        .build()
    );

    public AfkLog() {
        super(LucidAddon.CATEGORY, "AfkLog", "Logs out when you are AFK.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (IsAFK.isAFK(afkTime.get() * 1000)) { // AFK-Check (Sekunden in Millisekunden umwandeln)
            info("You are AFK! Logging out...");
            disconnect(Text.literal("AFK detected"));
        }
    }

    private void disconnect(Text reason) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        if (mc.player != null) {
            MutableText text = Text.literal("[AfkLog] ");
            text.append(reason);

            AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
            if (autoReconnect.isActive() && toggleAutoReconnect.get()) {
                text.append(Text.literal("\n\nINFO - AutoReconnect was disabled").styled(style -> style.withColor(0xAAAAAA)));
                autoReconnect.toggle();
            }
            
            mc.player.networkHandler.getConnection().disconnect(text);
        }
    }
}