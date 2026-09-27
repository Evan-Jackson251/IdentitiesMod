package com.schnozz.identitiesmod.mixin;

import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Import your DragonEntity here.

@Mixin(Player.class)
public abstract class DragonRiderPlayerMixin {

    @Inject(
            method = "canBeHitByProjectile",
            at = @At("HEAD"),
            cancellable = true
    )
    private void identitiesmod$disableRiderProjectileHits(
            CallbackInfoReturnable<Boolean> cir
    ) {
        Player player = (Player) (Object) this;

        if (player.getVehicle() instanceof DragonEntity) {
            cir.setReturnValue(false);
        }
    }
}
