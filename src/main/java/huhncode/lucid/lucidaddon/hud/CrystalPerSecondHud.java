package huhncode.lucid.lucidaddon.hud;

import huhncode.lucid.lucidaddon.LucidAddon;
import huhncode.lucid.lucidaddon.services.CrystalBreakTracker;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class CrystalPerSecondHud extends HudElement {
    public static final HudElementInfo<CrystalPerSecondHud> INFO = new HudElementInfo<>(LucidAddon.HUD_GROUP, "crystal-per-second", "Displays the crystals destroyed per second.", CrystalPerSecondHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the text.")
        .defaultValue(new SettingColor(255, 255, 255))
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    public CrystalPerSecondHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        // Get the CPS from the central tracker service
        int cps = (LucidAddon.crystalBreakTracker != null) ? LucidAddon.crystalBreakTracker.getCps() : 0;
        String text = "Crystals/s: " + cps;

        setSize(renderer.textWidth(text, shadow.get()), renderer.textHeight(shadow.get()));

        // Render the text
        renderer.text(text, x, y, color.get(), shadow.get());
    }
}
