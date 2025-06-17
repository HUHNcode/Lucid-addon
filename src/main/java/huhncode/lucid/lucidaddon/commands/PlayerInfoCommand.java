package huhncode.lucid.lucidaddon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import huhncode.lucid.lucidaddon.ui.PlayerInfoDisplay;
// Importe waren teilweise doppelt oder nicht mehr benötigt
import com.mojang.brigadier.arguments.StringArgumentType;
// import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
// import net.minecraft.command.argument.EntityArgumentType; // Nicht für Client-seitige Spielersuche benötigt
import net.minecraft.entity.player.PlayerEntity;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class PlayerInfoCommand extends Command {
    public PlayerInfoCommand() {
        super("info", "Displays information about a player in a GUI.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Verwende StringArgumentType für den Spielernamen
        builder.then(argument("player", StringArgumentType.string())
            .executes(context -> {
                ChatUtils.info("PlayerInfoCommand: Befehl ausgeführt."); // DEBUG
                String playerName = StringArgumentType.getString(context, "player");
                PlayerEntity player = null;

                if (MinecraftClient.getInstance().world != null) {
                    ChatUtils.info("PlayerInfoCommand: Suche Spieler '%s' in der Welt.", playerName); // DEBUG
                    for (PlayerEntity p : MinecraftClient.getInstance().world.getPlayers()) {
                        if (p.getName().getString().equalsIgnoreCase(playerName)) {
                            player = p;
                            break;
                        }
                    }
                }
                ChatUtils.info("PlayerInfoCommand: Spieler gefunden: " + (player != null ? player.getName().getString() : "null")); // DEBUG

                if (player == null) {
                    
                    ChatUtils.error("Player not found.");
                    return 0;
                }
                ChatUtils.info("PlayerInfoCommand: Spieler '%s' gefunden. Versuche GUI zu öffnen.", player.getName().getString()); // DEBUG

                final PlayerEntity finalPlayer = player;
                
                // Stelle sicher, dass dies auf dem Client-Thread für GUI-Operationen ausgeführt wird
                MinecraftClient.getInstance().execute(() -> PlayerInfoDisplay.show(finalPlayer));
                 return SINGLE_SUCCESS;
            }));
    }
 }
    
