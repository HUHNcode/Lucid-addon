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
        // Check if the call is mc.setScreen(null) (which happens after sending a message)
        // and if our flag is set.
        if (screen == null && PlayerInfoCommand.isOpeningPlayerInfoGui()) {
            PlayerInfoCommand.setOpeningPlayerInfoGui(false); // Reset the flag
            // Do NOT call instance.setScreen(null) to prevent closing.
            // Our GUI has already been (or is being) set via mc.execute().
        } else {
            instance.setScreen(screen); // Execute the original behavior
        }
    }
}
