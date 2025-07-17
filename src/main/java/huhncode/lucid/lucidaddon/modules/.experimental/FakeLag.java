package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

public class FakeLag extends Module {

    public enum Mode {
        CONSTANT,
        DYNAMIC
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFlush = settings.createGroup("Flush Conditions");

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("When to lag.")
        .defaultValue(Mode.DYNAMIC)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The range to check for enemies in dynamic mode.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(10)
        .visible(() -> mode.get() == Mode.DYNAMIC)
        .build()
    );

    private final Setting<Integer> minDelay = sgGeneral.add(new IntSetting.Builder()
        .name("min-delay")
        .description("The minimum delay in milliseconds to hold packets.")
        .defaultValue(300)
        .min(0)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Integer> maxDelay = sgGeneral.add(new IntSetting.Builder()
        .name("max-delay")
        .description("The maximum delay in milliseconds to hold packets.")
        .defaultValue(600)
        .min(0)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Integer> recoilTime = sgGeneral.add(new IntSetting.Builder()
        .name("recoil-time")
        .description("How long to wait in milliseconds after flushing before lagging again.")
        .defaultValue(250)
        .min(0)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Boolean> flushOnEntityInteract = sgFlush.add(new BoolSetting.Builder()
        .name("flush-on-entity-interact")
        .description("Flushes packets when you attack or interact with an entity.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> flushOnBlockInteract = sgFlush.add(new BoolSetting.Builder()
        .name("flush-on-block-interact")
        .description("Flushes packets when you interact with a block.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> flushOnAction = sgFlush.add(new BoolSetting.Builder()
        .name("flush-on-action")
        .description("Flushes packets on player actions like starting to sprint or sneak.")
        .defaultValue(true)
        .build()
    );

    private final Queue<Packet<?>> packetQueue = new ConcurrentLinkedQueue<>();
    private Vec3d serverSidePos;

    private long recoilUntil = 0;
    private long lagStartedTime = 0;
    private int currentDelay = 0;
    private boolean isEnemyNearby = false;

    public FakeLag() {
        super(LucidAddon.CATEGORY, "fake-lag", "Holds back packets to make you harder to hit.");
    }

    @Override
    public void onActivate() {
        reset();
    }

    @Override
    public void onDeactivate() {
        flush();
    }

    private void reset() {
        packetQueue.clear();
        recoilUntil = 0;
        lagStartedTime = 0;
        currentDelay = 0;
        if (mc.player != null) {
            serverSidePos = mc.player.getPos();
        } else {
            serverSidePos = Vec3d.ZERO;
        }
    }

    private void flush() {
        if (mc.getNetworkHandler() == null) {
            packetQueue.clear();
            return;
        }
        synchronized (packetQueue) {
            while (!packetQueue.isEmpty()) {
                mc.getNetworkHandler().sendPacket(packetQueue.poll());
            }
        }
        if (mc.player != null) {
            serverSidePos = mc.player.getPos();
        }
        recoilUntil = System.currentTimeMillis() + recoilTime.get();
        lagStartedTime = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mode.get() == Mode.DYNAMIC) {
            isEnemyNearby = findEnemy();
        }

        if (lagStartedTime > 0 && System.currentTimeMillis() - lagStartedTime > currentDelay) {
            flush();
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;

        if (event.packet instanceof HealthUpdateS2CPacket packet && packet.getHealth() < mc.player.getHealth()) {
            flush();
            return;
        }

        if (event.packet instanceof EntityVelocityUpdateS2CPacket packet && packet.getId() == mc.player.getId()) {
            if (packet.getVelocity().lengthSquared() > 0.01) {
                flush();
                return;
            }
        }
        if (event.packet instanceof ExplosionS2CPacket) {
            flush();
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.player.isDead() || mc.player.isTouchingWater() || mc.currentScreen != null) {
            if (!packetQueue.isEmpty()) flush();
            return;
        }

        if (System.currentTimeMillis() < recoilUntil) return;

        if (mc.player.isUsingItem()) {
            var item = mc.player.getActiveItem().getItem();
            if (item.isFood() || item instanceof PotionItem || item instanceof BowItem || item instanceof CrossbowItem) {
                if (!packetQueue.isEmpty()) flush();
                return;
            }
        }

        if (flushOnEntityInteract.get() && (event.packet instanceof PlayerInteractEntityC2SPacket || event.packet instanceof HandSwingC2SPacket)) {
            if (!packetQueue.isEmpty()) flush();
            return;
        }
        if (flushOnBlockInteract.get() && (event.packet instanceof PlayerInteractBlockC2SPacket || event.packet instanceof UpdateSignC2SPacket)) {
            if (!packetQueue.isEmpty()) flush();
            return;
        }
        if (flushOnAction.get() && event.packet instanceof PlayerActionC2SPacket) {
            if (!packetQueue.isEmpty()) flush();
            return;
        }

        if (!(event.packet instanceof PlayerMoveC2SPacket) && !(event.packet instanceof KeepAliveC2SPacket)) {
            return;
        }

        if (mode.get() == Mode.DYNAMIC) {
            if (!isEnemyNearby) {
                if (!packetQueue.isEmpty()) flush();
                return;
            }

            Box playerBoxAtServerPos = mc.player.getBoundingBox().offset(serverSidePos.subtract(mc.player.getPos()));
            boolean shouldNotLag = false;
            double closestClientDistSq = Double.MAX_VALUE;
            double closestServerDistSq = Double.MAX_VALUE;

            for (PlayerEntity p : mc.world.getPlayers()) {
                if (!shouldAttack(p)) continue;

                if (p.getBoundingBox().intersects(playerBoxAtServerPos)) {
                    shouldNotLag = true;
                    break;
                }

                double clientDistSq = mc.player.getPos().squaredDistanceTo(p.getPos());
                double serverDistSq = serverSidePos.squaredDistanceTo(p.getPos());

                if (clientDistSq < closestClientDistSq) closestClientDistSq = clientDistSq;
                if (serverDistSq < closestServerDistSq) closestServerDistSq = serverDistSq;
            }

            if (shouldNotLag || closestServerDistSq < closestClientDistSq) {
                if (!packetQueue.isEmpty()) flush();
                return;
            }
        }

        queuePacket(event.packet);
        event.cancel();
    }

    private void queuePacket(Packet<?> packet) {
        synchronized (packetQueue) {
            packetQueue.add(packet);
        }

        if (packet instanceof PlayerMoveC2SPacket p) {
            serverSidePos = new Vec3d(p.getX(serverSidePos.x), p.getY(serverSidePos.y), p.getZ(serverSidePos.z));
        }

        if (lagStartedTime == 0) {
            lagStartedTime = System.currentTimeMillis();
            currentDelay = ThreadLocalRandom.current().nextInt(minDelay.get(), maxDelay.get() + 1);
        }
    }

    private boolean findEnemy() {
        if (mc.world == null || mc.player == null) return false;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (shouldAttack(player)) return true;
        }
        return false;
    }

    private boolean shouldAttack(PlayerEntity p) {
        if (p == mc.player || p.isDead() || !Friends.get().shouldAttack(p) || p.isCreative()) return false;
        return mc.player.distanceTo(p) <= range.get();
    }
}