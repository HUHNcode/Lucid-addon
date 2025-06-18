package huhncode.lucid.lucidaddon.mixin;

import huhncode.lucid.lucidaddon.commands.PlayerInfoCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void lucidAddon$conditionallyCloseChatOnEnter(MinecraftClient instance, Screen screen) {
        // Prüfe, ob der Aufruf mc.setScreen(null) ist (was nach dem Senden einer Nachricht passiert)
        // und ob unser Flag gesetzt ist.
        if (screen == null && PlayerInfoCommand.isOpeningPlayerInfoGui()) {
            PlayerInfoCommand.setOpeningPlayerInfoGui(false); // Setze das Flag zurück
            // Rufe instance.setScreen(null) NICHT auf, um das Schließen zu verhindern.
            // Unsere GUI wurde bereits (oder wird gerade) über mc.execute() gesetzt.
        } else {
            instance.setScreen(screen); // Führe das ursprüngliche Verhalten aus
        }
    }
}
