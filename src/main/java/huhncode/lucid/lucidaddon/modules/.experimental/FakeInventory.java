package huhncode.lucid.lucidaddon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

public class FakeInventory extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSlots = settings.createGroup("Slots");

    // General Settings
    private final Setting<Boolean> autoOpen = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-open")
        .description("Automatically open when activated")
        .defaultValue(false)
        .build()
    );

    // Slot Visibility
    private final Setting<Boolean> showMainHand = sgSlots.add(new BoolSetting.Builder()
        .name("main-hand")
        .description("Show main hand item")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showHelmet = sgSlots.add(new BoolSetting.Builder()
        .name("helmet")
        .description("Show helmet")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showChestplate = sgSlots.add(new BoolSetting.Builder()
        .name("chestplate")
        .description("Show chestplate")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showLeggings = sgSlots.add(new BoolSetting.Builder()
        .name("leggings")
        .description("Show leggings")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showBoots = sgSlots.add(new BoolSetting.Builder()
        .name("boots")
        .description("Show boots")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showOffhand = sgSlots.add(new BoolSetting.Builder()
        .name("offhand")
        .description("Show offhand item")
        .defaultValue(true)
        .build()
    );

    public FakeInventory() {
        super(huhncode.lucid.lucidaddon.LucidAddon.CATEGORY, "fake-inventory", "Displays visible player equipment");
    }

    @Override
    public void onActivate() {
        if (autoOpen.get() && mc.player != null) {
            openFakeInventory();
        }
    }

    private void openFakeInventory() {
        // Create inventory with 6 slots (2 rows à 3)
        SimpleInventory fakeInv = new SimpleInventory(6);
        PlayerEntity player = mc.player;

        // Fill slots based on settings
        if (showMainHand.get()) fakeInv.setStack(0, player.getMainHandStack());
        if (showHelmet.get()) fakeInv.setStack(1, player.getInventory().armor.get(3));
        if (showChestplate.get()) fakeInv.setStack(2, player.getInventory().armor.get(2));
        if (showLeggings.get()) fakeInv.setStack(3, player.getInventory().armor.get(1));
        if (showBoots.get()) fakeInv.setStack(4, player.getInventory().armor.get(0));
        if (showOffhand.get()) fakeInv.setStack(5, player.getOffHandStack());

        GenericContainerScreenHandler handler = new GenericContainerScreenHandler(
            ScreenHandlerType.GENERIC_9X3,
            0,
            mc.player.getInventory(),
            fakeInv,
            2 // rows
        );

        mc.setScreen(new GenericContainerScreen(
            handler,
            mc.player.getInventory(),
            Text.literal("Player Equipment")
        ));
    }

    @Override
    public String getInfoString() {
        return String.format(
            "%d/%d",
            getVisibleItemCount(),
            6
        );
    }

    private int getVisibleItemCount() {
        int count = 0;
        if (showMainHand.get()) count++;
        if (showHelmet.get()) count++;
        if (showChestplate.get()) count++;
        if (showLeggings.get()) count++;
        if (showBoots.get()) count++;
        if (showOffhand.get()) count++;
        return count;
    }
}