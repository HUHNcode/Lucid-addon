package huhncode.lucid.lucidaddon.commands;

import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;  // Korrekt importiert
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;

import net.minecraft.client.util.math.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import meteordevelopment.orbit.EventHandler;


import net.minecraft.client.util.math.MatrixStack;

public class PlayerInfoCommand extends Command {
    private PlayerEntity targetPlayer;
    private long displayTime;
    private static final int DISPLAY_DURATION = 5000; // Display time in milliseconds
    private final Color BACKGROUND_COLOR = new Color(0, 0, 0, 150); // Background color
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public PlayerInfoCommand() {
        super("info", "Displays player info in a popup.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("info")
            .then(argument("player", StringArgumentType.word())
                .executes(context -> {
                    String playerName = StringArgumentType.getString(context, "player");
                    // Der Rest deines Codes bleibt unverändert...
                    return SINGLE_SUCCESS;
                })
            )
        );
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (targetPlayer == null || System.currentTimeMillis() > displayTime) return; // If no player or time expired, don't display

        String playerName = targetPlayer.getName().getString();
        int ping = getPing(targetPlayer);
        String pingText = "Ping: " + ping + "ms";

        int x = 10, y = 10, width = 150, height = 70; // Define the position and size of the popup

        // Zeichne Hintergrund
        Renderer2D.fill(x, y, x + width, y + height, BACKGROUND_COLOR.getPacked()); // Ersetze 'fill' mit 'Renderer2D.fill'

        // Render Text
        mc.textRenderer.draw(Text.of(playerName), x + 5, y + 5, 0xFFFFFF);
        mc.textRenderer.draw(Text.of(pingText), x + 5, y + 20, 0x00FF00);

        // Render Items
        for (int i = 0; i < 6; i++) {
            ItemStack stack = getItem(targetPlayer, i);
            if (!stack.isEmpty()) {
                event.getMatrixStack().push();
                Renderer2D.drawItem(stack, x + 5 + i * 18, y + 30); // Draw items in a horizontal line
                event.getMatrixStack().pop();
            }
        }
    }

    private int getPing(PlayerEntity player) {
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        return entry != null ? entry.getLatency() : 0; // Return the player's ping (latency)
    }

    private ItemStack getItem(PlayerEntity player, int index) {
        return switch (index) {
            case 0 -> player.getMainHandStack();
            case 1 -> player.getInventory().armor.get(3);
            case 2 -> player.getInventory().armor.get(2);
            case 3 -> player.getInventory().armor.get(1);
            case 4 -> player.getInventory().armor.get(0);
            case 5 -> player.getOffHandStack();
            default -> ItemStack.EMPTY;
        };
    }
}
