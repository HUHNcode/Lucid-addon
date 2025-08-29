package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.WidgetScreen; // Use WidgetScreen
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class PlayerInfo extends Module {

    public PlayerInfo() {
        super(LucidAddon.CATEGORY, "player-info", "Displays a GUI with information about a player.");
    }

    @EventHandler
    private void onChatMessage(SendMessageEvent event) {
        if (!event.message.startsWith(".info ")) return;

        String playerName = event.message.substring(6).trim();
        PlayerListEntry player = mc.getNetworkHandler().getPlayerList().stream()
            .filter(p -> p.getProfile().getName().equalsIgnoreCase(playerName))
            .findFirst()
            .orElse(null);

        if (player == null) {
            info("Player not found: " + playerName);
            return;
        }

        // Show the custom WidgetScreen GUI
        mc.execute(() -> mc.setScreen(new PlayerInfoScreen(player)));
        event.cancel();
    }

    private static class PlayerInfoScreen extends WidgetScreen { // WidgetScreen instead of WScreen
        private final PlayerListEntry player;

        public PlayerInfoScreen(PlayerListEntry player) {
            this.player = player;
        }

        @Override
        public void init() {
            // Initialize the GUI and widgets
            WVerticalList list = new WVerticalList();
            list.add(theme.label(Text.of("Player name: " + player.getProfile().getName())));
            list.add(theme.label(Text.of("Ping: " + player.getLatency() + " ms")));
            // More information can be added here
            this.add(list); // Add the widget
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
        }
    }
}
