package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;


public class My_test extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // Default group for settings

    public My_test() {
        super(AddonTemplate.CATEGORY, "ChatListener", "Hört auf Chat-Nachrichten von anderen Spielern.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPacketReceive(PacketEvent.Receive event) {
        // Überprüfen, ob das Paket ein ChatMessageS2CPacket ist
        if (event.packet instanceof ChatMessageS2CPacket) {
            ChatMessageS2CPacket packet = (ChatMessageS2CPacket) event.packet;
            
            // Zugriff auf die Nachricht und den Absender
            String message = packet.body().toString();  // Zugriff auf den Body (Text der Nachricht)
            String sender = packet.sender().toString();  // Zugriff auf den Sender (UUID)

            // Nachricht im Chat anzeigen
            ChatUtils.info(sender + ": " + message);
            System.out.println(sender + ": " + message);
            
        }
    }
}
