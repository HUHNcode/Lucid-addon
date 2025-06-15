package huhncode.lucid.lucidaddon.modules;

import huhncode.lucid.lucidaddon.LucidAddon;


import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public class PacketLogger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<Class<? extends Packet<?>>>> s2cPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("S2C-packets")
        .description("Server-to-client packets to log.")
        .filter(aClass -> PacketUtils.getS2CPackets().contains(aClass))
        .build()
    );

    private final Setting<Set<Class<? extends Packet<?>>>> c2sPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("C2S-packets")
        .description("Client-to-server packets to log.")
        .filter(aClass -> PacketUtils.getC2SPackets().contains(aClass))
        .build()
    );

    public PacketLogger() {
        super(LucidAddon.CATEGORY, "packet-logger", "Logs specific packets to console.");
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (s2cPackets.get().contains(event.packet.getClass())) log("incoming", event.packet);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onSendPacket(PacketEvent.Send event) {
        if (c2sPackets.get().contains(event.packet.getClass())) log("outgoing", event.packet);
    }

    private void log(String prefix, Packet<?> packet) {
        ChatUtils.info("Packet %s %s", prefix, packetToString(packet));
    }

    /**
     * Returns a string representation of the packet.
     * The string representation consists of the packet's name and a list of the packet's values, enclosed in square brackets ("[]").
     * Adjacent values are separated by the characters ", " (a comma followed by a space).
     * Values are converted to strings as by {@link Objects#toString(Object)}
     *
     * @param packet the packet whose string representation to return
     * @return a string representation of packet
     */
    private String packetToString(Packet<?> packet) {
        String packetName = PacketUtils.getName((Class<? extends Packet<?>>) packet.getClass());

        try {
            StringJoiner values = new StringJoiner(", ", "[", "]");
            Class<?> clazz = packet.getClass();

            // Traverse up the class hierarchy to find a class with declared fields,
            // as some packet classes might inherit all their fields.
            while (clazz != null && clazz.getDeclaredFields().length == 0) {
                clazz = clazz.getSuperclass();
            }

            // If clazz is null here, it means we reached Object.class without finding fields
            // or the original class was an interface.
            if (clazz != null) {
                for (Field f : clazz.getDeclaredFields()) {
                    if (Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers()))
                        continue; // constant

                    if (!f.canAccess(packet))
                        f.setAccessible(true);

                    @Nullable Object value = f.get(Modifier.isStatic(f.getModifiers()) ? null : packet);
                    values.add(Objects.toString(value));
                }
            }
            return String.join(" ", packetName, values.toString());
        } catch (Exception e) {
            MeteorClient.LOG.error("Cannot construct packet values string", e);
            return packetName;
        }
    }
}