package com.example.addon.modules;

import com.example.addon.AddonTemplate;
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
    // Default: "<PLAYER>\s"
    private final Setting<String> messageRegex = sgGeneral.add(new StringSetting.Builder()
            .name("Message regex")
            .description("Regex to extract player and message. Everything after the match becomes the message. Use 'PLAYER' as placeholder for the player's name.")
            .defaultValue(".*\\sPLAYER\\s›\\s")
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

    private final Setting<List<String>> itemData = sgGeneral.add(new StringListSetting.Builder()
            .name("Trigger and outputs")
            .description("Format: \"trigger1,trigger2;output\". No default values.")
            .defaultValue(List.of(
            "Diamonds,Dias;30c/stack",
            "xp bottles,xp;20c/stack",
            "emerald blocks,ems,emblocks;35c/block stack",
            "iron block,iron;20c/block stack",
            "lapis block,lapislazuli,lapis;25c/block stack",
            "obsidian,obi,oby;15c/stack",
            "redstone blocks,redstone;20c/block stack",
            "wood,logs;5c/stack",
            "bookshelfs;20c/stack",
            "endcrystal,crystals;35c/stack",
            "extra hearts,hearts,heart;5c",
            "books;8c/stack",
            "netherite scraps,scraps;5c",
            "netherite ingot,netherite;20c",
            "nether upgrade,Smithing Template;10c",
            "pot shulk,potion,pots;10c/shulk",
            "player head,head;5c",
            "enchanted book,ebook;5c",
            "golden carrots,gcarrots,carrots,food;10c/stack",
            "steak,food;5c/stack",
            "bread,food;2c/stack",
            "gset,g set;35c",
            "totem shulk,totems;5c/shulk",
            "shells;15c/stack"))
            .build()
    );

    public ChatBot() {
    super(AddonTemplate.CATEGORY, "ChatBot", "Automatically detects and responds to chat messages based on configurable regex patterns.");
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
                mc.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of(sender), Text.of(word + " for " + price)));
                mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            });

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ChatUtils.sendPlayerMsg("/msg " + sender + " " + word + " for " + price);
                }
            }, (long)(delay.get() * 1000));
        }
    }

    // Parses the message using the regex setting.
    // Replaces "PLAYER" with a capturing group that matches one or more word characters.
    // Everything after the match is considered the message content.
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

    // Extracts the sender and item info from the message.
    private Map.Entry<String, Map.Entry<String, String>> extractMessageInfo(String message) {
        Map.Entry<String, String> parsed = parseMessage(message);
        if (parsed == null) return null;
        String sender = parsed.getKey();
        String content = parsed.getValue();
        Map.Entry<String, String> itemInfo = smartMode.get() ? findItemSmart(content) : (containsForbiddenKeyword(content) ? null : findMatchingItem(content));
        return (itemInfo != null) ? new AbstractMap.SimpleEntry<>(sender, itemInfo) : null;
    }

    // In smart mode, finds the first needed keyword and then searches in the text after it (up to a forbidden keyword, if present).
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

    // Checks if any forbidden keyword is present in the message.
    private boolean containsForbiddenKeyword(String message) {
        for (String forbiddenWord : forbiddenKeywords.get()) {
            if (message.contains(forbiddenWord)) return true;
        }
        return false;
    }

    // Searches for a matching item in the text and returns the trigger and output as a pair.
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
