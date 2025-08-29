package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NBTStream extends Module {

    private static class NBTUpdateInfo {
        public final BlockPos pos;
        public final String changeText;
        public int timer;

        public NBTUpdateInfo(BlockPos pos, NbtCompound oldNbt, NbtCompound newNbt, int initialTime) {
            this.pos = pos;
            this.changeText = getNbtChangeString(oldNbt, newNbt);
            this.timer = initialTime;
        }

        private String getNbtChangeString(NbtCompound oldNbt, NbtCompound newNbt) {
            if (oldNbt == null) {
                return "NBT initialized: " + newNbt;
            } else if (newNbt == null) {
                return "NBT removed"; // Sollte eigentlich nicht passieren, aber zur Sicherheit
            } else {
                List<String> changes = new ArrayList<>();
                for (String key : newNbt.getKeys()) {
                    if (!Objects.equals(oldNbt.get(key), newNbt.get(key))) {
                        changes.add(String.format("%s: %s -> %s", key, oldNbt.get(key), newNbt.get(key)));
                    }
                }
                for (String key : oldNbt.getKeys()) {
                    if (!newNbt.contains(key)) {
                        changes.add(key + ": removed");
                    }
                }
                return changes.isEmpty() ? "NBT updated" : "[" + String.join(", ", changes) + "]";
            }
        }
    }

    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgChat = settings.createGroup("Chat");
    private final SettingGroup sgFilter = settings.createGroup("Filter");

    // Render Settings (identisch zu ChangingBlockStateESP)
    private final Setting<Integer> renderTime = sgRender.add(new IntSetting.Builder()
        .name("render-time")
        .description("Wie lange die Box in Ticks gerendert werden soll.")
        .defaultValue(40)
        .min(1)
        .sliderMax(200)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("Wie die Box gerendert werden soll.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Die Farbe der Seiten der Box.")
        .defaultValue(new SettingColor(255, 255, 0, 75)) // Gelb als Standard
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Die Farbe der Linien der Box.")
        .defaultValue(new SettingColor(255, 255, 0, 255))
        .build()
    );

    private final Setting<Boolean> renderTracers = sgRender.add(new BoolSetting.Builder()
        .name("render-tracers")
        .description("Renders a line from your screen to the block change.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgRender.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("The color of the tracer line.")
        .defaultValue(new SettingColor(255, 255, 0, 200))
        .visible(renderTracers::get)
        .build()
    );

    // Chat Settings (identisch zu ChangingBlockStateESP)
    private final Setting<Boolean> logToChat = sgChat.add(new BoolSetting.Builder()
        .name("log-to-chat")
        .description("Gibt die NBT-Änderung im Chat aus.")
        .defaultValue(true)
        .build()
    );

    // Filter Settings
    private final Setting<Integer> ignoreRange = sgFilter.add(new IntSetting.Builder()
        .name("ignore-range")
        .description("Does not log or render NBT changes within this distance to you. 0 to disable.")
        .defaultValue(8)
        .min(0)
        .sliderMax(32)
        .build()
    );

    // Blacklist
    private final Setting<List<Block>> blacklist = sgFilter.add(new BlockListSetting.Builder()
        .name("blacklist")
        .description("Do not show NBT changes for blocks in this list.")
        .defaultValue(Arrays.asList(
            // Füge hier Standard-Blacklist-Einträge hinzu, falls gewünscht.
        ))
        .build()
    );

    private final List<NBTUpdateInfo> updatedBlocks = new ArrayList<>();

    public NBTStream() {
        super(LucidAddon.CATEGORY, "NBT Stream", "Renders and logs NBT changes of blocks.");
    }

    @Override
    public void onActivate() {
        synchronized (updatedBlocks) {
            updatedBlocks.clear();
        }
    }

    @Override
    public void onDeactivate() {
        synchronized (updatedBlocks) {
            updatedBlocks.clear();
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof BlockEntityUpdateS2CPacket packet) || mc.world == null || mc.player == null) return;

        BlockPos pos = packet.getPos();
        Block block = mc.world.getBlockState(pos).getBlock();

        if (blacklist.get().contains(block)) return;

        int range = ignoreRange.get();
        if (range > 0 && pos.getSquaredDistance(mc.player.getBlockPos()) <= (double) range * range) return;

        NbtCompound oldNbt = mc.world.getBlockEntity(pos) != null ? mc.world.getBlockEntity(pos).createNbtWithIdentifyingData(mc.world.getRegistryManager()) : null;
        NbtCompound newNbt = packet.getNbt();
        

        NBTUpdateInfo info = new NBTUpdateInfo(pos.toImmutable(), oldNbt, newNbt, renderTime.get());
        if (info.changeText == null || info.changeText.isEmpty()) return;

        mc.execute(() -> {
            synchronized (updatedBlocks) {
                updatedBlocks.add(info);
            }
            if (logToChat.get()) {
                ChatUtils.info("NBT changed at (%d, %d, %d): %s", info.pos.getX(), info.pos.getY(), info.pos.getZ(), info.changeText);
            }
        });
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        synchronized (updatedBlocks) {
            updatedBlocks.removeIf(info -> {
                info.timer--;
                return info.timer <= 0;
            });
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        synchronized (updatedBlocks) {
            for (NBTUpdateInfo info : updatedBlocks) {
                event.renderer.box(new Box(info.pos), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                if (renderTracers.get()) {
                    event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
                        info.pos.getX() + 0.5, info.pos.getY() + 0.5, info.pos.getZ() + 0.5, tracerColor.get());
                }
            }
        }
    }
}