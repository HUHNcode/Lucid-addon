package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.Utils;

import java.util.*;

public class My_test extends Module {
    // Map mit Item-Namen und Preisen
    private final Map<List<String>, String> items = new HashMap<>() {{
        put(List.of("Diamonds", "Dias"), "30c");
        put(List.of("xp bottels", "xp"), "20c");
        put(List.of("emerald blocks", "ems", "emblocks"), "35c");
        put(List.of("iron block", "iron"), "20c");
        put(List.of("lapis block", "lapislazuli", "lapis"), "25c");
        put(List.of("obsidian", "obi", "oby"), "15c");
        put(List.of("redstone blocks", "redstone"), "20c");
        put(List.of("wood", "logs"), "5c");
        put(List.of("bookshelfs"), "20c");
        put(List.of("endcrystal", "crystals"), "35c");
        put(List.of("extra hearts", "hearts", "heart"), "5c");
        put(List.of("books"), "8c");
        put(List.of("netherite scraps", "scraps"), "5c");
        put(List.of("netherite ingot", "netherite"), "20c");
        put(List.of("nether upgrade", "Smithing Template"), "10c");
        put(List.of("pot shulk", "potion", "pots"), "10c");
        put(List.of("player head", "head"), "5c");
        put(List.of("enchanted book", "ebook"), "5c");
        put(List.of("golden carrots", "gcarrots", "carrots", "food"), "10c");
        put(List.of("steak", "food"), "5c");
        put(List.of("bread", "food"), "2c");
        put(List.of("gset", "g set"), "35c");
        put(List.of("totem shulk", "totems"), "5c");
    }};

    public My_test() {
        super(AddonTemplate.CATEGORY, "My Test", "Eine Testbeschreibung.");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (event.getMessage() == null) return;
        
        String message = event.getMessage().getString();
        if (shouldIgnoreMessage(message)) return;

        System.out.println("Nachricht erhalten: " + message);

        // Nachricht verarbeiten und Antwort senden
        String response = processMessage(message);
        if (response != null) {
            try {
                Thread.sleep(1500); // 200ms warten, bevor die Nachricht gesendet wird
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ChatUtils.sendPlayerMsg(response);
        }
    }

    /**
     * Überprüft, ob die Nachricht ignoriert werden soll (z. B. eigene Antworten)
     */
    private boolean shouldIgnoreMessage(String message) {
        return message.contains("Nachricht erhalten:") ||
               message.contains("Last part:") ||
               message.contains("Sender:") ||
               message.contains("Nachricht:") ||
               message.contains("Gefunden:") ||
               message.contains("/msg ") ||
               message.contains("[Meteor]");
    }

    /**
     * Verarbeitet die empfangene Nachricht und erstellt eine Antwort, falls ein Item gefunden wird.
     */
    private String processMessage(String message) {
        // Nachricht in Teile splitten
        String[] parts = message.split("›");
        if (parts.length < 2) return null;

        String messageContent = parts[parts.length - 1].trim();
        String messageSender = extractSender(parts[0]);

        // Item in der Nachricht suchen
        String itemInfo = findItemInMessage(messageContent);
        if (itemInfo == null) return null;

        return "/msg " + messageSender + " " + itemInfo;
    }

    /**
     * Extrahiert den Sender aus dem Nachrichtenteil.
     */
    private String extractSender(String messagePart) {
        String[] senderParts = messagePart.trim().split(" ");
        return senderParts[senderParts.length - 1]; // Letztes Wort als Sendername
    }

    /**
     * Sucht nach einem passenden Item in der Nachricht und gibt dessen Preis zurück.
     */
    private String findItemInMessage(String messageContent) {
        String lowerMessage = messageContent.toLowerCase();

        // Definiere benötigte und verbotene Schlüsselwörter
        List<String> needed_keywords = List.of("buy");
        List<String> forbidden_keywords = List.of("sell");

        // Prüfe, ob mindestens ein benötigtes Schlüsselwort in der Nachricht enthalten ist
        boolean hasNeededKeyword = needed_keywords.stream().anyMatch(lowerMessage::contains);

        // Prüfe, ob ein verbotenes Schlüsselwort in der Nachricht enthalten ist
        boolean hasForbiddenKeyword = forbidden_keywords.stream().anyMatch(lowerMessage::contains);

        // Falls kein benötigtes Keyword vorhanden ist oder ein verbotenes Keyword gefunden wurde, abbrechen
        if (!hasNeededKeyword || hasForbiddenKeyword) return null;

        // Suche nach einem Item in der Nachricht
        for (Map.Entry<List<String>, String> entry : items.entrySet()) {
            for (String keyword : entry.getKey()) {
                if (lowerMessage.contains(keyword.toLowerCase())) {
                    System.out.println("Gefunden: " + keyword + " - Preis: " + entry.getValue());
                    return keyword + " - " + entry.getValue(); // Erster gültiger Treffer wird zurückgegeben
                }
            }
        }

        return null; // Kein gültiges Item gefunden
    }
}
