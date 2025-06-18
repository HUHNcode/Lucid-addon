package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import huhncode.lucid.lucidaddon.ui.PlayerInfoDisplay; // Import der UI-Anzeigeklasse
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
        super(LucidAddon.CATEGORY, "player-info", "Zeigt eine GUI mit Informationen zu einem bestimmten Spieler an.");
    }

    @Override
    public void onActivate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || playerName.get().isEmpty()) {
            ChatUtils.error("Spieler oder Welt nicht geladen.");
            toggle(); // Deaktiviere das Modul, wenn die GUI nicht geöffnet werden kann
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
            ChatUtils.error("Spieler '" + playerName.get() + "' nicht gefunden.");
            toggle();
            return;
        }

        PlayerInfoDisplay.show(targetPlayer);

        // Deaktiviere das Modul sofort, nachdem die GUI angezeigt wurde (One-Shot-Aktion)
        toggle();
    }

    @Override
    public void onDeactivate() {
        // Nichts zu tun hier, da das Modul sich selbst deaktiviert.
    }
}