package huhncode.lucid.lucidaddon.modules;

import meteordevelopment.meteorclient.events.entity.player.PlayerDeathEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

public class DeathEventHandler {
    private final CrystalProtect crystalProtect;

    public DeathEventHandler(CrystalProtect crystalProtect) {
        this.crystalProtect = crystalProtect;
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        if (event.player == mc.player) return; // Ignore our own death
        
        // Check if we killed this player
        if (event.player.getAttacker() == mc.player) {
            crystalProtect.onPlayerKill(event.player);
        }
    }
}