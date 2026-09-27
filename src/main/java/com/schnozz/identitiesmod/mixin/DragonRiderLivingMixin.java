package com.schnozz.identitiesmod.mixin;

import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class DragonRiderLivingMixin {

    @Inject(
            method = "isPickable",
            at = @At("HEAD"),
            cancellable = true
    )
    private void identitiesmod$disableRiderPicking(
            CallbackInfoReturnable<Boolean> cir
    ) {
        if ((Object) this instanceof Player player
                && player.getVehicle() instanceof DragonEntity) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "isPushable",
            at = @At("HEAD"),
            cancellable = true
    )
    private void identitiesmod$disableRiderPushing(
            CallbackInfoReturnable<Boolean> cir
    ) {
        if ((Object) this instanceof Player player
                && player.getVehicle() instanceof DragonEntity) {
            cir.setReturnValue(false);
        }
    }
}