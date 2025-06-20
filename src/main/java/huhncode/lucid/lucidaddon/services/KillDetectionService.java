package huhncode.lucid.lucidaddon.services;

import huhncode.lucid.lucidaddon.events.PlayerKillEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.SentMessage.Chat;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KillDetectionService {
    // Configuration directly here or via another config solution
    private int hitValiditySeconds = 5; // Default to 5 seconds, now can be changed

    private final Map<UUID, Long> recentHits = new ConcurrentHashMap<>();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public KillDetectionService() {
        // Constructor can remain empty or be used for initializations
        // ChatUtils.info("KillDetectionService initialized."); // Optional: for debugging
    }

    // Public method to set the hit validity duration
    public void setHitValiditySeconds(int seconds) {
        this.hitValiditySeconds = seconds;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) {
            // If the player is not in the world, we could clear the map
            // to remove old entries if the player leaves and rejoins the world.
            if (!recentHits.isEmpty()) recentHits.clear();
            return;
        }
        long currentTime = System.currentTimeMillis();
        long validityMillis = hitValiditySeconds * 1000L;

        // Remove old hits
        recentHits.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > validityMillis);
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        // mc.player is the attacker in AttackEntityEvent
        if (mc.player == null || event.entity == null || !(event.entity instanceof PlayerEntity)) {
            return;
        }
        ChatUtils.info("[KDS] Player attacked: " + event.entity.getName().getString()); // Optional: for debugging

        PlayerEntity hitPlayer = (PlayerEntity) event.entity;
        if (hitPlayer.equals(mc.player)) return; // Don't track self-hits

        recentHits.put(hitPlayer.getUuid(), System.currentTimeMillis());
        // ChatUtils.info("[KDS] Tracked hit on: " + hitPlayer.getName().getString()); // Optional: for debugging
    }

    @EventHandler
    private void onEntityDeathPacket(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null || !(event.packet instanceof EntityStatusS2CPacket packet) || packet.getStatus() != 3) {
            return; // Not a death packet or player/world is null
        }
        ChatUtils.info("[KDS] Player died: " + packet.getEntity(mc.world).getName().getString() + ""); // Optional: for debugging
        Entity entity = packet.getEntity(mc.world);
        if (!(entity instanceof PlayerEntity killedPlayer) || killedPlayer.equals(mc.player)) {
            return; // Not a player death or self-death
        }

        Long lastHitTime = recentHits.get(killedPlayer.getUuid());
        if (lastHitTime != null && (System.currentTimeMillis() - lastHitTime) <= (hitValiditySeconds * 1000L)) {
            // Player was hit by us recently and died.
            MeteorClient.EVENT_BUS.post(new PlayerKillEvent(mc.player, killedPlayer, System.currentTimeMillis(), lastHitTime));
            recentHits.remove(killedPlayer.getUuid()); // Prevent multiple events for the same death
            ChatUtils.info("[KDS] PlayerKillEvent posted for: " + killedPlayer.getName().getString()); // Optional: for debugging
        }
    }
}