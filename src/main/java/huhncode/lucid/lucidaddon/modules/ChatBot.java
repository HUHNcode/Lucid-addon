package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private static final Path CONFIG_PATH = MeteorClient.FOLDER.toPath().resolve("Lucid/ChatBot.config");

    // Regex to extract player and message (PLAYER placeholder is replaced)
    private final Setting<String> messageRegex = sgGeneral.add(new StringSetting.Builder()
            .name("Message regex")
            .description("Regex to extract player and message. Everything after the match is treated as the message. Use 'PLAYER' as a placeholder.")
            .defaultValue("<PLAYER>\\s")
            .build()
    );

    // Delay before the response is sent (in seconds)
    private final Setting<Double> msgDelay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Message delay")
            .description("Delay (in seconds) until the response is sent.")
            .defaultValue(1.5)
            .min(0)
            .sliderMax(10)
            .build()
    );

    // Delay that prevents the same bot message from being sent to the same player again
    // Range: 0s (disabled) to 300s (5 minutes), Default: 10s.
    private final Setting<Double> duplicateDelay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Duplicate prevention delay")
            .description("Minimum time (in seconds) that must pass between sending the same bot message to the same player. 0 disables this check.")
            .defaultValue(10.0)
            .min(0)
            .sliderMax(300)
            .build()
    );

    private final Setting<List<String>> neededKeywords = sgGeneral.add(new StringListSetting.Builder()
            .name("Needed keywords")
            .description("The message must contain at least one of these words.")
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
            .description("When enabled, an item is only searched for after a 'needed keyword' (up to a 'forbidden keyword').")
            .defaultValue(true)
            .build()
    );

    // Setting for the output message, with placeholders TRIGGER and OUTPUT.
    private final Setting<String> customOutputMessage = sgGeneral.add(new StringSetting.Builder()
            .name("Custom output message")
            .description("Customize the message format. Use placeholders TRIGGER and OUTPUT. The message prefix (e.g., '/msg PLAYER') can also be adjusted.")
            .defaultValue("/msg PLAYER i am selling TRIGGER for OUTPUT")
            .build()
    );

    private final Setting<List<String>> itemData = sgGeneral.add(new StringListSetting.Builder()
            .name("Trigger and outputs")
            .description("Format: \"trigger1,trigger2;output\". No default values.")
            .defaultValue(loadItemData())
            .build()
    );

    // Map to store the last send time for each combination of player and message
    private final Map<String, Long> lastSentTimes = new HashMap<>();

    public ChatBot() {
        super(LucidAddon.CATEGORY, "ChatBot", "Detects chat messages and responds based on configurable regex patterns.");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (event.getMessage() == null) return;

        Map.Entry<String, Map.Entry<String, String>> extractedInfo = extractMessageInfo(event.getMessage().getString());
        if (extractedInfo != null) {
            String sender = extractedInfo.getKey();
            String word = extractedInfo.getValue().getKey();
            String price = extractedInfo.getValue().getValue();

            // Generate the output message using the custom format
            String outputMessage = customOutputMessage.get()
                    .replace("PLAYER", sender)
                    .replace("TRIGGER", word)
                    .replace("OUTPUT", price);

            // Duplicate Prevention: Check if the same message was recently sent to the same player
            if (duplicateDelay.get() > 0) {
                long now = System.currentTimeMillis();
                String key = sender + "_" + outputMessage;
                if (lastSentTimes.containsKey(key) && now - lastSentTimes.get(key) < duplicateDelay.get() * 1000) {
                    // Skip sending if the minimum time has not yet passed
                    return;
                }
                lastSentTimes.put(key, now);
            }

            // Show a toast and play a sound
            mc.execute(() -> {
                mc.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of(sender), Text.of(outputMessage)));
                mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            });

            // Send the message after the msgDelay time has elapsed
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ChatUtils.sendPlayerMsg(outputMessage);
                }
            }, (long) (msgDelay.get() * 1000));
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

    private static List<String> loadItemData() {
        if (!Files.exists(CONFIG_PATH)) {
            System.out.println("Config file does not exist. Creating default.");
            saveDefaultConfig();
            return List.of("test;optutest");
        }

        try {
            List<String> lines = Files.readAllLines(CONFIG_PATH);
            System.out.println("Config loaded successfully.");
            return lines;
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
            return List.of("test;optutest");
        }
    }

    private static void saveDefaultConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.write(CONFIG_PATH, List.of("test;optutest"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Default config saved successfully.");
        } catch (IOException e) {
            System.err.println("Failed to save default config: " + e.getMessage());
        }
    }    

}
