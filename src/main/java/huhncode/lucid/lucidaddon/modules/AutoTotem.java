package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Set;

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
        .defaultValue(true) // Standardmäßig an
        .build()
    );

    private final Setting<Boolean> blockPackets = sgGeneral.add(new BoolSetting.Builder()
        .name("block-packets-while-restocking")
        .description("Blocks certain packets while the totem is being equipped.\n\nThis is not recommended as it can trigger anti-cheats.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Set<Class<? extends Packet<?>>>> packetsToBlock = sgGeneral.add(new PacketListSetting.Builder()
        .name("packets-to-block")
        .description("Which packets to block while restocking. This makes you unable to move or interact.")
        .defaultValue(Set.of(
            PlayerInteractBlockC2SPacket.class,
            PlayerInteractEntityC2SPacket.class,
            PlayerActionC2SPacket.class,
            PlayerMoveC2SPacket.Full.class,
            PlayerMoveC2SPacket.PositionAndOnGround.class,
            PlayerMoveC2SPacket.LookAndOnGround.class,
            UpdateSelectedSlotC2SPacket.class
        ))
        .visible(blockPackets::get)
        .build()
    );

    private boolean totemPopped = false;
    private int totems;
    private long nextActionTime = 0;
    private volatile boolean isRestocking = false;

    public AutoTotem() {
        super(LucidAddon.CATEGORY, "Auto Totem+", "Automatically equips a totem when it is popped with a random delay to avoid anti-cheats.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // If a restock is already in progress, let it finish. Don't start a new one.
        if (isRestocking) return;

        long currentTime = System.currentTimeMillis();

        if (!totemPopped || currentTime < nextActionTime) return; // Warte auf den Delay nach Totem-Pop
        if (mc.player == null || mc.getNetworkHandler() == null) {
            totemPopped = false; // Reset if player or network handler is null
            isRestocking = false;
            return;
        }

        // Warte, bis der Offhand-Slot tatsächlich leer ist.
        // Das vermeidet Probleme mit der Synchronisation und verhindert, dass andere Items ersetzt werden.
        ItemStack offhandStack = mc.player.getOffHandStack();
        if (!offhandStack.isEmpty()) {
            // Wenn bereits ein neues Totem da ist, ist unsere Arbeit getan.
            if (offhandStack.getItem() == Items.TOTEM_OF_UNDYING) {
                totemPopped = false;
                isRestocking = false;
            }
            // Ansonsten warten wir, bis der Slot frei wird. Wir setzen das Flag nicht zurück.
            return;
        }

        FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
        totems = result.count();

        if (totems > 0) {
            // Start the restocking process and lock interactions.
            if (blockPackets.get()) {
                isRestocking = true;
            }

            // BUGFIX: InvUtils.find() gibt einen Inventar-Slot zurück, aber clickSlot() benötigt einen Bildschirm-Slot.
            // Das führte dazu, dass Hotbar-Slot-IDs (z.B. 6) als Bildschirm-Slot-IDs verwendet wurden,
            // was Rüstungs-Slots (z.B. Brustplatte) entspricht und das falsche Item bewegt hat.
            int screenSlot = result.slot();
            if (result.isHotbar()) {
                screenSlot += 36; // Konvertiert den Hotbar-Inventar-Slot in die korrekte Bildschirm-Slot-ID.
            }

            // The previous method used two PICKUP packets, which is easily detected by anti-cheats.
            // This now uses a single SWAP action, which is much less suspicious.
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, screenSlot, 40, SlotActionType.SWAP, mc.player);

            // The trigger flag can be reset now. The isRestocking flag will manage the lock from here.
            totemPopped = false;

            if (sendClosePacket.get()) {
                int delay = 5 + random.nextInt(36); // 5ms bis 40ms
                //ChatUtils.info("Totem Restocked, close packet sent in " + delay + "ms...");

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mc.player != null) {
                            mc.execute(() -> {
                                // Wenn ein GUI-Fenster offen ist, schließe es auf die saubere Art.
                                if (mc.currentScreen != null) {
                                    mc.player.closeHandledScreen();
                                }
                                // Wenn kein Fenster offen ist, sende trotzdem das Paket für das Spieler-Inventar.
                                // Das ist wichtig, um die Aktion für Anti-Cheats zu legitimieren.
                                else {
                                    mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                                }
                                isRestocking = false; // Gib die Sperre nach dem Senden frei.
                            });
                        } else {
                            isRestocking = false; // Failsafe
                        }
                    }
                }, delay);
            } else {
                // No close packet is sent. Release the lock after a short, fixed delay
                // to simulate the time a player would be "busy" in their inventory.
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isRestocking = false;
                    }
                }, 150); // 150ms delay
            }
        } else {
            // No totems found, reset the trigger.
            totemPopped = false;
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!isRestocking || !blockPackets.get()) return;

        if (packetsToBlock.get().contains(event.packet.getClass())) {
            ChatUtils.info("Blocking packet: " + event.packet.getClass().getSimpleName()); // Uncomment for debugging
            event.cancel();
        }
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


/*
[Meteor] Packet outgoing ClickSlotC25Packet [0, 10, 12, 40, SWAP, O minecraft:air, (12=>0 minecraft:air, 45=>1 minecraft totem_of_undying)]
/* */
