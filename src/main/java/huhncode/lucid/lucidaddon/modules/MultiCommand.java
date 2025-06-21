package huhncode.lucid.lucidaddon.modules;


import huhncode.lucid.lucidaddon.LucidAddon;
import huhncode.lucid.lucidaddon.events.CommandAttemptEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import net.minecraft.network.packet.Packet;
import meteordevelopment.meteorclient.settings.*;



public class MultiCommand extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public MultiCommand() {
        super(LucidAddon.CATEGORY, "MultiCommand", "MultiCommand");
    }


    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay in milliseconds between commands.")
            .defaultValue(50)
            .min(0)
            .sliderMax(5000)
            .build()
    );

    private void SendCommands(String command) {
        new Thread(() -> {
            String[] commands = command.split("&&");

            for (String c : commands) {
                c = c.strip(); // Whitespace entfernen
                

                // Alles was mit Minecraft-API zu tun hat, muss im Main Thread ausgeführt werden!
                String finalCommand = c;
                mc.execute(() -> {
                    if (finalCommand.startsWith("/")) {
                        if (finalCommand.startsWith("/msg ")) {
                            ChatUtils.sendPlayerMsg(finalCommand);
                        } else {
                            mc.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket(finalCommand.replace("/", "")));
                        }
                    } else if (finalCommand.startsWith(".")) {
                        ChatUtils.sendPlayerMsg(finalCommand);
                    }
                });

                // Delay zwischen den Befehlen (z. B. 500 ms)
                try {
                    Thread.sleep(delay.get()); // delay.get() in ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    @EventHandler
    private void onReceivePacket(PacketEvent.Send event) {
        
        Packet<?> packet = event.packet;
        if (packet instanceof CommandExecutionC2SPacket || packet instanceof ChatCommandSignedC2SPacket) {

            String chatPacket = null;
            if (packet instanceof CommandExecutionC2SPacket) {
                chatPacket = ((CommandExecutionC2SPacket) packet).command();
            }
            if (packet instanceof ChatCommandSignedC2SPacket) {
                chatPacket = ((ChatCommandSignedC2SPacket) packet).command();
            }

            if (chatPacket == null) {
                return;
            }

            String mccommand = "/" + chatPacket;
            
            if (mccommand.contains("&&")) {
                SendCommands(mccommand);
                event.cancel();
            }
            else {
                return;
            }
            
            
        };



    };

    @EventHandler
    private void onCommandAttempt(CommandAttemptEvent event) {
        
        String meteorcommand = "." + event.message;
        
        if (meteorcommand.contains("&&")) {
            SendCommands(meteorcommand);
            event.cancel();
                
        }
        else {
            return;
        };    
        
        

    }

}

    

