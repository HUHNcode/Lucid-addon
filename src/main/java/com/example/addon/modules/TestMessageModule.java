package com.example.addon.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.ArrayList;
import java.util.List;

public class TestMessageModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTriggers = settings.createGroup("Triggers");

    // Delay in Sekunden (Standard 0 = kein Delay, zum Testen)
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Verzögerung (in Sekunden) zwischen Trigger-Erkennung und Senden des Outputs (0 = sofort).")
        .defaultValue(0)
        .min(0)
        .sliderMax(60)
        .build()
    );

    // Listen für Trigger und zugehörige Outputs
    private final Setting<List<String>> triggers = sgTriggers.add(new StringListSetting.Builder()
        .name("triggers")
        .description("Liste von Trigger-Strings, die in Chat-Nachrichten enthalten sein müssen.")
        .defaultValue(new ArrayList<>())
        .build()
    );

    private final Setting<List<String>> outputs = sgTriggers.add(new StringListSetting.Builder()
        .name("outputs")
        .description("Liste von Outputs, die gesendet werden, wenn der zugehörige Trigger erkannt wird.")
        .defaultValue(new ArrayList<>())
        .build()
    );

    public TestMessageModule() {
        super(AddonTemplate.CATEGORY, "TestMessage", "Sendet eine Nachricht, wenn ein Chat-Trigger erkannt wird.");
    }

    @Override
    public void onActivate() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void onDeactivate() {
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = (GameMessageS2CPacket) event.packet;
            String message = packet.content().getString();
            checkTriggers(message);
        }
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        checkTriggers(event.message);
    }

    // Prüft, ob die empfangene Nachricht einen der Trigger enthält und löst ggf. den Output aus.
    private void checkTriggers(String message) {
        List<String> triggerList = triggers.get();
        List<String> outputList = outputs.get();

        for (int i = 0; i < triggerList.size(); i++) {
            String trigger = triggerList.get(i);
            if (trigger != null && !trigger.isEmpty() && message.contains(trigger)) {
                if (i < outputList.size()) {
                    String out = outputList.get(i);
                    if (out != null && !out.isEmpty()) {
                        scheduleOutput(out);
                    }
                }
            }
        }
    }

    // Sendet den Output – entweder sofort (Delay = 0) oder nach der konfigurierten Verzögerung.
    private void scheduleOutput(String out) {
        int delaySeconds = delay.get();
        if (delaySeconds <= 0) {
            ChatUtils.sendPlayerMsg(out);
        } else {
            MeteorClient.mc.execute(() -> {
                try {
                    Thread.sleep(delaySeconds * 1000L);
                } catch (InterruptedException ignored) {}
                ChatUtils.sendPlayerMsg(out);
            });
        }
    }

    // Dynamisches Hinzufügen/Entfernen von Trigger-/Output-Paaren (bei Bedarf programmatisch)
    public void addTriggerPair(String trigger, String output) {
        triggers.get().add(trigger);
        outputs.get().add(output);
        settings.save();
    }

    public void removeTriggerPair(int index) {
        if (index >= 0 && index < triggers.get().size() && index < outputs.get().size()) {
            triggers.get().remove(index);
            outputs.get().remove(index);
            settings.save();
        }
    }
}
