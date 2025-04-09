package huhncode.lucid.lucidaddon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.inventory.SimpleInventory; // Fehlender Import
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class FakeInventory extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> rows = sgGeneral.add(new IntSetting.Builder()
        .name("rows")
        .description("Number of rows in the fake inventory")
        .defaultValue(3)
        .min(1)
        .max(6)
        .build()
    );

    private final Setting<Boolean> autoOpen = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-open")
        .description("Automatically open when activated")
        .defaultValue(true)
        .build()
    );

    public FakeInventory() {
        super(huhncode.lucid.lucidaddon.LucidAddon.CATEGORY, "fake-inventory", "Displays a fake inventory using Fabric API");
    }

    @Override
    public void onActivate() {
        if (autoOpen.get() && mc.player != null) {
            openFakeInventory();
        }
    }

    private void openFakeInventory() {
        int slots = rows.get() * 9;
        GenericContainerScreenHandler handler = new GenericContainerScreenHandler(
            ScreenHandlerType.GENERIC_9X6, 
            0, 
            mc.player.getInventory(), 
            new FakeInventoryInventory(slots), 
            rows.get()
        );
        
        mc.setScreen(new GenericContainerScreen(
            handler, 
            mc.player.getInventory(), 
            Text.literal("Fake Inventory")
        ));
    }

    private static class FakeInventoryInventory extends SimpleInventory {
        public FakeInventoryInventory(int size) {
            super(size);
            // Beispiel-Items
            setStack(0, new ItemStack(Items.DIAMOND_SWORD));
            setStack(9, new ItemStack(Items.IRON_CHESTPLATE));
            setStack(18, new ItemStack(Items.GOLDEN_APPLE));
        }
    }
}