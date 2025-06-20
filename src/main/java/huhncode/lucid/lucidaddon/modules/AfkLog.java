package huhncode.lucid.lucidaddon.modules;


import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;

import huhncode.lucid.lucidaddon.LucidAddon;
import huhncode.lucid.lucidaddon.modules.utils.IsAFK;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

public class AfkLog extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> afkDelay = sgGeneral.add(new IntSetting.    Builder()
        .name("afk-delay")
        .description("How long do you have to be AFK before logging out? (in seconds)")
        .defaultValue(60) // 60 Sekunden
        .min(5) // 5 Sekunden
        .max(300) // 5 Minuten
        .sliderMax(300)
        .build()
    );

    private final Setting<Boolean> toggleAutoReconnect = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-auto-reconnect")
        .description("Whether to disable Auto Reconnect after a logout.")
        .defaultValue(true)
        .build()
    );

    public AfkLog() {
        super(LucidAddon.CATEGORY, "Afk-Log", "Automatically logs out when AFK for too long.");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        IsAFK.reset(); // Setze den AFK-Timer zurück, wenn das Modul aktiviert wird
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        // Wenn der Spieler neu beigetreten ist (weniger als ca. 1 Sekunde alt), den AFK-Zustand zurücksetzen
        if (mc.player.age < 20) { // Annahme: 20 Ticks pro Sekunde
            IsAFK.reset();
            return;
        }
        if (IsAFK.isAFK(afkDelay.get() * 1000)) {
            disconnect("[AfkLog] Player was AFK for too long.");
        }
    }

    private void disconnect(String reason) {
        disconnect(Text.literal(reason));
    }

    private void disconnect(Text reason) {
        if (toggleAutoReconnect.get()) {
            // Falls AutoReconnect in deiner Version nicht existiert, kannst du diesen Teil entfernen
            Modules.get().get(AutoReconnect.class).toggle();
        }
        mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(reason));
    }
}
