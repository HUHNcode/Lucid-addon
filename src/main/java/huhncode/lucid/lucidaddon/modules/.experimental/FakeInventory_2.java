package huhncode.lucid.lucidaddon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/**
 * This module displays a fake inventory (Chest GUI) as soon as it is activated.
 * The number of rows and automatic opening can be configured via the settings.
 */
public class FakeInventory extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    // Configurable number of rows (each row has 9 slots)
    public final Setting<Integer> rows = sgGeneral.add(new IntSetting.Builder()
        .name("rows")
        .description("Number of rows in the fake inventory")
        .defaultValue(3)
        .min(1)
        .max(6)
        .build()
    );
    
    // Automatic opening when activating the module
    public final Setting<Boolean> autoOpen = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-open")
        .description("Opens the inventory automatically upon activation")
        .defaultValue(true)
        .build()
    );
    
    public FakeInventory() {
        // Adjust the category as defined in your LucidAddon.
        super(huhncode.lucid.lucidaddon.LucidAddon.CATEGORY, "fake-inventory", "Displays a fake inventory (Chest GUI).");
    }
    
    @Override
    public void onActivate() {
        if (autoOpen.get() && mc.player != null) {
            openFakeInventory();
        }
    }
    
    /**
     * Opens the fake inventory. A dummy inventory is created in which, for example,
     * a diamond sword is placed in slot 0, an iron chestplate in slot 9, and a golden apple in slot 18.
     */
    public void openFakeInventory() {
        int rowsValue = rows.get();
        int slots = rowsValue * 9;
        
        GenericContainerScreenHandler handler = new GenericContainerScreenHandler(
            ScreenHandlerType.GENERIC_9X6, // GENERIC_9X6 covers up to 6 rows
            0,                           // Dummy sync ID (client-side, therefore irrelevant)
            mc.player.getInventory(),
            new FakeInventoryInventory(slots),
            rowsValue
        );
        
        mc.setScreen(new GenericContainerScreen(handler, mc.player.getInventory(), Text.literal("Fake Inventory")));
    }
    
    // Inner class: Dummy inventory that places example items in specific slots.
    private static class FakeInventoryInventory extends SimpleInventory {
        public FakeInventoryInventory(int size) {
            super(size);
            // Fill with examples:
            // Slot 0: Diamond sword, Slot 9: Iron chestplate, Slot 18: Golden apple
            setStack(0, new ItemStack(Items.DIAMOND_SWORD));
            setStack(9, new ItemStack(Items.IRON_CHESTPLATE));
            setStack(18, new ItemStack(Items.GOLDEN_APPLE));
        }
    }
}
