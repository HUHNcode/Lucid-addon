package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import huhncode.lucid.lucidaddon.ui.PlayerInfoDisplay; // Import der UI-Anzeigeklasse
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;


public class FakeChestInfo extends Module {

    // currentScreenInstance wird hier nicht unbedingt benötigt, wenn das Modul ein einmaliger Auslöser ist
    // und PlayerInfoDisplay mc.setScreen handhabt.

    public FakeChestInfo() {
        super(LucidAddon.CATEGORY, "player-info-gui", "Zeigt eine GUI mit Informationen zum lokalen Spieler an.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) {
            ChatUtils.error("Spieler oder Welt nicht geladen.");
            toggle(); // Deaktiviere das Modul, wenn die GUI nicht geöffnet werden kann
            return;
        }

        // Rufe die statische show-Methode von PlayerInfoDisplay für den lokalen Spieler auf
        PlayerInfoDisplay.show(mc.player);

        // Deaktiviere das Modul sofort, nachdem die GUI angezeigt wurde (One-Shot-Aktion)
        toggle();
    }

    @Override
    public void onDeactivate() {
        // Da PlayerInfoDisplay.show() mc.setScreen direkt aufruft und das Modul sich selbst deaktiviert,
        // ist hier keine explizite Schließlogik für den Bildschirm erforderlich, die von dieser Modulinstanz abhängt.
        // Der Bildschirm wird normal über Esc oder andere Mittel geschlossen.
    }
}