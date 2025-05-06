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
 * Dieses Modul zeigt ein Fake-Inventar (Chest GUI) an, sobald es aktiviert wird.
 * Über die Settings lassen sich Anzahl der Zeilen und das automatische Öffnen konfigurieren.
 */
public class FakeInventory extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    // Konfigurierbare Anzahl der Zeilen (jede Zeile hat 9 Slots)
    public final Setting<Integer> rows = sgGeneral.add(new IntSetting.Builder()
        .name("rows")
        .description("Anzahl der Zeilen im Fake-Inventar")
        .defaultValue(3)
        .min(1)
        .max(6)
        .build()
    );
    
    // Automatisches Öffnen beim Aktivieren des Moduls
    public final Setting<Boolean> autoOpen = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-open")
        .description("Öffnet das Inventar automatisch bei Aktivierung")
        .defaultValue(true)
        .build()
    );
    
    public FakeInventory() {
        // Passe die Kategorie an, wie sie in deinem LucidAddon definiert ist.
        super(huhncode.lucid.lucidaddon.LucidAddon.CATEGORY, "fake-inventory", "Zeigt ein Fake-Inventar (Chest GUI) an.");
    }
    
    @Override
    public void onActivate() {
        if (autoOpen.get() && mc.player != null) {
            openFakeInventory();
        }
    }
    
    /**
     * Öffnet das Fake-Inventar. Es wird ein Dummy-Inventar erstellt, in dem beispielhaft
     * in Slot 0 ein Diamantschwert, in Slot 9 ein Eisenbrustpanzer und in Slot 18 ein goldener Apfel platziert wird.
     */
    public void openFakeInventory() {
        int rowsValue = rows.get();
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
