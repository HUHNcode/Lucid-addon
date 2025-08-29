package huhncode.lucid.lucidaddon.services;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;

public class CrystalBreakTracker {
    private final Queue<Long> breakTimes = new LinkedList<>();

    public int getCps() {
        return breakTimes.size();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        long now = System.currentTimeMillis();
        breakTimes.removeIf(time -> time < now - 1000);
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (MeteorClient.mc.world != null && event.packet instanceof PlayerInteractEntityC2SPacket packet) {
            try {
                Field field = PlayerInteractEntityC2SPacket.class.getDeclaredField("entityId");
                field.setAccessible(true);
                int entityId = (int) field.get(packet);

                Entity entity = MeteorClient.mc.world.getEntityById(entityId);
                if (entity instanceof EndCrystalEntity) {
                    breakTimes.add(System.currentTimeMillis());
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}