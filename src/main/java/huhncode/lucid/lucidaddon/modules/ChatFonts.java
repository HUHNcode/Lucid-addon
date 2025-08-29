package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.message.SentMessage.Chat;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;

import java.util.HashMap;
import java.util.Map;

public class ChatFonts extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<FontStyle> fontStyle = sgGeneral.add(new EnumSetting.Builder<FontStyle>()
            .name("font-style")
            .description("Select the font style for chat messages.")
            .defaultValue(FontStyle.BLOCK)
            .build()
    );
    
    private final Setting<Boolean> applyFromSyntax = sgGeneral.add(new BoolSetting.Builder()
            .name("apply-from-syntax")
            .description("Apply font only between $f markers. (Beginwhit $f and optionally end with $f)")
            .defaultValue(true)
            .build()
    );

    private static final String SYNTAX_MARKER = "$f";
    
    private static final Map<Character, String> BLOCK_MAP = new HashMap<>();
    private static final Map<Character, String> BLACKLETTER_MAP = new HashMap<>();
    private static final Map<Character, String> BOLD_MAP = new HashMap<>();
    private static final Map<Character, String> SCRIPT_MAP = new HashMap<>();
    private static final Map<Character, String> DOUBLESTRUCK_MAP = new HashMap<>();
    private static final Map<Character, String> CIRCLED_MAP = new HashMap<>();
    private static final Map<Character, String> SQUARED_MAP = new HashMap<>();
    private static final Map<Character, String> REGIONAL_MAP = new HashMap<>();
    
    static {
        // Block font mapping
        char[] letters = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        String[] blockLetters = {
            "🅰", "🅱", "🅲", "🅳", "🅴", "🅵", "🅶", "🅷", "🅸", "🅹", 
            "🅺", "🅻", "🅼", "🅽", "🅾", "🅿", "🆀", "🆁", "🆂", "🆃", 
            "🆄", "🆅", "🆆", "🆇", "🆈", "🆉"
        };
        for (int i = 0; i < letters.length; i++) {
            BLOCK_MAP.put(letters[i], blockLetters[i]);
        }

        // Blackletter font mapping
        String[] blackletterLetters = {
            "𝔄", "𝔅", "ℭ", "𝔇", "𝔈", "𝔉", "𝔊", "ℌ", "ℑ", "𝔍", 
            "𝔎", "𝔏", "𝔐", "𝔑", "𝔒", "𝔓", "𝔔", "ℜ", "𝔖", "𝔗", 
            "𝔘", "𝔙", "𝔚", "𝔛", "𝔜", "ℨ"
        };
        for (int i = 0; i < letters.length; i++) {
            BLACKLETTER_MAP.put(letters[i], blackletterLetters[i]);
        }

        // Bold font mapping
        String[] boldLetters = {
            "𝕬", "𝕭", "𝕮", "𝕯", "𝕰", "𝕱", "𝕲", "𝕳", "𝕴", "𝕵", 
            "𝕶", "𝕷", "𝕸", "𝕹", "𝕺", "𝕻", "𝑸", "𝕽", "𝕾", "𝕿", 
            "𝖀", "𝖁", "𝖂", "𝖃", "𝖄", "𝖅"
        };
        for (int i = 0; i < letters.length; i++) {
            BOLD_MAP.put(letters[i], boldLetters[i]);
        }

        // Script font mapping
        String[] scriptLetters = {
            "𝒜", "𝐵", "𝒞", "𝒟", "𝐸", "𝐹", "𝒢", "𝐻", "𝐼", "𝒥", 
            "𝒦", "𝐿", "𝑀", "𝒩", "𝒪", "𝒫", "𝒬", "𝑅", "𝒮", "𝒯", 
            "𝒰", "𝒱", "𝒲", "𝒳", "𝒴", "𝒵"
        };
        for (int i = 0; i < letters.length; i++) {
            SCRIPT_MAP.put(letters[i], scriptLetters[i]);
        }

        // DoubleStruck font mapping
        String[] doubleStruckLetters = {
            "𝔸", "𝔹", "ℂ", "𝔻", "𝔼", "𝔽", "𝔾", "ℍ", "𝕀", "𝕁", 
            "𝕂", "𝕃", "𝕄", "ℕ", "𝕆", "ℙ", "ℚ", "ℝ", "𝕊", "𝕋", 
            "𝕌", "𝕍", "𝕎", "𝕏", "𝕐", "ℤ"
        };
        for (int i = 0; i < letters.length; i++) {
            DOUBLESTRUCK_MAP.put(letters[i], doubleStruckLetters[i]);
        }

        // Adding the new fonts
        String[] circledLetters = {
            "Ⓐ", "Ⓑ", "Ⓒ", "Ⓓ", "Ⓔ", "Ⓕ", "Ⓖ", "Ⓗ", "Ⓘ", "Ⓙ", 
            "Ⓚ", "Ⓛ", "Ⓜ", "Ⓝ", "Ⓞ", "Ⓟ", "Ⓠ", "Ⓡ", "Ⓢ", "Ⓣ", 
            "Ⓤ", "Ⓥ", "Ⓦ", "Ⓧ", "Ⓨ", "Ⓩ"
        };
        for (int i = 0; i < letters.length; i++) {
            CIRCLED_MAP.put(letters[i], circledLetters[i]);
        }

        String[] squaredLetters = {
            "🄰", "🄱", "🄲", "🄳", "🄴", "🄵", "🄶", "🄷", "🄸", "🄹", 
            "🄺", "🄻", "🄼", "🄽", "🄾", "🄿", "🅀", "🅁", "🅂", "🅃", 
            "🅄", "🅅", "🅆", "🅇", "🅈", "🅉"
        };
        for (int i = 0; i < letters.length; i++) {
            SQUARED_MAP.put(letters[i], squaredLetters[i]);
        }

        // Regional font mapping
        String[] regionalLetters = {
            "🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬", "🇭", "🇮", "🇯", 
            "🇰", "🇱", "🇲", "🇳", "🇴", "🇵", "🇶", "🇷", "🇸", "🇹", 
            "🇺", "🇻", "🇼", "🇽", "🇾", "🇿"
        };
        for (int i = 0; i < letters.length; i++) {
            REGIONAL_MAP.put(letters[i], regionalLetters[i]);
        }
    }
    
    public ChatFonts() {
        super(LucidAddon.CATEGORY, "Chat Fonts", "Modify your chat messages with different fonts.");
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        event.message = convertText(event.message, fontStyle.get(), applyFromSyntax.get());
        System.out.println("Converted Message: " + event.message); // Debug output
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Send event) {
        
        Packet<?> packet = event.packet;
        if (packet instanceof ChatCommandSignedC2SPacket) {
            ChatCommandSignedC2SPacket chatPacket = (ChatCommandSignedC2SPacket) packet;
            String command = chatPacket.command();
            if (command.contains(SYNTAX_MARKER)) {
                event.cancel();
                String PrivateMessageCommand = command.substring(0, command.indexOf(" ", command.indexOf("msg ") + 4));
                String PrivateMessage = command.replace(PrivateMessageCommand, "").strip();
                
                ChatUtils.sendPlayerMsg("/" + PrivateMessageCommand + " " + convertText(PrivateMessage, fontStyle.get(), applyFromSyntax.get()));
                
            }
        } 

        
            
            
        

    };

    private String convertText(String message, FontStyle style, boolean applyFromSyntax) {
        Map<Character, String> fontMap = getFontMap(style);
        if (fontMap == null) return message;
        
        if (applyFromSyntax) {
            StringBuilder result = new StringBuilder();
            int index = 0;
            boolean applyingFont = false;
            
            while (index < message.length()) {
                int markerIndex = message.indexOf(SYNTAX_MARKER, index);
                if (markerIndex == -1) {
                    // If no more markers are found, add the rest of the text
                    result.append(applyingFont ? applyFont(message.substring(index), fontMap) : message.substring(index));
                    break;
                }

                // Text before the marker
                result.append(applyingFont ? applyFont(message.substring(index, markerIndex), fontMap) : message.substring(index, markerIndex));
                // Toggle the formatting status
                applyingFont = !applyingFont;
                // Skip over the marker
                index = markerIndex + SYNTAX_MARKER.length();
            }
            return result.toString();
        }

        return applyFont(message, fontMap);
    }

    private String applyFont(String text, Map<Character, String> fontMap) {
        StringBuilder converted = new StringBuilder();
        for (char c : text.toCharArray()) {
            converted.append(fontMap.getOrDefault(Character.toLowerCase(c), String.valueOf(c)));
        }
        return converted.toString();
    }

    private Map<Character, String> getFontMap(FontStyle style) {
        return switch (style) {
            case BLOCK -> BLOCK_MAP;
            case BLACKLETTER -> BLACKLETTER_MAP;
            case BOLD -> BOLD_MAP;
            case SCRIPT -> SCRIPT_MAP;
            case DOUBLESTRUCK -> DOUBLESTRUCK_MAP;
            case CIRCLED -> CIRCLED_MAP;
            case SQUARED -> SQUARED_MAP;
            case REGIONAL -> REGIONAL_MAP;
        };
    }

    public enum FontStyle {
        BLOCK, BLACKLETTER, BOLD, SCRIPT, DOUBLESTRUCK, CIRCLED, SQUARED, REGIONAL
    }
}
