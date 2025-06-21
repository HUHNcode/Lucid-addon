package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import huhncode.lucid.lucidaddon.events.CommandAttemptEvent;

public class MeteorCommandTracker extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public MeteorCommandTracker() {
        super(LucidAddon.CATEGORY, "command-tracker", "Logs all attempted client-side commands to chat.");
    }
    @EventHandler
    private void onCommandAttempt(CommandAttemptEvent event) {
        // Event-Handler hier
        ChatUtils.info("Command attempted: " + event.message);
        event.cancel();
    }
}