package huhncode.lucid.lucidaddon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import huhncode.lucid.lucidaddon.ui.PlayerInfoDisplay;
// Importe waren teilweise doppelt oder nicht mehr benötigt
import com.mojang.brigadier.arguments.StringArgumentType;
// import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
// import net.minecraft.command.argument.EntityArgumentType; // Nicht für Client-seitige Spielersuche benötigt
import net.minecraft.entity.player.PlayerEntity;


import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class PlayerInfoCommand extends Command {
    private static boolean openingPlayerInfoGui = false;

    public PlayerInfoCommand() {
        super("info", "Displays information about a player in a GUI.");
    }

    public static boolean isOpeningPlayerInfoGui() {
        return openingPlayerInfoGui;
    }

    public static void setOpeningPlayerInfoGui(boolean opening) {
        openingPlayerInfoGui = opening;
    }


    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Verwende StringArgumentType für den Spielernamen
        builder.then(argument("player", PlayerListEntryArgumentType.create())
            
            .executes(context -> {
                
                String playerName = PlayerListEntryArgumentType.get(context).getProfile().getName();
                PlayerEntity player = null;

                if (MinecraftClient.getInstance().world != null) {
                    
                    for (PlayerEntity p : MinecraftClient.getInstance().world.getPlayers()) {
                        if (p.getName().getString().equalsIgnoreCase(playerName)) {
                            player = p;
                            break;
                        }
                    }
                }
                

                if (player == null) {
                    
                    ChatUtils.error("Player not found.");
                    return 0;
                }
                

                final PlayerEntity finalPlayer = player;
                setOpeningPlayerInfoGui(true); // Signalisiere, dass wir die GUI öffnen wollen

                // Stelle sicher, dass dies auf dem Client-Thread für GUI-Operationen ausgeführt wird
                MinecraftClient.getInstance().execute(() -> {
                    PlayerInfoDisplay.show(finalPlayer);
                });
                 return SINGLE_SUCCESS;
            }));
    }
 }
    
