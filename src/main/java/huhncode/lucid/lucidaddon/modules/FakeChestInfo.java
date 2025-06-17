package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
//import net.minecraft.inventory.Inventory; // Nicht mehr benötigt, da SimpleInventory Inventory implementiert
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
// import net.minecraft.nbt.NbtCompound; // Nicht mehr benötigt
// import net.minecraft.nbt.NbtList; // Nicht mehr benötigt
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference; // Import für Reference
import net.minecraft.text.Text;


public class FakeChestInfo extends Module {

    private Screen currentScreenInstance = null;

    public FakeChestInfo() {
        super(LucidAddon.CATEGORY, "fake-chest-info", "Zeigt eine clientseitige Truhen-GUI mit optimal verzauberter Netherite-Rüstung als Info.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) {
            ChatUtils.error("Spieler oder Welt nicht geladen.");
            toggle(); // Deaktiviere das Modul, wenn die GUI nicht geöffnet werden kann
            return;
        }

        // Erstelle ein 3-reihiges Truheninventar (27 Slots)
        SimpleInventory inventory = new SimpleInventory(27); // GENERIC_9X3 verwendet 3 Reihen

        // Platziere die Netherite-Rüstungsteile im Inventar mit Verzauberungen
        // Mittlere Reihe für bessere Sichtbarkeit: Slots 10, 12, 14, 16
        inventory.setStack(10, createEnchantedArmor(Items.NETHERITE_HELMET,
            new RegistryKey[]{Enchantments.PROTECTION, Enchantments.UNBREAKING, Enchantments.MENDING, Enchantments.RESPIRATION, Enchantments.AQUA_AFFINITY},
            new int[]{4, 3, 1, 3, 1}));

        inventory.setStack(12, createEnchantedArmor(Items.NETHERITE_CHESTPLATE,
            new RegistryKey[]{Enchantments.PROTECTION, Enchantments.UNBREAKING, Enchantments.MENDING, Enchantments.THORNS},
            new int[]{4, 3, 1, 3}));

        inventory.setStack(14, createEnchantedArmor(Items.NETHERITE_LEGGINGS,
            new RegistryKey[]{Enchantments.PROTECTION, Enchantments.UNBREAKING, Enchantments.MENDING, Enchantments.SWIFT_SNEAK},
            new int[]{4, 3, 1, 3}));

        inventory.setStack(16, createEnchantedArmor(Items.NETHERITE_BOOTS,
            new RegistryKey[]{Enchantments.PROTECTION, Enchantments.UNBREAKING, Enchantments.MENDING, Enchantments.FEATHER_FALLING, Enchantments.DEPTH_STRIDER, Enchantments.SOUL_SPEED},
            new int[]{4, 3, 1, 4, 3, 3}));

        PlayerInventory playerInventory = mc.player.getInventory();
        Text guiTitle = Text.literal("Info: Optimale Rüstung");

        // Erstelle einen benutzerdefinierten ScreenHandler, der Interaktionen verhindert
        GenericContainerScreenHandler screenHandler = new GenericContainerScreenHandler(
            ScreenHandlerType.GENERIC_9X3, // Typ für eine 3-reihige Truhe
            0, // Dummy syncId für clientseitige GUI
            playerInventory,
            inventory,
            3  // Anzahl der Reihen im Container-Inventar
        ) {
            @Override
            public ItemStack quickMove(PlayerEntity player, int slot) {
                return ItemStack.EMPTY; // Verhindert Shift-Klicks
            }

            @Override
            public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
                // Blockiert jegliche Slot-Interaktion vollständig
            }

            @Override
            public boolean canInsertIntoSlot(ItemStack stack, net.minecraft.screen.slot.Slot slot) {
                return false; // Verhindert das Einfügen von Items
            }

            @Override
            public boolean canUse(PlayerEntity player) {
                return true; // Erlaube immer die Nutzung für diese clientseitige GUI
            }
        };

        currentScreenInstance = new GenericContainerScreen(screenHandler, playerInventory, guiTitle);
        mc.setScreen(currentScreenInstance);
    }

    // Typ des Arrays für enchantmentKeys präzisiert
    private ItemStack createEnchantedArmor(Item item, RegistryKey<Enchantment>[] enchantmentKeys, int[] levels) {
        ItemStack itemStack = new ItemStack(item);

        if (mc.world == null) {
            ChatUtils.error("Welt nicht geladen, kann Verzauberungen nicht anwenden.");
            return itemStack;
        }

        // Verwende getOrThrow, um die Registry zu erhalten
        Registry<Enchantment> enchantmentRegistry = mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        if (enchantmentRegistry == null) {
            ChatUtils.error("Enchantment-Registry nicht gefunden.");
            return itemStack;
        }

        for (int i = 0; i < enchantmentKeys.length; i++) {
            RegistryKey<Enchantment> key = enchantmentKeys[i];
            // Hole den RegistryEntry für den Schlüssel
            java.util.Optional<Reference<Enchantment>> enchantmentEntryOpt = enchantmentRegistry.getEntry(key.getValue());

            if (enchantmentEntryOpt.isPresent()) {
                itemStack.addEnchantment(enchantmentEntryOpt.get(), levels[i]);
            } else {
                ChatUtils.warning("Konnte Verzauberungseintrag nicht finden für Schlüssel: " + key.getValue());
            }
        }

        return itemStack;
    }

    @Override
    public void onDeactivate() {
        if (mc.currentScreen == currentScreenInstance && currentScreenInstance != null) {
            currentScreenInstance.close(); // Schließt den Bildschirm
        }
        currentScreenInstance = null;
    }
}