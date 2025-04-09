package huhncode.lucid.lucidaddon.mixin;

import huhncode.lucid.lucidaddon.modules.CrystalProtect;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (target instanceof EndCrystalEntity) {
            CrystalProtect crystalProtect = Modules.get().get(CrystalProtect.class);
            if (crystalProtect != null && crystalProtect.shouldBlockCrystalBreaking()) {
                ci.cancel();
            }
        }
    }
}