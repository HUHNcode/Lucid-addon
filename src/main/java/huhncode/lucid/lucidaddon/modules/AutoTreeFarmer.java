package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;

public class AutoTreeFarmer extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public AutoTreeFarmer() {
        super(LucidAddon.CATEGORY, "MODULE NAME", "MODUEL BESCHREIBUNG");
    }

    @EventHandler
    private void onEvent() {
        // Event-Handler hier
    }
}