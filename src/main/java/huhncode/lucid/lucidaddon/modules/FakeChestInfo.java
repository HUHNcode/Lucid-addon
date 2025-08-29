package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import huhncode.lucid.lucidaddon.ui.PlayerInfoDisplay; // Import of the UI display class
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;


public class FakeChestInfo extends Module {

    // currentScreenInstance is not necessarily needed here if the module is a one-time trigger
    // and PlayerInfoDisplay handles mc.setScreen.

    public FakeChestInfo() {
        super(LucidAddon.CATEGORY, "player-info-gui", "Displays a GUI with information about the local player.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) {
            ChatUtils.error("Player or world not loaded.");
            toggle(); // Deactivate the module if the GUI cannot be opened
            return;
        }

        // Call the static show method of PlayerInfoDisplay for the local player
        PlayerInfoDisplay.show(mc.player);

        // Deactivate the module immediately after the GUI is displayed (one-shot action)
        toggle();
    }

    @Override
    public void onDeactivate() {
        // Since PlayerInfoDisplay.show() calls mc.setScreen directly and the module deactivates itself,
        // no explicit closing logic for the screen that depends on this module instance is required here.
        // The screen is closed normally via Esc or other means.
    }
}