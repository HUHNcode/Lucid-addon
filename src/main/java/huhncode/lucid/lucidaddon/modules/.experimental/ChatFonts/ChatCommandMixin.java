package huhncode.lucid.lucidaddon.mixin;

import huhncode.lucid.lucidaddon.events.SendCommandEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ChatCommandMixin {
    @Unique
    private boolean ignoreChatMessage;

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void onSendChatCommand(String message, CallbackInfo ci) {
        if (ignoreChatMessage) return;

        System.out.println("Command intercepted: " + message); // Debug

        SendCommandEvent event = MeteorClient.EVENT_BUS.post(SendCommandEvent.get(message));
        if (!event.isCancelled()) {
            ignoreChatMessage = true;

            // Nachricht über den Client senden, anstatt direkt `sendChatCommand` aufzurufen
            MinecraftClient.getInstance().player.networkHandler.sendChatCommand(event.message);

            ignoreChatMessage = false;
        }
        ci.cancel();
    }
}
