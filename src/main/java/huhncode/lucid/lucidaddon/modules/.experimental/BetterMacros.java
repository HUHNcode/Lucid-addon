package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.WWidget;



import java.util.Collections;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BetterMacros extends Module {
    private final SettingGroup sgMacros = settings.createGroup("Macros");

    private final List<Setting<?>> macroSettings = new ArrayList<>();
    private int macroCount = 0;
    private final List<ParsedMacro> parsedMacros = new ArrayList<>();
    private boolean needsReparse = true;
    private static final String CURSOR_MARKER = "$c";
    private static final Map<String, Integer> KEY_NAME_TO_CODE = new HashMap<>();
    private static final Map<String, Integer> BUTTON_NAME_TO_CODE = new HashMap<>();

    static {
        for (Field field : org.lwjgl.glfw.GLFW.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && field.getType() == int.class) {
                String name = field.getName();
                try {
                    if (name.startsWith("GLFW_KEY_")) {
                        KEY_NAME_TO_CODE.put(name.substring("GLFW_KEY_".length()).toLowerCase(), field.getInt(null));
                    } else if (name.startsWith("GLFW_MOUSE_BUTTON_")) {
                        // GLFW_MOUSE_BUTTON_LEFT -> mouse_left
                        // GLFW_MOUSE_BUTTON_1 -> mouse_1
                        String buttonName = name.substring("GLFW_MOUSE_BUTTON_".length());
                        if (buttonName.equals("LAST")) continue;
                        if (buttonName.equals("LEFT")) buttonName = "1";
                        else if (buttonName.equals("RIGHT")) buttonName = "2";
                        else if (buttonName.equals("MIDDLE")) buttonName = "3";
                        // Normalize to "mouse_X" where X is number, or "mouse_left", "mouse_right", etc.
                        BUTTON_NAME_TO_CODE.put("mouse_" + buttonName.toLowerCase(), field.getInt(null));
                    }
                } catch (IllegalAccessException e) {
                    // Log error or handle
                }
            }
        }
        // Add common aliases if users might type them differently
        // e.g. BUTTON_NAME_TO_CODE.put("mouse_left", org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1);
        // Ensure "mouse_1", "mouse_2" etc. are correctly mapped if users use numbers
        if (!BUTTON_NAME_TO_CODE.containsKey("mouse_left")) BUTTON_NAME_TO_CODE.put("mouse_left", org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1);
        if (!BUTTON_NAME_TO_CODE.containsKey("mouse_right")) BUTTON_NAME_TO_CODE.put("mouse_right", org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2);
        if (!BUTTON_NAME_TO_CODE.containsKey("mouse_middle")) BUTTON_NAME_TO_CODE.put("mouse_middle", org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_3);
    }

    private Keybind createKeybindFromString(String name) {
        String lowerName = name.toLowerCase();
        if (KEY_NAME_TO_CODE.containsKey(lowerName)) {
            return Keybind.fromKey(KEY_NAME_TO_CODE.get(lowerName));
        } else if (BUTTON_NAME_TO_CODE.containsKey(lowerName)) {
            return Keybind.fromButton(BUTTON_NAME_TO_CODE.get(lowerName));
        }
        return Keybind.fromKey(-1); // Unbound or unknown
    }

    private static class ParsedMacro {
        final Keybind keybind;
        final String textWithCursor;

        ParsedMacro(Keybind keybind, String textWithCursor) {
            this.keybind = keybind;
            this.textWithCursor = textWithCursor;
        }
    }

    
    
    public BetterMacros() {
        super(LucidAddon.CATEGORY, "Better Macros", "Opens the chat input screen with predefined text and cursor position via keybind.");
    }

    @Override
    public void onActivate() {
        reparseMacros();
        addMacro();
    }
    
    
    @Override
    public void onDeactivate() {
        parsedMacros.clear();
    }

    private void reparseMacros() {
        parsedMacros.clear();
        for (int i = 0; i < macroSettings.size() - 1; i += 2) { // Schrittweite 2, da wir Paare von Text und Keybind haben
            Setting<String> textSetting = (Setting<String>) macroSettings.get(i);
            Setting<Keybind> keybindSetting = (Setting<Keybind>) macroSettings.get(i + 1);
            if (textSetting != null && keybindSetting != null) {
                parsedMacros.add(new ParsedMacro(keybindSetting.get(), textSetting.get()));
            }
        }
        needsReparse = false;    
    }

    private void addMacro(GuiTheme theme) {
        macroCount++;
        String macroName = "Macro " + macroCount;

        // Create settings with default values
        Setting<String> textSetting = sgMacros.add(new StringSetting.Builder()
            .name(macroName + " Text")
            .description("Text for " + macroName + ".")
            .defaultValue("Type your macro text here with $c for cursor position")
            .onChanged(this::onSettingChanged)
            .build()
        );
        Setting<Keybind> keybindSetting = sgMacros.add(new KeybindSetting.Builder()
            .name(macroName + " Keybind")
            .description("Keybind for " + macroName + ".")
            .defaultValue(Keybind.none())
            .onChanged(this::onSettingChanged)
            .build()
        );
        macroSettings.addAll(Arrays.asList(textSetting, keybindSetting));

        WTable table = theme.table();
        WButton removeButton = table.add(theme.button("Remove Macro")).expandX().widget();
        removeButton.action = () -> {
            int index = macroSettings.indexOf(keybindSetting);
            if (index >= 1) {
                macroSettings.remove(index); // Remove keybind
                macroSettings.remove(index - 1); // Remove text
                reparseMacros();
            }
        };
        
    }

    private <T> void onSettingChanged(T value) {
        needsReparse = true;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || !isActive()) return;

        if (needsReparse) {
            reparseMacros();
        }


        if (mc.currentScreen != null) return; // Don't execute if a GUI is open

        for (ParsedMacro pm : parsedMacros) {
            if (pm.keybind.isPressed()) {
                openChatWithMacroLogic(pm.textWithCursor);
            }
        }
    }

    public void openChatWithMacroLogic(String fullText) {
        if (mc.player == null) return;

        String textToInsert = fullText;
        int cursorPos = fullText.indexOf(CURSOR_MARKER);

        if (cursorPos != -1) {
            textToInsert = fullText.replaceFirst(Pattern.quote(CURSOR_MARKER), "");
        } else {
            cursorPos = textToInsert.length(); // Cursor to the end if no marker is present
        }

        String finalText = textToInsert;
        int finalCursorPos = cursorPos;

        mc.execute(() -> {
            mc.setScreen(new ChatScreen(finalText));
            if (mc.currentScreen instanceof ChatScreen chatScreen) {
                try {
                    Field chatFieldField = ChatScreen.class.getDeclaredField("chatField"); // In newer MC versions this might be "chatInput" or similar
                    chatFieldField.setAccessible(true);
                    TextFieldWidget chatWidget = (TextFieldWidget) chatFieldField.get(chatScreen);
                    chatWidget.setCursor(finalCursorPos, false);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    error("Could not set cursor in chat: " + e.getMessage());
                }
            }
        });
    }
    @Override
    public WWidget getWidget(GuiTheme theme) {
        WTable table = theme.table();
        macroSettings.sort(Comparator.comparing(setting -> setting.name));

        for (int i = 0; i < macroSettings.size(); i++) {
            Setting<?> setting = macroSettings.get(i);
            if (i % 2 == 0) { // Textfeld auf der linken Seite
                table.add(theme.label(setting.name)).minWidth(200); // Adjust width as needed
            } else { // Keybind on the right side
                table.add(theme.label(setting.name)).minWidth(100); // Adjust width as needed

                // Add remove button
                WButton removeButton = table.add(theme.button("Remove")).widget();
                removeButton.action = () -> {
                    int index = macroSettings.indexOf(setting);
                    if (index >= 1) {
                        macroSettings.remove(index); // Remove keybind
                        macroSettings.remove(index - 1); // Remove text
                        reparseMacros();
                    }
                };
                table.row();
            }
        }
        WButton addButton = table.add(theme.button("Add Macro")).expandX().widget();
        addButton.action = () -> addMacro();

        return table;
    }

}
