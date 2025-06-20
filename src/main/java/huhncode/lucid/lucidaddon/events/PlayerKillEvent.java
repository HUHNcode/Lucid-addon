package huhncode.lucid.lucidaddon.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource; // Optional: If you ever get DamageSource info

public class PlayerKillEvent {
    public final PlayerEntity killer;
    public final PlayerEntity killed;
    public final long timeOfKill; // Zeitpunkt des Kills
    // Optional: Zeitpunkt des letzten relevanten Schlags, der zum Kill führte
    public final long lastHitTime;  // Optional: Zeitpunkt des letzten relevanten Schlags, der zum Kill führte

    /**
     * Konstruktor für ein PlayerKillEvent.
     *
     * @param killer Der Spieler, der den Kill ausgeführt hat.
     * @param killed Der Spieler, der getötet wurde.
     * @param timeOfKill Der Zeitpunkt (System.currentTimeMillis()), zu dem der Kill stattgefunden hat.
     * @param lastHitTime Der Zeitpunkt (System.currentTimeMillis()) des letzten relevanten Schlags.
     *                    Kann derselbe wie timeOfKill sein oder kurz davor.
     *                    Kann 0 sein, wenn keine explizite Hit-Info für diesen Kill verwendet wurde.
     */
    public PlayerKillEvent(PlayerEntity killer, PlayerEntity killed, long timeOfKill, long lastHitTime) {
        this.killer = killer;
        this.killed = killed;
        this.timeOfKill = timeOfKill;
        this.lastHitTime = lastHitTime;
    }
}
