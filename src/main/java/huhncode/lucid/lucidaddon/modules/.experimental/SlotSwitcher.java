package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.settings.*;
import java.util.*;

public class SlotSwitcher extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay between slot switches in ms")
        .defaultValue(500)
        .min(0)
        .sliderMax(5000)
        .build()
    );
    
    private final Setting<String> startMessage = sgGeneral.add(new StringSetting.Builder()
        .name("Start Message")
        .description("Message sent before switching slots.")
        .defaultValue("🆂🅴🅻🅻🅸🅽🅶:")
        .build()
    );
    
    private final Setting<List<String>> slotMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("Slot Messages")
        .description("Define slot and message pairs in format SLOT,MESSAGE")
        .defaultValue(Arrays.asList("4,Message 1", "5,Message 2", "6,Message 3"))
        .build()
    );

    public SlotSwitcher() {
        super(LucidAddon.CATEGORY, "SlotSwitcher", "Switches slots and sends messages once upon activation.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.interactionManager == null) return;
        
        ChatUtils.sendPlayerMsg(startMessage.get());
        
        List<String> slotsAndMessages = slotMessages.get();
        int totalDelay = delay.get();
        
        for (String entry : slotsAndMessages) {
            String[] parts = entry.split(",");
            if (parts.length != 2) continue;
            
            int slot;
            try {
                slot = Integer.parseInt(parts[0].trim());
            } catch (NumberFormatException e) {
                continue;
            }
            
            String message = parts[1].trim();
            
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    switchSlotAndMessage(slot, message);
                }
            }, totalDelay);
            
            totalDelay += delay.get();
        }
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toggle(); // Deactivate module after the action
            }
        }, totalDelay);
    }

    private void switchSlotAndMessage(int slot, String message) {
        if (mc.player == null || mc.player.getInventory() == null) return;
        
        mc.player.getInventory().selectedSlot = slot - 1; // Minecraft slots start at 0
        ChatUtils.sendPlayerMsg(message);
    }
}
