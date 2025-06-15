package huhncode.lucid.lucidaddon.modules;


import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.SettingGroup;
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


    @EventHandler
    private void onReceivePacket(PacketEvent.Send event) {
        
        Packet<?> packet = event.packet;
        if (packet instanceof CommandExecutionC2SPacket) {
            CommandExecutionC2SPacket chatPacket = (CommandExecutionC2SPacket) packet;
            String command = chatPacket.command().replace("/", "");
            
            if (command.contains("&&")) {
                event.cancel();
                String[] commands = command.split("&&");
                for (String c : commands) {
                    
                    mc.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket(c.strip()));
                    try {
                        Thread.sleep(delay.get());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
            }
            
        };

    };

    
}
