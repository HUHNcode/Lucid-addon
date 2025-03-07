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
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;

import java.util.*;

public class My_test extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> neededKeywords = sgGeneral.add(new StringListSetting.Builder()
        .name("Needed keywords")
        .description("keywords that have to be in a message")
        .defaultValue(List.of("buy"))
        .build()
    );

    private final Setting<List<String>> forbiddenKeywords = sgGeneral.add(new StringListSetting.Builder()
        .name("Forbidden keywords")
        .description("keywords that can't be in a message")
        .defaultValue(List.of("sell"))
        .build()
    );

    private final Setting<Boolean> enabled = sgGeneral.add(new BoolSetting.Builder()
        .name("Smart needed/forbidden keywords")
        .description("Finds items after needed keywords, but not after forbidden keywords")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Message delay")
        .description("delay between messages in ms")
        .defaultValue(200)
        .min(0)
        .sliderMax(5000)
        .build()
    );

    private final Setting<List<String>> itemData = sgGeneral.add(new StringListSetting.Builder()
        .name("Trigger and outputs")
        .description("Words that have to be in a message (Trigger) and words that will be sent (Output).\nUse ; to separate trigger and output, you can separate multiple triggers with ','")
        .defaultValue(List.of(
            "Diamonds,Dias;30c/stack",
            "xp bottles,xp;20c/stack",
            "emerald blocks,ems,emblocks;35c/stack",
            "iron block,iron;20c/stack",
            "lapis block,lapislazuli,lapis;25c/stack",
            "obsidian,obi,oby;15c/stack",
            "redstone blocks,redstone;20c/stack",
            "wood,logs;5c/stack",
            "bookshelfs;20c/stack",
            "endcrystal,crystals;35c/stack",
            "extra hearts,hearts,heart;5c",
            "books;8c/stack",
            "netherite scraps,scraps;5c",
            "netherite ingot,netherite;20c",
            "nether upgrade,Smithing Template;10c",
            "pot shulk,potion,pots;10c",
            "player head,head;5c",
            "enchanted book,ebook;5c",
            "golden carrots,gcarrots,carrots,food;10c/stack",
            "steak,food;5c/stack",
            "bread,food;2c/stack",
            "gset,g set;35c",
            "totem shulk,totems;5c"
        ))
        .build()
    );

    public My_test() {
        super(AddonTemplate.CATEGORY, "My Test", "Eine Testbeschreibung.");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (!enabled.get() || event.getMessage() == null) return;

        String message = event.getMessage().getString().toLowerCase();
        Map.Entry<String, Map.Entry<String, String>> extractedInfo = extractMessageInfo(message);
        if (extractedInfo != null) {
            String sender = extractedInfo.getKey();
            String word = extractedInfo.getValue().getKey();
            String price = extractedInfo.getValue().getValue();

            mc.execute(() -> mc.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of(sender), Text.of(word + " for " + price))));
            mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);


            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ChatUtils.sendPlayerMsg("/msg " + sender + " " + word + " for " + price);
                }
            }, delay.get());
        }
    }

    private Map.Entry<String, Map.Entry<String, String>> extractMessageInfo(String message) {
        String[] parts = message.split("›");
        if (parts.length < 2) return null;

        String messageContent = parts[parts.length - 1].trim();
        String messageSender = extractSender(parts[0]);

        Map.Entry<String, String> itemInfo = findItemAfterNeededKeyword(messageContent);
        if (itemInfo == null) return null;

        return new AbstractMap.SimpleEntry<>(messageSender, itemInfo);
    }

    private Map.Entry<String, String> findItemAfterNeededKeyword(String message) {
        List<String> needed = neededKeywords.get();
        List<String> forbidden = forbiddenKeywords.get();
        List<String> items = itemData.get();

        // Wenn Smart-Modus ausgeschaltet ist: ignoriert Nachricht, wenn ein forbiddenKeyword vorkommt, sonst in der gesamten Nachricht suchen.
        if (!enabled.get()) {
            for (String forbiddenWord : forbidden) {
                if (message.contains(forbiddenWord)) {
                    return null;
                }
            }
            return findMatchingItem(message);
        } else {
            // Smart-Modus an: Suche das erste Vorkommen eines neededKeyword.
            int neededPos = -1;
            for (String neededWord : needed) {
                int pos = message.indexOf(neededWord);
                if (pos != -1) {
                    // Nimm das kleinste Vorkommen (Ende des neededKeyword)
                    if (neededPos == -1 || pos < neededPos) {
                        neededPos = pos + neededWord.length();
                    }
                }
            }
            if (neededPos == -1) return null; // Kein neededKeyword gefunden.

            // Suche nach dem nächsten forbiddenKeyword ab neededPos.
            int forbiddenPos = Integer.MAX_VALUE;
            for (String forbiddenWord : forbidden) {
                int pos = message.indexOf(forbiddenWord, neededPos);
                if (pos != -1 && pos < forbiddenPos) {
                    forbiddenPos = pos;
                }
            }
            // Definiere den Suchbereich: Von neededPos bis zum nächsten forbiddenKeyword (falls vorhanden) oder bis zum Ende.
            String searchArea = forbiddenPos == Integer.MAX_VALUE ? message.substring(neededPos)
                                                                : message.substring(neededPos, forbiddenPos);
            // Suche das erste passende Item im definierten Suchbereich.
            return findMatchingItem(searchArea);
        }
    }

    // Sucht nach einem passenden Item in der übergebenen Nachricht (oder Teilstring) und gibt ein Map.Entry mit Trigger und Price zurück.
    private Map.Entry<String, String> findMatchingItem(String text) {
        for (String itemEntry : itemData.get()) {
            String[] itemParts = itemEntry.split(";");
            if (itemParts.length != 2) continue;
            List<String> keyList = Arrays.asList(itemParts[0].split(","));
            String price = itemParts[1];
            // Prüfe, ob einer der Trigger in dem Text vorkommt.
            for (String key : keyList) {
                if (text.contains(key)) {
                    return new AbstractMap.SimpleEntry<>(key, price);
                }
            }
        }
        return null;
    }


    private String extractSender(String messagePart) {
        String[] senderParts = messagePart.trim().split(" ");
        return senderParts[senderParts.length - 1];
    }
}