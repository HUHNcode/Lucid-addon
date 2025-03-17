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

    // Regex zum Extrahieren von Spieler und Nachricht (Platzhalter PLAYER wird ersetzt)
    private final Setting<String> messageRegex = sgGeneral.add(new StringSetting.Builder()
            .name("Message regex")
            .description("Regex zum Extrahieren von Spieler und Nachricht. Alles nach dem Match wird als Nachricht behandelt. Verwende 'PLAYER' als Platzhalter.")
            .defaultValue("<PLAYER>\\s")
            .build()
    );

    // Verzögerung, bevor die Antwort gesendet wird (in Sekunden)
    private final Setting<Double> msgDelay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Message delay")
            .description("Verzögerung (in Sekunden), bis die Antwort gesendet wird.")
            .defaultValue(1.5)
            .min(0)
            .sliderMax(10)
            .build()
    );

    // Verzögerung, die verhindert, dass dieselbe Bot-Nachricht an denselben Spieler erneut gesendet wird
    // Range: 0s (deaktiviert) bis 300s (5 Minuten), Standard: 10s.
    private final Setting<Double> duplicateDelay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Duplicate prevention delay")
            .description("Mindestzeit (in Sekunden), die zwischen dem Senden derselben Bot-Nachricht an denselben Spieler liegen muss. 0 deaktiviert diese Überprüfung.")
            .defaultValue(10.0)
            .min(0)
            .sliderMax(300)
            .build()
    );

    private final Setting<List<String>> neededKeywords = sgGeneral.add(new StringListSetting.Builder()
            .name("Needed keywords")
            .description("Die Nachricht muss mindestens eines dieser Wörter enthalten.")
            .defaultValue(List.of("buy"))
            .build()
    );

    private final Setting<List<String>> forbiddenKeywords = sgGeneral.add(new StringListSetting.Builder()
            .name("Forbidden keywords")
            .description("Nachrichten, die diese Wörter enthalten, werden ignoriert.")
            .defaultValue(List.of("sell"))
            .build()
    );

    private final Setting<Boolean> smartMode = sgGeneral.add(new BoolSetting.Builder()
            .name("Smart keyword detection")
            .description("Wenn aktiviert, wird nach einem Item nur nach einem 'needed keyword' (bis zu einem 'forbidden keyword') gesucht.")
            .defaultValue(true)
            .build()
    );

    // Einstellung für die Ausgabe-Nachricht, mit Platzhaltern TRIGGER und OUTPUT.
    private final Setting<String> customOutputMessage = sgGeneral.add(new StringSetting.Builder()
            .name("Custom output message")
            .description("Passe das Nachrichtenformat an. Verwende Platzhalter TRIGGER und OUTPUT. Der Nachrichtenprefix (z.B. '/msg PLAYER') kann ebenfalls angepasst werden.")
            .defaultValue("/msg PLAYER i am selling TRIGGER for OUTPUT")
            .build()
    );

    private final Setting<List<String>> itemData = sgGeneral.add(new StringListSetting.Builder()
            .name("Trigger and outputs")
            .description("Format: \"trigger1,trigger2;output\". Keine Standardwerte.")
            .defaultValue(List.of(
                "Diamonds,Dias;25$/stack",
                "Emeralds,Ems;10$/stack"))
            .build()
    );

    // Map, um für jede Kombination aus Spieler und Nachricht den letzten Sendezeitpunkt zu speichern
    private final Map<String, Long> lastSentTimes = new HashMap<>();

    public ChatBot() {
        super(LucidAddon.CATEGORY, "ChatBot", "Erkennt Chatnachrichten und antwortet basierend auf konfigurierbaren Regex-Mustern.");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (event.getMessage() == null) return;

        Map.Entry<String, Map.Entry<String, String>> extractedInfo = extractMessageInfo(event.getMessage().getString());
        if (extractedInfo != null) {
            String sender = extractedInfo.getKey();
            String word = extractedInfo.getValue().getKey();
            String price = extractedInfo.getValue().getValue();

            // Erzeuge die Ausgabe-Nachricht mithilfe des benutzerdefinierten Formats
            String outputMessage = customOutputMessage.get()
                    .replace("PLAYER", sender)
                    .replace("TRIGGER", word)
                    .replace("OUTPUT", price);

            // Duplicate Prevention: Überprüfe, ob dieselbe Nachricht an den selben Spieler kürzlich gesendet wurde
            if (duplicateDelay.get() > 0) {
                long now = System.currentTimeMillis();
                String key = sender + "_" + outputMessage;
                if (lastSentTimes.containsKey(key) && now - lastSentTimes.get(key) < duplicateDelay.get() * 1000) {
                    // Überspringe das Senden, wenn die Mindestzeit noch nicht verstrichen ist
                    return;
                }
                lastSentTimes.put(key, now);
            }

            // Zeige einen Toast an und spiele einen Sound ab
            mc.execute(() -> {
                mc.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of(sender), Text.of(outputMessage)));
                mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            });

            // Sende die Nachricht nach Ablauf der msgDelay-Zeit
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
}
