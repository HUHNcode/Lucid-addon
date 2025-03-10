package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // This setting defines the message format. The placeholder "PLAYER" will be replaced by a regex group.
    private final Setting<String> messageRegex = sgGeneral.add(new StringSetting.Builder()
            .name("Message regex")
            .description("Regex to extract player and message. Everything after the match becomes the message. Use 'PLAYER' as placeholder for the player's name.")
            .defaultValue("<PLAYER>\\s")
            .build()
    );

    // Delay between messages (in seconds)
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Message delay")
            .description("Delay between messages (in seconds).")
            .defaultValue(1.5)
            .min(0)
            .sliderMax(10)
            .build()
    );

    private final Setting<List<String>> neededKeywords = sgGeneral.add(new StringListSetting.Builder()
            .name("Needed keywords")
            .description("Messages must contain at least one of these words.")
            .defaultValue(List.of("buy"))
            .build()
    );

    private final Setting<List<String>> forbiddenKeywords = sgGeneral.add(new StringListSetting.Builder()
            .name("Forbidden keywords")
            .description("Messages containing these words will be ignored.")
            .defaultValue(List.of("sell"))
            .build()
    );

    private final Setting<Boolean> smartMode = sgGeneral.add(new BoolSetting.Builder()
            .name("Smart keyword detection")
            .description("When enabled, searches for an item only after a needed keyword (up to a forbidden keyword).")
            .defaultValue(true)
            .build()
    );

    // New setting for customizing the output message format (entire message structure).
    private final Setting<String> customOutputMessage = sgGeneral.add(new StringSetting.Builder()
            .name("Custom output message")
            .description("Customize the message format. Use placeholders TRIGGER and OUTPUT. You can also customize the message prefix (e.g., '/msg PLAYER').")
            .defaultValue("/msg PLAYER i am selling TRIGGER for OUTPUT")
            .build()
    );

    private final Setting<List<String>> itemData = sgGeneral.add(new StringListSetting.Builder()
            .name("Trigger and outputs")
            .description("Format: \"trigger1,trigger2;output\". No default values.")
            .defaultValue(List.of(
                "Diamonds,Dias;25$/stack",
                "Emeralds,Ems;10$/stack"))
            .build()
    );

    public ChatBot() {
        super(LucidAddon.CATEGORY, "ChatBot", "Automatically detects and responds to chat messages based on configurable regex patterns.");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (event.getMessage() == null) return;

        Map.Entry<String, Map.Entry<String, String>> extractedInfo = extractMessageInfo(event.getMessage().getString());
        if (extractedInfo != null) {
            String sender = extractedInfo.getKey();
            String word = extractedInfo.getValue().getKey();
            String price = extractedInfo.getValue().getValue();

            mc.execute(() -> {

                String outputMessage = customOutputMessage.get()
                        .replace("PLAYER", sender)  // Replace PLAYER with the actual sender
                        .replace("TRIGGER", word)    // Replace TRIGGER with the matched keyword
                        .replace("OUTPUT", price);   // Replace OUTPUT with the item price
                
                
                mc.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of(sender), Text.of(outputMessage)));
                mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            });

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // Use the custom message format with placeholders TRIGGER and OUTPUT, and allow full customization of the message prefix.
                    String outputMessage = customOutputMessage.get()
                        .replace("PLAYER", sender)  // Replace PLAYER with the actual sender
                        .replace("TRIGGER", word)    // Replace TRIGGER with the matched keyword
                        .replace("OUTPUT", price);   // Replace OUTPUT with the item price

                    // Send the customized message
                    ChatUtils.sendPlayerMsg(outputMessage);
                    
                }
            }, (long)(delay.get() * 1000));
        }
    }

    private Map.Entry<String, String> parseMessage(String message) {
        String regex = messageRegex.get().replace("PLAYER", "(\\w+)");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String sender = matcher.group(1);
            String content = message.substring(matcher.end()).trim();
            return new AbstractMap.SimpleEntry<>(sender, content);
        }
        return null;
    }

    private Map.Entry<String, Map.Entry<String, String>> extractMessageInfo(String message) {
        Map.Entry<String, String> parsed = parseMessage(message);
        if (parsed == null) return null;
        String sender = parsed.getKey();
        String content = parsed.getValue();
        Map.Entry<String, String> itemInfo = smartMode.get() ? findItemSmart(content) : (containsForbiddenKeyword(content) ? null : findMatchingItem(content));
        return (itemInfo != null) ? new AbstractMap.SimpleEntry<>(sender, itemInfo) : null;
    }

    private Map.Entry<String, String> findItemSmart(String message) {
        List<String> needed = neededKeywords.get();
        List<String> forbidden = forbiddenKeywords.get();

        int neededPos = -1;
        if (needed.isEmpty()) {
            neededPos = 0;
        } else {
            for (String neededWord : needed) {
                int pos = message.indexOf(neededWord);
                if (pos != -1 && (neededPos == -1 || pos < neededPos)) {
                    neededPos = pos + neededWord.length();
                }
            }
        }
        if (neededPos == -1) return null;

        int forbiddenPos = Integer.MAX_VALUE;
        for (String forbiddenWord : forbidden) {
            int pos = message.indexOf(forbiddenWord, neededPos);
            if (pos != -1 && pos < forbiddenPos) {
                forbiddenPos = pos;
            }
        }
        String searchArea = (forbiddenPos == Integer.MAX_VALUE) ? message.substring(neededPos)
                                                                : message.substring(neededPos, forbiddenPos);
        return findMatchingItem(searchArea);
    }

    private boolean containsForbiddenKeyword(String message) {
        for (String forbiddenWord : forbiddenKeywords.get()) {
            if (message.contains(forbiddenWord)) return true;
        }
        return false;
    }

    private Map.Entry<String, String> findMatchingItem(String text) {
        for (String itemEntry : itemData.get()) {
            String[] itemParts = itemEntry.split(";");
            if (itemParts.length != 2) continue;
            List<String> keyList = Arrays.asList(itemParts[0].split(","));
            String price = itemParts[1];
            for (String key : keyList) {
                if (text.contains(key)) {
                    return new AbstractMap.SimpleEntry<>(key, price);
                }
            }
        }
        return null;
    }
}
