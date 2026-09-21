package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.EffectAddPayload;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public abstract class ServerEffectAddHandler {

    public static void handle(EffectAddPayload payload, IPayloadContext context)
    {
        Entity entity = context.player().level().getEntity(payload.id());
        if(entity instanceof LivingEntity livingEntity){
            livingEntity.addEffect(new MobEffectInstance(payload.effect(), payload.duration(), payload.level(), false, false,true));
        }
    }
}