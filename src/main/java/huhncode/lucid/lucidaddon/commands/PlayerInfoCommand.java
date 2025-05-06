package huhncode.lucid.lucidaddon.commands;

import net.minecraft.command.CommandSource;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.commands.Command;
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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;


public class PlayerInfoCommand extends Command {
    public PlayerInfoCommand() {
        super("fakeinventory", "Öffnet das Fake-Inventar (Chest GUI) über den Command.");
    }
    
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            openFakeInventory();
            return SINGLE_SUCCESS;
        });
    }

    private void openFakeInventory() {
        int rowsValue = 3;
        int slots = rowsValue * 9;
        
        GenericContainerScreenHandler handler = new GenericContainerScreenHandler(
            ScreenHandlerType.GENERIC_9X6, // GENERIC_9X6 deckt bis zu 6 Zeilen ab
            0,                           // Dummy-Sync-ID (clientseitig, daher irrelevant)
            mc.player.getInventory(),
            new FakeInventoryInventory(slots),
            rowsValue
        );
        
        mc.setScreen(new GenericContainerScreen(handler, mc.player.getInventory(), Text.literal("Fake Inventory")));
    }

    // Innere Klasse: Dummy-Inventar, das beispielhafte Items in bestimmten Slots platziert.
    private static class FakeInventoryInventory extends SimpleInventory {
        public FakeInventoryInventory(int size) {
            super(size);
            // Befülle exemplarisch:
            // Slot 0: Diamantschwert, Slot 9: Eisenbrustpanzer, Slot 18: Goldener Apfel
            setStack(0, new ItemStack(Items.DIAMOND_SWORD));
            setStack(9, new ItemStack(Items.IRON_CHESTPLATE));
            setStack(18, new ItemStack(Items.GOLDEN_APPLE));
        }
    }
}