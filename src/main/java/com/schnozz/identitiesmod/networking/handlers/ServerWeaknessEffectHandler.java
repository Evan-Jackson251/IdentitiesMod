package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.WeaknessEffectPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public abstract class ServerWeaknessEffectHandler {
    public static void handle(WeaknessEffectPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        Entity placeholder = player.level().getEntity(payload.targetId());
        if (placeholder instanceof LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, payload.duration(), 0, false, true, true));
        }
    }
}
