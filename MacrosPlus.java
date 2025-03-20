package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.macro.Macro;
import meteordevelopment.meteorclient.systems.modules.macro.Macros;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MacrosPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> macros = sgGeneral.add(new ListSetting.Builder<String>()
        .name("macros")
        .description("List of macros with their keybinds and messages.")
        .defaultValue(new ArrayList<>())
        .build()
    );

    public MacrosPlus() {
        super(LucidAddon.CATEGORY, "Macros+", "Inserts text into the chat box and moves the cursor.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (String macro : macros.get()) {
            String[] parts = macro.split("::", 2);
            if (parts.length < 2) continue;
            String message = parts[1];
            int cursorPos = message.indexOf("$c");
            message = message.replace("$c", "");
            openChatWithMessage(message, cursorPos);
        }
    }

    private void openChatWithMessage(String message, int cursorPos) {
        mc.setScreen(new ChatScreen(message));
        if (mc.currentScreen instanceof ChatScreen chatScreen) {
            try {
                Field chatFieldField = ChatScreen.class.getDeclaredField("chatField");
                chatFieldField.setAccessible(true);
                TextFieldWidget chatField = (TextFieldWidget) chatFieldField.get(chatScreen);
                chatField.setCursor(cursorPos, false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}