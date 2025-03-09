package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

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

    // Maps for different fonts
    private static final Map<Character, String> BLOCK_MAP = new HashMap<>();
    private static final Map<Character, String> BLACKLETTER_MAP = new HashMap<>();
    private static final Map<Character, String> BOLD_MAP = new HashMap<>();
    private static final Map<Character, String> SCRIPT_MAP = new HashMap<>();
    private static final Map<Character, String> DOUBLESTRUCK_MAP = new HashMap<>();
    private static final Map<Character, String> CIRCLED_MAP = new HashMap<>();
    private static final Map<Character, String> SQUARED_MAP = new HashMap<>();
    private static final Map<Character, String> REGIONAL_MAP = new HashMap<>();
    private static final Map<Character, String> SUBSCRIPT_MAP = new HashMap<>();
    private static final Map<Character, String> FRAKTUR_MAP = new HashMap<>();
    private static final Map<Character, String> DOUBLESTRUCK2_MAP = new HashMap<>();
    private static final Map<Character, String> SCRIPT2_MAP = new HashMap<>();
    private static final Map<Character, String> SMALLCAPS_MAP = new HashMap<>();
    private static final Map<Character, String> CIRCLED2_MAP = new HashMap<>();
    private static final Map<Character, String> SQUARED2_MAP = new HashMap<>();
    
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

        // Add more fonts here (Fraktur, Small Caps, etc.) by following similar patterns
        // You can continue adding the remaining fonts here, just follow the above pattern.
    }

    public ChatFonts() {
        super(LucidAddon.CATEGORY, "Chat Fonts", "Modify your chat messages with different fonts.");
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        event.message = convertText(event.message, fontStyle.get());
    }

    private String convertText(String message, FontStyle style) {
        Map<Character, String> fontMap = getFontMap(style);
        if (fontMap == null) return message; // Falls die Schriftart nicht existiert

        StringBuilder converted = new StringBuilder();
        for (char c : message.toLowerCase().toCharArray()) {
            converted.append(fontMap.getOrDefault(c, String.valueOf(c))); // Ersetze Zeichen
        }
        return converted.toString();
    }

    private Map<Character, String> getFontMap(FontStyle style) {
        switch (style) {
            case BLOCK: return BLOCK_MAP;
            case BLACKLETTER: return BLACKLETTER_MAP;
            case BOLD: return BOLD_MAP;
            case SCRIPT: return SCRIPT_MAP;
            case DOUBLESTRUCK: return DOUBLESTRUCK_MAP;
            case CIRCLED: return CIRCLED_MAP;
            case SQUARED: return SQUARED_MAP;
            case REGIONAL: return REGIONAL_MAP;
            // Continue for additional fonts here
            default: return BLOCK_MAP; // Standard Schriftart
        }
    }

    public enum FontStyle {
        BLOCK, BLACKLETTER, BOLD, SCRIPT, DOUBLESTRUCK, CIRCLED, SQUARED, REGIONAL
        // Add new fonts to the enum here as needed
    }
}
