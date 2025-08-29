package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static meteordevelopment.meteorclient.gui.renderer.GuiRenderer.EDIT;

public class BetterMacros extends Module {

    public List<Macro> macros = new ArrayList<>();
    private static final String CURSOR_MARKER = "$c";

    public BetterMacros() {
        super(LucidAddon.CATEGORY, "Better Macros", "Opens the chat input screen with predefined text and cursor position via keybind.");
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = super.toTag();
        NbtList macrosTag = new NbtList();
        for (Macro macro : macros) {
            macrosTag.add(macro.toTag());
        }
        tag.put("macros", macrosTag);
        return tag;
    }

    @Override
    public Module fromTag(NbtCompound tag) {
        super.fromTag(tag);
        macros.clear();
        NbtList macrosTag = tag.getList("macros", NbtElement.COMPOUND_TYPE);
        for (NbtElement macroTag : macrosTag) {
            macros.add(Macro.fromTag((NbtCompound) macroTag));
        }
        return this;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        fillWidget(theme, list);
        return list;
    }

    private void fillWidget(GuiTheme theme, WVerticalList list) {
        list.clear();

        WTable table = list.add(theme.table()).expandX().widget();

        for (Macro macro : new ArrayList<>(macros)) {
            String preview = macro.text.length() > 40 ? macro.text.substring(0, 40) + "..." : macro.text;
            table.add(theme.label(preview));

            WKeybind keybindWidget = table.add(theme.keybind(macro.keybind)).widget();
            keybindWidget.action = () -> {
                // opens a mini-popup to capture the key
                mc.setScreen(new WindowScreen(theme, "Press a Key") {
                    @Override
                    public void initWidgets() {
                        add(theme.label("Press any key to set Keybind"));
                    }

                    @Override
                    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                        macro.keybind = Keybind.fromKey(keyCode);
                        mc.setScreen(BetterMacros.this.getWidget(theme)); // back to the macro widget
                        return true;
                    }
                });
            };

            WButton edit = table.add(theme.button(EDIT)).widget();
            edit.action = () -> mc.setScreen(new MacroScreen(theme, macro, () -> fillWidget(theme, list), this));

            WMinus remove = table.add(theme.minus()).widget();
            remove.action = () -> {
                macros.remove(macro);
                fillWidget(theme, list);
            };

            table.row();
        }

        WButton add = list.add(theme.button("Add Macro")).expandX().widget();
        add.action = () -> {
            Macro newMacro = new Macro();
            macros.add(newMacro);
            mc.setScreen(new MacroScreen(theme, newMacro, () -> fillWidget(theme, list), this));
        };
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.currentScreen != null) return;

        for (Macro macro : macros) {
            if (macro.keybind.isPressed()) {
                openChatWithMacroLogic(macro.text);
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
            cursorPos = textToInsert.length();
        }

        String finalText = textToInsert;
        int finalCursorPos = cursorPos;

        mc.execute(() -> {
            mc.setScreen(new ChatScreen(finalText));
            if (mc.currentScreen instanceof ChatScreen chatScreen) {
                try {
                    Field chatFieldField = ChatScreen.class.getDeclaredField("chatField");
                    chatFieldField.setAccessible(true);
                    TextFieldWidget chatWidget = (TextFieldWidget) chatFieldField.get(chatScreen);
                    chatWidget.setCursor(finalCursorPos, false);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    error("Could not set cursor in chat: " + e.getMessage());
                }
            }
        });
    }

    public static class Macro {
        public String text;
        public Keybind keybind;

        public Macro(String text, Keybind keybind) {
            this.text = text;
            this.keybind = keybind;
        }

        public Macro() {
            this("Your text here... use $c for cursor", Keybind.none());
        }

        public NbtCompound toTag() {
            NbtCompound tag = new NbtCompound();
            tag.putString("text", text);
            tag.put("keybind", keybind.toTag());
            return tag;
        }

        public static Macro fromTag(NbtCompound tag) {
            Macro macro = new Macro();
            macro.text = tag.getString("text");
            Keybind keybind = Keybind.none();
            if (tag.contains("keybind")) {
                keybind.fromTag(tag.getCompound("keybind"));
            }
            macro.keybind = keybind;
            return macro;
        }
    }

    public static class MacroScreen extends WindowScreen {
        private final Macro macro;
        private final Runnable onClosed;
        private final BetterMacros parent;

        public MacroScreen(GuiTheme theme, Macro macro, Runnable onClosed, BetterMacros parent) {
            super(theme, "Edit Macro");
            this.macro = macro;
            this.onClosed = onClosed;
            this.parent = parent;
        }

        @Override
        public void initWidgets() {
            WVerticalList list = add(theme.verticalList()).expandX().widget();
            list.minWidth = 400;

            list.add(theme.label("Text:"));
            WTextBox textBox = list.add(theme.textBox(macro.text)).expandX().widget();
            textBox.action = () -> macro.text = textBox.get();

            list.add(theme.label("Keybind:"));
        WKeybind keybindWidget = list.add(theme.keybind(macro.keybind)).expandX().widget();

        // Clicking on the keybind widget opens a popup
        keybindWidget.action = () -> {
            Screen previousScreen = mc.currentScreen; // save the current screen

            mc.setScreen(new WindowScreen(theme, "Press a Key") {
                @Override
                public void initWidgets() {
                    add(theme.label("Press any key to set Keybind"));
                }

                @Override
                public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                    macro.keybind = Keybind.fromKey(keyCode);
                    mc.setScreen(previousScreen); // back to the previous screen
                    return true;
                }
            });
        };

            WButton saveButton = list.add(theme.button("Save")).expandX().widget();
            saveButton.action = () -> {
                parent.macros.remove(macro);
                parent.macros.add(macro);
                parent.fillWidget(theme, parent.list);
                close();
            };
        }

        @Override
        public void close() {
            super.close();
            if (onClosed != null) onClosed.run();
        }
    }
}
