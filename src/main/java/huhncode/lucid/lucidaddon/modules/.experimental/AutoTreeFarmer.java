package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoTreeFarmer extends Module {

    private enum Stage {
        FINDING_SPOT,
        PLACING_SAPLINGS,
        BONE_MEALING,
        CHOPPING,
        WAITING,
        DONE
    }

    // New Enum for Tree Types
    public enum TreeType {
        DARK_OAK("Dark Oak", Items.DARK_OAK_SAPLING, Blocks.DARK_OAK_LOG, 8), // Dark Oak is usually 5-8 blocks tall
        SPRUCE("Spruce", Items.SPRUCE_SAPLING, Blocks.SPRUCE_LOG, 20); // Spruce can be very tall, up to 30 blocks, 20 is a safe bet

        private final String name;
        private final net.minecraft.item.Item saplingItem;
        private final Block logBlock;
        private final int minHeight;

        TreeType(String name, net.minecraft.item.Item saplingItem, Block logBlock, int minHeight) {
            this.name = name;
            this.saplingItem = saplingItem;
            this.logBlock = logBlock;
            this.minHeight = minHeight;
        }

        public net.minecraft.item.Item getSaplingItem() {
            return saplingItem;
        }

        public Block getLogBlock() {
            return logBlock;
        }

        public int getMinHeight() {
            return minHeight;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<TreeType> treeType = sgGeneral.add(new EnumSetting.Builder<TreeType>()
        .name("tree-type")
        .description("The type of 2x2 tree to farm.")
        .defaultValue(TreeType.DARK_OAK)
        .build()
    );

    private final Setting<Integer> searchRadius = sgGeneral.add(new IntSetting.Builder()
        .name("search-radius")
        .description("The radius to search for a suitable spot to plant.")
        .defaultValue(5)
        .min(3)
        .sliderMax(10)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("action-delay-ticks")
        .description("The delay in ticks between actions.")
        .defaultValue(5)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> autoRestart = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-restart")
        .description("Automatically starts the process again after finishing.")
        .defaultValue(true)
        .build()
    );

    // Offsets for the 2x2 planting pattern
    private static final BlockPos[] SAPLING_OFFSETS = {
        new BlockPos(0, 0, 0),
        new BlockPos(1, 0, 0),
        new BlockPos(0, 0, 1),
        new BlockPos(1, 0, 1)
    };

    private Stage stage;
    private BlockPos plantPos;
    private int saplingsPlaced;
    private int timer;

    public AutoTreeFarmer() {
        super(LucidAddon.CATEGORY, "Auto Tree Farmer", "Automatically plants, grows, and chops down 2x2 trees.");
    }

    @Override
    public void onActivate() {
        reset();
    }

    private void reset() {
        stage = Stage.FINDING_SPOT;
        plantPos = null;
        saplingsPlaced = 0;
        timer = 0;
        info("Looking for a suitable spot...");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (timer > 0) {
            timer--;
            return;
        }

        switch (stage) {
            case FINDING_SPOT:
                findSpot();
                break;

            case PLACING_SAPLINGS:
                placeSaplings();
                break;

            case BONE_MEALING:
                boneMeal();
                break;

            case CHOPPING:
                chop();
                break;

            case WAITING:
                // Warte, bis der Baum komplett abgebaut ist und die Items gefallen sind
                if (autoRestart.get()) {
                    reset();
                } else {
                    info("Farming cycle complete. Toggling off.");
                    stage = Stage.DONE;
                    toggle();
                }
                break;
            case DONE:
                // Module is toggled off, nothing to do
                break;
        }
    }

    private void findSpot() {
        // Use the selected tree type's sapling
        if (!InvUtils.find(treeType.get().getSaplingItem()).found()) {
            error("No " + treeType.get().toString() + " Saplings found. Disabling.");
            toggle();
            return;
        }

        for (BlockPos pos : BlockPos.iterate(mc.player.getBlockPos().add(-searchRadius.get(), -searchRadius.get(), -searchRadius.get()), mc.player.getBlockPos().add(searchRadius.get(), searchRadius.get(), searchRadius.get()))) {
            if (isSuitable(pos)) {
                plantPos = pos.toImmutable();
                stage = Stage.PLACING_SAPLINGS;
                info("Found a spot at %d, %d, %d. Placing saplings...", plantPos.getX(), plantPos.getY(), plantPos.getZ());
                return;
            }
        }

        error("Could not find a suitable 2x2 spot. Disabling.");
        toggle();
    }

    private boolean isSuitable(BlockPos pos) {
        // Prüft, ob ein 2x2 Bereich frei ist und auf einem validen Block liegt
        BlockPos[] positions = {pos, pos.add(1, 0, 0), pos.add(0, 0, 1), pos.add(1, 0, 1)};
        for (BlockPos p : positions) {
            if (!mc.world.getBlockState(p.down()).isSolidBlock(mc.world, p.down()) || !mc.world.getBlockState(p).isAir()) {
                return false;
            }
            // Prüfe auf genügend Platz nach oben
            for (int i = 1; i < treeType.get().getMinHeight(); i++) { // Use minHeight from TreeType
                if (!mc.world.getBlockState(p.up(i)).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void placeSaplings() {
        if (saplingsPlaced >= 4) {
            stage = Stage.BONE_MEALING;
            info("Saplings placed. Applying bone meal...");
            return;
        }

        FindItemResult sapling = InvUtils.findInHotbar(treeType.get().getSaplingItem());
        if (!sapling.found()) {
            error("Ran out of " + treeType.get().toString() + " saplings. Disabling.");
            toggle();
            return;
        }

        BlockPos targetPos = plantPos.add(SAPLING_OFFSETS[saplingsPlaced]);

        // Use the place method with swing=true and check if it was successful.
        // This is more robust and required by many servers.
        if (BlockUtils.place(targetPos, sapling, true, 0, true)) {
            saplingsPlaced++;
            timer = delay.get();
        }
    }

    private void boneMeal() {
        // Prüfen, ob der Baum bereits gewachsen ist
        Block blockAtPos = mc.world.getBlockState(plantPos).getBlock(); // Use the selected tree type's log
        if (blockAtPos == treeType.get().getLogBlock()) {
            stage = Stage.CHOPPING;
            info("Tree has grown. Chopping...");
            timer = delay.get();
            return;
        }

        // Prüfen, ob der Block immer noch ein Setzling ist. Wenn nicht, ist etwas schiefgelaufen.
        if (!(blockAtPos instanceof SaplingBlock)) { // Check for any sapling block
            error("Something went wrong during growth. Disabling.");
            toggle();
            return;
        }

        FindItemResult boneMeal = InvUtils.findInHotbar(Items.BONE_MEAL);
        if (!boneMeal.found()) {
            error("No bone meal found. Disabling.");
            toggle();
            return;
        }

        // Knochenmehl auf einen der Setzlinge anwenden (wir nehmen einfach den ersten)
        InvUtils.swap(boneMeal.slot(), true);
        // After swapping, the item is in the main hand. We must use Hand.MAIN_HAND directly.
        BlockHitResult hitResult = new BlockHitResult(plantPos.toCenterPos(), Direction.UP, plantPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);
        InvUtils.swapBack();

        timer = delay.get();
    }

    private void chop() {
        FindItemResult axe = InvUtils.find(itemStack -> itemStack.getItem() instanceof AxeItem);
        if (!axe.found()) {
            error("No axe found. Disabling.");
            toggle();
            return;
        }

        // Der Benutzer möchte den *selben Block* abbauen, der gebone-mealt wurde, also plantPos.
        // Überprüfen, ob es tatsächlich ein Log des gewählten Typs ist.
        if (mc.world.getBlockState(plantPos).getBlock() != treeType.get().getLogBlock()) {
            error("Block at plant position is not a " + treeType.get().toString() + " log. Something went wrong. Disabling.");
            toggle();
            return;
        }

        // Wechsle zur Axt und baue den Block ab
        InvUtils.swap(axe.slot(), true);
        // After swapping, the item is in the main hand. We must use Hand.MAIN_HAND directly.
        Rotations.rotate(Rotations.getYaw(plantPos), Rotations.getPitch(plantPos), () -> {
            mc.interactionManager.updateBlockBreakingProgress(plantPos, mc.player.getHorizontalFacing());
            mc.player.swingHand(Hand.MAIN_HAND);
        });
        InvUtils.swapBack();

        info("Chopped tree. Waiting for items...");
        stage = Stage.WAITING;
        timer = 60; // Warte 3 Sekunden (20 ticks/sec * 3 sec = 60 ticks)
    }

    @Override
    public String getInfoString() {
        return stage.name();
    }
}