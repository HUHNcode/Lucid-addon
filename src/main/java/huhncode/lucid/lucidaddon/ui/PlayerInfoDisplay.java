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
        

        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) {
            ChatUtils.error("Client or world not loaded.");
            return;
        }
        if (targetPlayer == null) {
            ChatUtils.error("Target player is null.");
            return;
        }

        
        SimpleInventory inventory = new SimpleInventory(27); // 3-row chest

        ItemStack lightBlueGlassPane = new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE);
        lightBlueGlassPane.set(DataComponentTypes.CUSTOM_NAME, Text.literal(" ")); // Leerer Name, um Tooltip zu vermeiden

        // Slot 0: Glas
        inventory.setStack(0, lightBlueGlassPane.copy());
        

        // --- Ausrüstung ---
        
        ItemStack mainHand = targetPlayer.getMainHandStack().copy();
        inventory.setStack(1, mainHand); // Slot 1: Haupt-Hand
        
        ItemStack helm = targetPlayer.getInventory().getArmorStack(3).copy();
        inventory.setStack(2, helm); // Slot 2: Helm
        
        ItemStack chest = targetPlayer.getInventory().getArmorStack(2).copy();
        inventory.setStack(3, chest); // Slot 3: Brustplatte
        
        ItemStack legs = targetPlayer.getInventory().getArmorStack(1).copy();
        inventory.setStack(4, legs); // Slot 4: Hose
        
        ItemStack boots = targetPlayer.getInventory().getArmorStack(0).copy();
        inventory.setStack(5, boots); // Slot 5: Schuhe
        
        ItemStack offHand = targetPlayer.getOffHandStack().copy();
        inventory.setStack(6, offHand); // Slot 6: Nebenhand
        
        

        // Slots 7-18: Glas
        for (int i = 7; i <= 18; i++) {
            inventory.setStack(i, lightBlueGlassPane.copy());
        }
        

        // --- Zusatzinformationen ---
        
        // Distanz
        float distance = mc.player.distanceTo(targetPlayer);
        ItemStack distanceStack = new ItemStack(Items.COMPASS);
        distanceStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Distance: " + String.format(Locale.US, "%.1f", distance) + "m"));
        inventory.setStack(19, distanceStack); // Slot 19
        

        // Ping
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(targetPlayer.getUuid());
        int ping = (playerListEntry != null) ? playerListEntry.getLatency() : -1;
        ItemStack pingStack = new ItemStack(Items.REPEATER);
        pingStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Ping: " + (ping == -1 ? "N/A" : ping + "ms")));
        inventory.setStack(20, pingStack); // Slot 20
        

        // Leben
        float health = targetPlayer.getHealth();
        float maxHealth = targetPlayer.getMaxHealth();
        ItemStack healthStack = new ItemStack(Items.RED_DYE);
        healthStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Health: " + String.format(Locale.US, "%.1f", health) + " / " + String.format(Locale.US, "%.1f", maxHealth) + " ")
            .append(Text.literal("❤").formatted(Formatting.RED)));
        inventory.setStack(21, healthStack); // Slot 21
        
        

        
        PlayerInventory playerInventory = mc.player.getInventory();
        Text guiTitle = Text.literal(targetPlayer.getName().getString());

        

        // Slots 22-26: Glas
        for (int i = 22; i <= 26; i++) {
            inventory.setStack(i, lightBlueGlassPane.copy());
        }
        
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

        

        // Innerhalb von PlayerInfoDisplay.show(), direkt vor dem ursprünglichen mc.setScreen()
        // 
        // mc.setScreen(new net.minecraft.client.gui.screen.ChatScreen("Test GUI"));
        // if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen) {
        //    
        // } else {
        //    ChatUtils.error("PlayerInfoDisplay: Einfacher ChatScreen konnte NICHT gesetzt werden."); // DEBUG
        // }
        // return; // Wichtig: return hier, um den Rest der Methode nicht auszuführen für diesen Test

        mc.setScreen(new GenericContainerScreen(screenHandler, playerInventory, guiTitle));
        
    }
}