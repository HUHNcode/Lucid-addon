package com.example.addon.modules;
import com.example.addon.AddonTemplate;

import java.io.FileWriter;
import java.io.IOException;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

public class My_test extends Module {
    public My_test() {
        super(AddonTemplate.CATEGORY, "My Test", "Eine Testbeschreibung.");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (event.getMessage() != null) {
            String message = event.getMessage().getString();

            // Verhindere Endlosschleife
            if (!message.contains("Nachricht erhalten:")) {
                System.out.println("Nachricht erhalten: " + message);
                ChatUtils.info("Nachricht erhalten: " + message);
                if (message.contains("Test")) {
                    try {
                        FileWriter writer = new FileWriter("/home/timon/Downloads/Meteor-Client-addon/src/main/java/com/example/addon/modules/test.txt", true);
                        writer.write("Nachricht erhalten: " + message + "\n");
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}









