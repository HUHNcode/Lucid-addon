package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import huhncode.lucid.lucidaddon.events.PlayerKillEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KillTrackerModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> hitValiditySeconds = sgGeneral.add(new IntSetting.Builder()
        .name("hit-validity-seconds")
        .description("How many seconds after your hit a player's death is considered your kill.")
        .defaultValue(5)
        .min(1)
        .sliderMax(30)
        .build()
    );

    // Stores UUID of the hit player and the timestamp of the hit
    private final Map<UUID, Long> recentHits = new ConcurrentHashMap<>();

    public KillTrackerModule() {
        super(LucidAddon.CATEGORY, "Kill Tracker", "Tracks player hits and triggers PlayerKillEvent on subsequent deaths.");
    }

    @Override
    public void onActivate() {
        recentHits.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        long currentTime = System.currentTimeMillis();
        long validityMillis = hitValiditySeconds.get() * 1000L;

        // Remove old hits
        recentHits.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > validityMillis);
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (mc.player == null || event.entity == null || !(event.entity instanceof PlayerEntity)) {
            return;
        }
        // Ensure the attacker is the local player

        PlayerEntity hitPlayer = (PlayerEntity) event.entity;
        if (hitPlayer.equals(mc.player)) return; // Don't track self-hits

        recentHits.put(hitPlayer.getUuid(), System.currentTimeMillis());
        ChatUtils.info("Tracked hit on: " + hitPlayer.getName().getString());
        
    }

    @EventHandler
    private void onEntityDeathPacket(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null || !(event.packet instanceof EntityStatusS2CPacket packet) || packet.getStatus() != 3) {
            return; // Not a death packet or player/world is null
        }

        Entity entity = packet.getEntity(mc.world);
        if (!(entity instanceof PlayerEntity killedPlayer) || killedPlayer.equals(mc.player)) {
            return; // Not a player death or self-death
        }

        Long lastHitTime = recentHits.get(killedPlayer.getUuid());
        if (lastHitTime != null && (System.currentTimeMillis() - lastHitTime) <= (hitValiditySeconds.get() * 1000L)) {
            // Player was hit by us recently and died.
            MeteorClient.EVENT_BUS.post(new PlayerKillEvent(mc.player, killedPlayer, System.currentTimeMillis(), lastHitTime));
            recentHits.remove(killedPlayer.getUuid()); // Prevent multiple events for the same death
            ChatUtils.info("PlayerKillEvent posted for: " + killedPlayer.getName().getString());
        }
    }
}