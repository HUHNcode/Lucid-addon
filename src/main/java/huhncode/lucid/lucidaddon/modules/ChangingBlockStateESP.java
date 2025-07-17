package huhncode.lucid.lucidaddon.modules;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
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
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChangingBlockStateESP extends Module {

    // Innere Klasse, um alle Informationen zu einem Block-Update zu speichern
    private static class BlockUpdateInfo {
        public final BlockPos pos;
        public final String changeText;
        public int timer;

        public BlockUpdateInfo(BlockPos pos, BlockState oldState, BlockState newState, int initialTime) {
            this.pos = pos;
            this.changeText = getChangeString(oldState, newState);
            this.timer = initialTime;
        }

        private String getChangeString(BlockState oldState, BlockState newState) {
            // oldState is now guaranteed to be non-null by the calling method.
            if (oldState.getBlock() != newState.getBlock()) {
                return oldState.getBlock().getName().getString() + " -> " + newState.getBlock().getName().getString();
            } else {
                List<String> changes = new ArrayList<>();
                // Wir vergleichen die Werte für jede Eigenschaft des Blocks.
                for (Property<?> property : oldState.getProperties()) {
                    Comparable<?> oldValue = oldState.get(property);
                    Comparable<?> newValue = newState.get(property);

                    // Objects.equal ist sicher, auch wenn die Werte null sein sollten.
                    if (!Objects.equal(oldValue, newValue)) {
                        changes.add(String.format("%s: %s -> %s", property.getName(), oldValue, newValue));
                    }
                }

                // Wenn es Änderungen gab, geben wir sie im gewünschten Format zurück.
                return changes.isEmpty() ? "" : "[" + String.join(", ", changes) + "]";
            }
        }
    }

    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgChat = settings.createGroup("Chat");
    private final SettingGroup sgFilter = settings.createGroup("Filter");
    // Neue, spezifische Gruppen für die Listen
    private final SettingGroup sgPlacementFilter = settings.createGroup("Placement Filter");
    private final SettingGroup sgBreakingFilter = settings.createGroup("Breaking Filter");
    private final SettingGroup sgStateChangeFilter = settings.createGroup("State Change Filter");

    // Render Settings
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
        .defaultValue(new SettingColor(0, 255, 0, 75))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Die Farbe der Linien der Box.")
        .defaultValue(new SettingColor(0, 255, 0, 255))
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
        .defaultValue(new SettingColor(0, 255, 0, 200))
        .visible(renderTracers::get)
        .build()
    );

    // Chat Settings
    private final Setting<Boolean> logToChat = sgChat.add(new BoolSetting.Builder()
        .name("log-to-chat")
        .description("Gibt die Zustandsänderung im Chat aus.")
        .defaultValue(true)
        .build()
    );

    // Filter Settings
    private final Setting<Boolean> renderPlacements = sgFilter.add(new BoolSetting.Builder()
        .name("render-placements")
        .description("Renders a box when a block is placed (Air -> Block).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderBreakings = sgFilter.add(new BoolSetting.Builder()
        .name("render-breakings")
        .description("Renders a box when a block is broken (Block -> Air).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderStateChanges = sgFilter.add(new BoolSetting.Builder()
        .name("render-state-changes")
        .description("Renders a box when a block's state changes (e.g., lever toggled).")
        .defaultValue(true)
        .build()
    );

    // Enum for list mode
    public enum ListMode {
        Blacklist,
        Whitelist
    }

    // --- Placement Filter Settings ---
    private final Setting<ListMode> placementListMode = sgPlacementFilter.add(new EnumSetting.Builder<ListMode>()
        .name("placement-list-mode")
        .description("Whitelist/Blacklist mode for placed blocks.")
        .defaultValue(ListMode.Blacklist)
        .build()
    );
    private final Setting<List<Block>> placementWhitelist = sgPlacementFilter.add(new BlockListSetting.Builder()
        .name("placement-whitelist")
        .description("Only show placements of these blocks.")
        .visible(() -> placementListMode.get() == ListMode.Whitelist)
        .build()
    );
    private final Setting<List<Block>> placementBlacklist = sgPlacementFilter.add(new BlockListSetting.Builder()
        .name("placement-blacklist")
        .description("Do not show placements of these blocks. Good for liquids.")
        .visible(() -> placementListMode.get() == ListMode.Blacklist)
        .defaultValue(Arrays.asList(
            // Growing Plants & Saplings
            Blocks.SUGAR_CANE, Blocks.CACTUS, Blocks.BAMBOO,
            Blocks.KELP, Blocks.KELP_PLANT, Blocks.SEA_PICKLE, Blocks.SWEET_BERRY_BUSH, Blocks.VINE, Blocks.TWISTING_VINES,
            Blocks.WEEPING_VINES, Blocks.CAVE_VINES, Blocks.CHORUS_FLOWER, Blocks.SPORE_BLOSSOM,
            Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.SMALL_DRIPLEAF,
            // Gravity Blocks
            Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_GRAVEL,
            // Others
            Blocks.BUBBLE_COLUMN, Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.SMALL_AMETHYST_BUD, Blocks.COBBLESTONE,
            // Leaf Types
            Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES,
            Blocks.CHERRY_LEAVES, Blocks.MANGROVE_LEAVES,

            // Void
            Blocks.VOID_AIR, Blocks.CAVE_AIR
        ))
        .build()
    );

    // --- Breaking Filter Settings ---
    private final Setting<ListMode> breakingListMode = sgBreakingFilter.add(new EnumSetting.Builder<ListMode>()
        .name("breaking-list-mode")
        .description("Whitelist/Blacklist mode for broken blocks.")
        .defaultValue(ListMode.Blacklist)
        .build()
    );
    private final Setting<List<Block>> breakingWhitelist = sgBreakingFilter.add(new BlockListSetting.Builder()
        .name("breaking-whitelist")
        .description("Only show breakings of these blocks.")
        .visible(() -> breakingListMode.get() == ListMode.Whitelist)
        .build()
    );
    private final Setting<List<Block>> breakingBlacklist = sgBreakingFilter.add(new BlockListSetting.Builder()
        .name("breaking-blacklist")
        .description("Do not show breakings of these blocks.")
        .visible(() -> breakingListMode.get() == ListMode.Blacklist)
        .defaultValue(Arrays.asList(
            // Gravity Blocks
            Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_GRAVEL,
           
            // Others
            Blocks.LAVA, Blocks.WATER, Blocks.FIRE, Blocks.VINE, Blocks.GLOW_LICHEN, Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT,

            // Leaf Types
            Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES,
            Blocks.CHERRY_LEAVES, Blocks.MANGROVE_LEAVES,

            // Void
            Blocks.VOID_AIR, Blocks.CAVE_AIR


        ))
        .build()
    );

    // --- State Change Filter Settings (was the old general list) ---
    private final Setting<ListMode> stateChangeListMode = sgStateChangeFilter.add(new EnumSetting.Builder<ListMode>()
        .name("state-change-list-mode")
        .description("Whitelist/Blacklist mode for state changes.")
        .defaultValue(ListMode.Blacklist)
        .build()
    );
    private final Setting<List<Block>> stateChangeWhitelist = sgStateChangeFilter.add(new BlockListSetting.Builder()
        .name("state-change-whitelist")
        .description("Only show state changes for blocks in this list.")
        .visible(() -> stateChangeListMode.get() == ListMode.Whitelist)
        .build()
    );
    private final Setting<List<Block>> stateChangeBlacklist = sgStateChangeFilter.add(new BlockListSetting.Builder()
        .name("state-change-blacklist")
        .description("Do not show state changes for blocks in this list.")
        .visible(() -> stateChangeListMode.get() == ListMode.Blacklist)
        .defaultValue(Arrays.asList(
            // Leaf Types
            Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES,
            Blocks.CHERRY_LEAVES, Blocks.MANGROVE_LEAVES,
            // Growing Plants
            Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS, Blocks.NETHER_WART,
            Blocks.MELON_STEM, Blocks.PUMPKIN_STEM, Blocks.SUGAR_CANE, Blocks.CACTUS, Blocks.BAMBOO,
            Blocks.KELP, Blocks.KELP_PLANT, Blocks.SEA_PICKLE,          
            Blocks.SWEET_BERRY_BUSH, Blocks.VINE, Blocks.TWISTING_VINES,
            Blocks.WEEPING_VINES, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.CHORUS_FLOWER, Blocks.CHORUS_PLANT, Blocks.SPORE_BLOSSOM,
            Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.SMALL_DRIPLEAF, Blocks.MANGROVE_PROPAGULE, Blocks.PITCHER_CROP,
            Blocks.TORCHFLOWER_CROP, Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING,
            Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.CHERRY_SAPLING, Blocks.COCOA,
            // Amethyst
            Blocks.BUDDING_AMETHYST, Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD, Blocks.AMETHYST_CLUSTER,
            // Sculk
            Blocks.SCULK_SENSOR, Blocks.CALIBRATED_SCULK_SENSOR,
            // Others
            Blocks.FIRE, Blocks.LAVA, Blocks.WATER, Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.BUBBLE_COLUMN, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.REDSTONE_ORE, Blocks.COBBLESTONE, Blocks.OBSIDIAN, Blocks.STONE,

            // Void
            Blocks.VOID_AIR, Blocks.CAVE_AIR,
            // Gravity Blocks
            Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_GRAVEL
        ))
        .build()
    );

    // Interner Cache, um den Zustand von Blöcken zu speichern und Änderungen zuverlässig zu erkennen.
    // Dies ist der robusteste Weg, um den "alten" Zustand eines Blocks zu kennen.
    private final List<BlockUpdateInfo> updatedBlocks = new ArrayList<>();

    public ChangingBlockStateESP() {
        super(LucidAddon.CATEGORY, "Block Update ESP", "Renders a box around block changes from the server.");
    }

    @Override
    public void onActivate() {
        // Clear the list of blocks to render to start fresh.
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

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null) return;

        // Einzelne Block-Änderung
        if (event.packet instanceof BlockUpdateS2CPacket packet) {
            // By listening with high priority, we get the state *before* the game processes the packet.
            BlockState oldState = mc.world.getBlockState(packet.getPos());
            handleBlockChange(packet.getPos(), oldState, packet.getState());
        }
        // Mehrere Block-Änderungen in einem Chunk
        else if (event.packet instanceof ChunkDeltaUpdateS2CPacket packet) {
            packet.visitUpdates((pos, newState) -> {
                BlockState oldState = mc.world.getBlockState(pos);
                handleBlockChange(pos, oldState, newState);
            });
        }
    }
    
    private void handleBlockChange(BlockPos pos, BlockState oldState, BlockState newState) {
        // If the state hasn't actually changed, do nothing.
        if (oldState.equals(newState)) {
            return;
        }

        BlockPos immutablePos = pos.toImmutable();

        // --- From here on, we have a confirmed, real change. ---

        // Filter logic for placements, breakings, and state changes.
        boolean isPlacement = oldState.isAir() && !newState.isAir();
        boolean isBreaking = !oldState.isAir() && newState.isAir();
        boolean isStateChange = !isPlacement && !isBreaking;

        // Apply list filters based on change type
        if (isPlacement) {
            Block blockToCheck = newState.getBlock();
            if (placementListMode.get() == ListMode.Blacklist) {
                if (placementBlacklist.get().contains(blockToCheck)) return;
            } else { // Whitelist
                if (!placementWhitelist.get().contains(blockToCheck)) return;
            }
        } else if (isBreaking) {
            Block blockToCheck = oldState.getBlock();
            if (breakingListMode.get() == ListMode.Blacklist) {
                if (breakingBlacklist.get().contains(blockToCheck)) return;
            } else { // Whitelist
                if (!breakingWhitelist.get().contains(blockToCheck)) return;
            }
        } else { // isStateChange
            Block blockToCheck = newState.getBlock();
            if (stateChangeListMode.get() == ListMode.Blacklist) {
                if (stateChangeBlacklist.get().contains(blockToCheck)) return;
            } else { // Whitelist
                if (!stateChangeWhitelist.get().contains(blockToCheck)) return;
            }
        }

        // Apply the general on/off filters from settings
        if ((isPlacement && !renderPlacements.get()) || (isBreaking && !renderBreakings.get()) || (isStateChange && !renderStateChanges.get())) {
            return;
        }

        // A valid change was detected.
        final BlockUpdateInfo info = new BlockUpdateInfo(immutablePos, oldState, newState, renderTime.get());
        if (info.changeText == null || info.changeText.isEmpty()) return;

        // Execute rendering and chat logic on the main thread to prevent crashes.
        mc.execute(() -> {
            synchronized (updatedBlocks) {
                updatedBlocks.add(info);
            }
            if (logToChat.get()) {
                // oldState is guaranteed not to be null here.
                if (oldState.getBlock() != newState.getBlock()) {
                    ChatUtils.info("Block changed at (%d, %d, %d): %s", immutablePos.getX(), immutablePos.getY(), immutablePos.getZ(), info.changeText);
                } else {
                    ChatUtils.info("%s (%d, %d, %d) %s", newState.getBlock().getName().getString(), immutablePos.getX(), immutablePos.getY(), immutablePos.getZ(), info.changeText);
                }
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
            for (BlockUpdateInfo info : updatedBlocks) {
                // Render the box around the block
                event.renderer.box(new Box(info.pos), sideColor.get(), lineColor.get(), shapeMode.get(), 0);

                // Render the tracer line if enabled
                if (renderTracers.get()) {
                    event.renderer.line(
                        RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
                        info.pos.getX() + 0.5,
                        info.pos.getY() + 0.5,
                        info.pos.getZ() + 0.5,
                        tracerColor.get()
                    );
                }
            }
        }
    }
}