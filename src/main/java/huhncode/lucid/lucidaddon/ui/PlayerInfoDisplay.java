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
        lightBlueGlassPane.set(DataComponentTypes.CUSTOM_NAME, Text.literal(" ")); // Empty name to avoid tooltip

        // Slot 0: Glass
        inventory.setStack(0, lightBlueGlassPane.copy());
        

        // --- Equipment ---
        
        ItemStack mainHand = targetPlayer.getMainHandStack().copy();
        inventory.setStack(1, mainHand); // Slot 1: Main-Hand
        
        ItemStack helm = targetPlayer.getInventory().getArmorStack(3).copy();
        inventory.setStack(2, helm); // Slot 2: Helmet
        
        ItemStack chest = targetPlayer.getInventory().getArmorStack(2).copy();
        inventory.setStack(3, chest); // Slot 3: Chestplate
        
        ItemStack legs = targetPlayer.getInventory().getArmorStack(1).copy();
        inventory.setStack(4, legs); // Slot 4: Leggings
        
        ItemStack boots = targetPlayer.getInventory().getArmorStack(0).copy();
        inventory.setStack(5, boots); // Slot 5: Boots
        
        ItemStack offHand = targetPlayer.getOffHandStack().copy();
        inventory.setStack(6, offHand); // Slot 6: Offhand
        
        

        // Slots 7-18: Glass
        for (int i = 7; i <= 18; i++) {
            inventory.setStack(i, lightBlueGlassPane.copy());
        }
        

        // --- Additional Information ---
        
        // Distance
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
        

        // Health
        float health = targetPlayer.getHealth();
        float maxHealth = targetPlayer.getMaxHealth();
        ItemStack healthStack = new ItemStack(Items.RED_DYE);
        healthStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Health: " + String.format(Locale.US, "%.1f", health) + " / " + String.format(Locale.US, "%.1f", maxHealth) + " ")
            .append(Text.literal("❤").formatted(Formatting.RED)));
        inventory.setStack(21, healthStack); // Slot 21
        
        

        
        PlayerInventory playerInventory = mc.player.getInventory();
        Text guiTitle = Text.literal(targetPlayer.getName().getString());

        

        // Slots 22-26: Glass
        for (int i = 22; i <= 26; i++) {
            inventory.setStack(i, lightBlueGlassPane.copy());
        }
        
        GenericContainerScreenHandler screenHandler = new GenericContainerScreenHandler(
            ScreenHandlerType.GENERIC_9X3, // Type for a 3-row chest
            0, // Dummy syncId for client-side GUI
            playerInventory,
            inventory,
            3  // Number of rows in the container inventory
        ) {
            @Override
            public ItemStack quickMove(PlayerEntity player, int slot) {
                return ItemStack.EMPTY; // Prevents shift-clicks
            }

            @Override
            public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
                // Completely blocks any slot interaction
            }

            @Override
            public boolean canInsertIntoSlot(ItemStack stack, net.minecraft.screen.slot.Slot slot) {
                return false; // Prevents inserting items
            }

            @Override
            public boolean canUse(PlayerEntity player) {
                return true; // Always allow use for this client-side GUI
            }
        };

        

        // Inside PlayerInfoDisplay.show(), right before the original mc.setScreen()
        // 
        // mc.setScreen(new net.minecraft.client.gui.screen.ChatScreen("Test GUI"));
        // if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen) {
        //    
        // } else {
        //    ChatUtils.error("PlayerInfoDisplay: Simple ChatScreen could NOT be set."); // DEBUG
        // }
        // return; // Important: return here to not execute the rest of the method for this test

        mc.setScreen(new GenericContainerScreen(screenHandler, playerInventory, guiTitle));
        
    }
}