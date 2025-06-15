package huhncode.lucid.lucidaddon.modules;

import meteordevelopment.meteorclient.events.entity.player.PlayerDeathEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

public class DeathEventHandler {
    private final AntiItemDestroy AntiItemDestroy;

    public DeathEventHandler(AntiItemDestroy AntiItemDestroy) {
        this.AntiItemDestroy = AntiItemDestroy;
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        if (event.player == mc.player) return; // Ignore our own death
        
        // Check if we killed this player
        if (event.player.getAttacker() == mc.player) {
            AntiItemDestroy.onPlayerKill(event.player);
        }
    }
}