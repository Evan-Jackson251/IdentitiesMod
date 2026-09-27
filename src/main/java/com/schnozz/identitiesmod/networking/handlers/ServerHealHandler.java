package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.HealPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerHealHandler {
    public static void handle(HealPayload payload, IPayloadContext context)
    {
        ServerPlayer player = (ServerPlayer) context.player();
        ServerLevel serverLevel = (ServerLevel) player.level();
        Entity entity = serverLevel.getEntity(payload.entityId());

        if(entity instanceof LivingEntity livingEntity) {
            livingEntity.heal(payload.health());
        }
    }
}
