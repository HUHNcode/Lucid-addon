package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import huhncode.lucid.lucidaddon.ui.PlayerInfoDisplay; // Import of the UI display class
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;


public class PlayerInfoModule extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> playerName = sgGeneral.add(new StringSetting.Builder()
        .name("player-name")
        .description("The name of the player to display information for.")
        .defaultValue("Player")
        .build()
    );

    public PlayerInfoModule() {
        super(LucidAddon.CATEGORY, "player-info", "Displays a GUI with information about a specific player.");
    }

    @Override
    public void onActivate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || playerName.get().isEmpty()) {
            ChatUtils.error("Player or world not loaded.");
            toggle(); // Deactivate the module if the GUI cannot be opened
            return;
        }

        PlayerEntity targetPlayer = null;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(playerName.get())) {
                targetPlayer = p;
                break;
            }
        }

        if (targetPlayer == null) {
            ChatUtils.error("Player '" + playerName.get() + "' not found.");
            toggle();
            return;
        }

        PlayerInfoDisplay.show(targetPlayer);

        // Deactivate the module immediately after the GUI is displayed (one-shot action)
        toggle();
    }

    @Override
    public void onDeactivate() {
        // Nothing to do here, as the module deactivates itself.
    }
}