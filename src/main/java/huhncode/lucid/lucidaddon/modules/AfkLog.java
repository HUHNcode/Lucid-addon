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
        .defaultValue(60) // 60 seconds
        .min(5) // 5 seconds
        .max(300) // 5 minutes
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
        IsAFK.reset(); // Reset the AFK timer when the module is activated
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        // If the player has just joined (less than approx. 1 second old), reset the AFK state
        if (mc.player.age < 20) { // Assumption: 20 ticks per second
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
            // If AutoReconnect does not exist in your version, you can remove this part
            Modules.get().get(AutoReconnect.class).toggle();
        }
        mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(reason));
    }
}
