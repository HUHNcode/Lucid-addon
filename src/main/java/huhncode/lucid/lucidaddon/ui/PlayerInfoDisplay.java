package huhncode.lucid.lucidaddon.ui;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.Formatting;

import java.util.Locale;

public class PlayerInfoDisplay {

    public static void show(PlayerEntity targetPlayer) {
        ChatUtils.info("PlayerInfoDisplay.show() aufgerufen für: " + (targetPlayer != null ? targetPlayer.getName().getString() : "null")); // DEBUG

        ChatUtils.info("PlayerInfoDisplay: Initialisiere MinecraftClient Instanz."); // DEBUG
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) {
            ChatUtils.error("Client or world not loaded.");
            return;
        }
        if (targetPlayer == null) {
            ChatUtils.error("Target player is null.");
            return;
        }

        ChatUtils.info("PlayerInfoDisplay: Erstelle SimpleInventory."); // DEBUG
        SimpleInventory inventory = new SimpleInventory(27); // 3-row chest

        // --- Ausrüstung ---
        ChatUtils.info("PlayerInfoDisplay: Setze Ausrüstung."); // DEBUG
        ItemStack mainHand = targetPlayer.getMainHandStack().copy();
        inventory.setStack(0, mainHand); // Haupt-Hand
        ChatUtils.info("PlayerInfoDisplay: Slot 0 (MainHand): " + mainHand.getItem().toString() + " Count: " + mainHand.getCount());
        ItemStack helm = targetPlayer.getInventory().getArmorStack(3).copy();
        inventory.setStack(1, helm); // Helm (Slot 3 im Rüstungsarray)
        ChatUtils.info("PlayerInfoDisplay: Slot 1 (Helm): " + helm.getItem().toString());
        ItemStack chest = targetPlayer.getInventory().getArmorStack(2).copy();
        inventory.setStack(2, chest); // Brustplatte (Slot 2)
        ChatUtils.info("PlayerInfoDisplay: Slot 2 (Brustplatte): " + chest.getItem().toString());
        ItemStack legs = targetPlayer.getInventory().getArmorStack(1).copy();
        inventory.setStack(3, legs); // Hose (Slot 1)
        ChatUtils.info("PlayerInfoDisplay: Slot 3 (Hose): " + legs.getItem().toString());
        ItemStack boots = targetPlayer.getInventory().getArmorStack(0).copy();
        inventory.setStack(4, boots); // Schuhe (Slot 0)
        ChatUtils.info("PlayerInfoDisplay: Slot 4 (Schuhe): " + boots.getItem().toString());
        ItemStack offHand = targetPlayer.getOffHandStack().copy();
        inventory.setStack(5, offHand); // Nebenhand
        ChatUtils.info("PlayerInfoDisplay: Slot 5 (OffHand): " + offHand.getItem().toString() + " Count: " + offHand.getCount());
        ChatUtils.info("PlayerInfoDisplay: Ausrüstung gesetzt."); // DEBUG

        // --- Zusatzinformationen ---
        ChatUtils.info("PlayerInfoDisplay: Setze Zusatzinformationen."); // DEBUG
        // Distanz
        float distance = mc.player.distanceTo(targetPlayer);
        ItemStack distanceStack = new ItemStack(Items.COMPASS);
        distanceStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Distance: " + String.format(Locale.US, "%.1f", distance) + "m"));
        inventory.setStack(9, distanceStack);
        ChatUtils.info("PlayerInfoDisplay: Slot 9 (Distanz): " + distance + "m"); // DEBUG

        // Ping
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(targetPlayer.getUuid());
        int ping = (playerListEntry != null) ? playerListEntry.getLatency() : -1;
        ItemStack pingStack = new ItemStack(Items.REPEATER);
        pingStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Ping: " + (ping == -1 ? "N/A" : ping + "ms")));
        inventory.setStack(10, pingStack);
        ChatUtils.info("PlayerInfoDisplay: Slot 10 (Ping): " + ping + "ms"); // DEBUG

        // Leben
        float health = targetPlayer.getHealth();
        float maxHealth = targetPlayer.getMaxHealth();
        ItemStack healthStack = new ItemStack(Items.RED_DYE);
        healthStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Health: " + String.format(Locale.US, "%.1f", health) + " / " + String.format(Locale.US, "%.1f", maxHealth) + " ")
            .append(Text.literal("❤").formatted(Formatting.RED)));
        inventory.setStack(11, healthStack);
        ChatUtils.info("PlayerInfoDisplay: Slot 11 (Leben): " + health + "/" + maxHealth); // DEBUG
        ChatUtils.info("PlayerInfoDisplay: Zusatzinformationen gesetzt."); // DEBUG

        ChatUtils.info("PlayerInfoDisplay: Hole PlayerInventory und setze GUI-Titel."); // DEBUG
        PlayerInventory playerInventory = mc.player.getInventory();
        Text guiTitle = Text.literal(targetPlayer.getName().getString());

        ChatUtils.info("PlayerInfoDisplay: Erstelle GenericContainerScreenHandler."); // DEBUG
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

        ChatUtils.info("PlayerInfoDisplay: Versuche mc.setScreen() aufzurufen."); // DEBUG

        // Innerhalb von PlayerInfoDisplay.show(), direkt vor dem ursprünglichen mc.setScreen()
        // ChatUtils.info("PlayerInfoDisplay: Teste einfachen Screen."); // DEBUG
        mc.setScreen(new net.minecraft.client.gui.screen.ChatScreen("Test GUI"));
        if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen) {
           ChatUtils.info("PlayerInfoDisplay: Einfacher ChatScreen wurde gesetzt."); // DEBUG
        } else {
           ChatUtils.error("PlayerInfoDisplay: Einfacher ChatScreen konnte NICHT gesetzt werden."); // DEBUG
        }
        return; // Wichtig: return hier, um den Rest der Methode nicht auszuführen für diesen Test

        //mc.setScreen(new GenericContainerScreen(screenHandler, playerInventory, guiTitle));
        //ChatUtils.info("PlayerInfoDisplay: mc.setScreen() aufgerufen."); // DEBUG
    }
}