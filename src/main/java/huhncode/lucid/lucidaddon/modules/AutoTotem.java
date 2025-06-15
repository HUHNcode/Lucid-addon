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
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

import java.util.Random;

public class AutoTotem extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Random random = new Random();

    private final Setting<Integer> baseDelay = sgGeneral.add(new IntSetting.Builder()
        .name("base-delay-ms")
        .description("The base delay in milliseconds before equipping a totem. To be safe, you should not go below 200ms.")
        .defaultValue(300)
        .min(10)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Integer> randomDelay = sgGeneral.add(new IntSetting.Builder()
        .name("random-delay-ms")
        .description("Random additional delay in milliseconds added to the base delay.")
        .defaultValue(100)
        .min(0)
        .sliderMax(500)
        .build()
    );

    private final Setting<Boolean> sendClosePacket = sgGeneral.add(new BoolSetting.Builder()
        .name("send-close-packet")
        .description("Sends a packet to the server as if closing the inventory after equipping the totem.")
        .defaultValue(false) // Standardmäßig aus
        .build()
    );

    private boolean totemPopped = false;
    private int totems;
    private long nextActionTime = 0;

    public AutoTotem() {
        super(LucidAddon.CATEGORY, "Auto Totem+", "Automatically equips a totem when it is popped with a random delay to avoid anti-cheats.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        long currentTime = System.currentTimeMillis();

        if (!totemPopped || currentTime < nextActionTime) return; // Warte auf den Delay nach Totem-Pop
        if (mc.player == null || mc.getNetworkHandler() == null) {
            totemPopped = false; // Reset if player or network handler is null
            return;
        }

        FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
        totems = result.count();

        if (totems > 0) {
            InvUtils.move().from(result.slot()).toOffhand(); // This likely uses InvUtils.swap() internally

            if (sendClosePacket.get() && mc.player.currentScreenHandler != null) {
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }
        }

        totemPopped = false; // Zurücksetzen, damit nicht weiter spammt
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityStatusS2CPacket p)) return;
        if (p.getStatus() != EntityStatuses.USE_TOTEM_OF_UNDYING) return;

        net.minecraft.entity.Entity entity = p.getEntity(mc.world);
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
