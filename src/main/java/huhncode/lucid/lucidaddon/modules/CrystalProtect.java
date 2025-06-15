package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.events.entity.player.PickItemsEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import meteordevelopment.meteorclient.events.entity.player.PickItemsEvent;


public class CrystalProtect extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgConditions = settings.createGroup("Conditions");

    public CrystalProtect() {
        super(LucidAddon.CATEGORY, "Crystal Protect", "Blocks crystal breaking after a kill.");
    }

    // Settings
    private final Setting<Double> blockDuration = sgGeneral.add(new DoubleSetting.Builder()
        .name("block-duration")
        .description("How long to block crystal breaking after a kill (in seconds).")
        .defaultValue(1.0)
        .min(0.1)
        .max(10.0)
        .build()
    );




    @EventHandler
    private void onItemPickup(PickItemsEvent event) {
        ItemStack pickedStack = event.itemStack;
        if (pickedStack.getItem() == Items.NETHERITE_BLOCK || pickedStack.getItem() == Items.OAK_LOG) {
            ChatUtils.info("You picked up a " + pickedStack.getItem().getName());
        }
    }


}