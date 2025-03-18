package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;

import java.util.Random;

public class AutoTotem extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Random random = new Random();

    private final Setting<Integer> baseDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Base Delay")
        .description("The base delay between slot movements in milliseconds. To be save you shud not go below 200")
        .defaultValue(300)
        .min(10)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Integer> randomDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Random Interval")
        .description("Random additional delay added to the base delay.")
        .defaultValue(100)
        .min(0)
        .sliderMax(500)
        .build()
    );

    private boolean totemPopped = false;
    private int totems;
    private long nextActionTime = 0;

    public AutoTotem() {
        super(LucidAddon.CATEGORY, "Auto Totem+", "Automatically equips a totem when it is popped whit and random delay to avoid anti cheats.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        long currentTime = System.currentTimeMillis();

        if (!totemPopped || currentTime < nextActionTime) return; // Warte auf den Delay nach Totem-Pop

        FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
        totems = result.count();

        if (totems > 0) {
            InvUtils.move().from(result.slot()).toOffhand();
        }

        totemPopped = false; // Zurücksetzen, damit nicht weiter spammt
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityStatusS2CPacket p)) return;
        if (p.getStatus() != EntityStatuses.USE_TOTEM_OF_UNDYING) return;

        Entity entity = p.getEntity(mc.world);
        if (entity == null || !entity.equals(mc.player)) return;

        // Totem ist geplatzt, Delay aktivieren
        totemPopped = true;
        nextActionTime = System.currentTimeMillis() + baseDelay.get() + random.nextInt(randomDelay.get() + 1);
    }

    @Override
    public String getInfoString() {
        return String.valueOf(totems);
    }
}
