package huhncode.lucid.lucidaddon.mixin;

import huhncode.lucid.lucidaddon.events.CommandAttemptEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Commands.class, remap = false)
public class CommandsMixin {
    @Inject(method = "dispatch", at = @At("HEAD"), cancellable = true)
    private static void onDispatch(String message, CallbackInfo info) {
        CommandAttemptEvent event = CommandAttemptEvent.get(message);
        MeteorClient.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            info.cancel();
        }
    }
}