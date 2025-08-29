package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent; // The following imports are no longer needed as kill detection is outsourced.
// import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
// import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import huhncode.lucid.lucidaddon.events.PlayerKillEvent; // Import PlayerKillEvent
// import net.minecraft.entity.Entity;
// import net.minecraft.entity.player.PlayerEntity;
// import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.List;
import java.util.Random;
// import java.util.Map;
// import java.util.UUID;
// import java.util.concurrent.ConcurrentHashMap;

public class AutoGG extends Module {
    public AutoGG() {
        super(LucidAddon.CATEGORY, "Auto GG+", "Sends a GG message after a kill detected by KillDetectionService.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // The killWindowSeconds setting is no longer needed as this is handled by the KillDetectionService.
    // private final Setting<Integer> killWindowSeconds = sgGeneral.add(new IntSetting.Builder()
    //     .name("kill-window-seconds")
    //     .description("Time window in seconds after hitting a player to consider their death as your kill.")
    //     .defaultValue(5)
    //     .min(1)
    //     .sliderMax(30)
    //     .build()
    // );

    private final Setting<Integer> tickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("How many ticks to wait before sending the GG message.")
        .defaultValue(50)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );

        private final Setting<Integer> hitValiditySeconds = sgGeneral.add(new IntSetting.Builder()
        .name("hit-validity-seconds")
        .description("The number of seconds within which a player's death is attributed to you after hitting them. This setting determines the time window for your hit to be considered as a kill.")
        .defaultValue(5)
        .min(1)
        .sliderMax(30) // Corrected: changed .withListener to .onChanged
        .onChanged(this::onSettingChange)
        .build()
    );

    private final Setting<Boolean> randomMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("Random Message")
        .description("Choose a random message from the list.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> privateMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("Private Message")
        .description("Send the GG message as a private /msg to the killed player.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<String>> ggMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("GG Messages")
        .description("Messages to send after a kill.")
        .defaultValue(List.of("GG!", "Good fight!", "Well played!", "Nice one!"))
        .build()
    );

    private final Random random = new Random();
    private int timer = 0;
    private boolean shouldSend = false;
    private int messageIndex = 0;
    private String lastKilledPlayer = "";
    // The recentHits map is no longer needed as this is handled by the KillDetectionService.
    // private final Map<UUID, Long> recentHits = new ConcurrentHashMap<>();

    @Override
    public void onActivate() {
        super.onActivate();
        timer = 0;
        shouldSend = false;
        messageIndex = 0;
        // recentHits.clear(); // No longer needed
        onSettingChange(hitValiditySeconds.get());
    }

    
    private void onSettingChange(Integer newValue) {
        if (LucidAddon.killDetectionService != null) {
            LucidAddon.killDetectionService.setHitValiditySeconds(newValue);
        }
    }
    

    // Listen for the PlayerKillEvent, which is triggered by the KillDetectionService
    @EventHandler
    private void onPlayerKill(PlayerKillEvent event) {
        // Check if the local player is the killer
        if (mc.player != null && event.killer.equals(mc.player)) {
            // ChatUtils.info("[AutoGG] PlayerKillEvent received for: " + event.killed.getName().getString()); // Optional: for debugging
            triggerGG(event.killed.getName().getString());
        }
    }

    // The following event handlers for direct kill detection are no longer needed:
    // @EventHandler
    // private void onAttackEntity(AttackEntityEvent event) { ... }
    // @EventHandler
    // private void onEntityDeathPacket(PacketEvent.Receive event) { ... }

    public void triggerGG(String playerName) {
        shouldSend = true;
        timer = 0;
        lastKilledPlayer = playerName;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // The logic for cleaning up the recentHits map is no longer here, but in the KillDetectionService
        // if (mc.player != null) { ... }

        if (!shouldSend) return;

        timer++;
        if (timer >= tickDelay.get()) {
            sendGGMessage();
            shouldSend = false;
        }
    }

    private void sendGGMessage() {
        if (ggMessages.get().isEmpty()) return;

        String message;
        if (randomMessage.get()) {
            message = ggMessages.get().get(random.nextInt(ggMessages.get().size()));
        } else {
            message = ggMessages.get().get(messageIndex);
            messageIndex = (messageIndex + 1) % ggMessages.get().size();
        }

        if (privateMessage.get() && !lastKilledPlayer.isEmpty()) {
            ChatUtils.sendPlayerMsg("/msg " + lastKilledPlayer + " " + message);
        } else {
            ChatUtils.sendPlayerMsg(message);
        }
    }
}
