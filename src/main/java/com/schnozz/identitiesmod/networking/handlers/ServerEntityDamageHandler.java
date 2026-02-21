package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.EntityDamagePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerEntityDamageHandler {
    public static void handle(EntityDamagePayload payload, IPayloadContext context)
    {
        ServerPlayer player = (ServerPlayer) context.player();
        ServerLevel serverLevel = (ServerLevel) player.level();
        Entity hurtEntity = serverLevel.getEntity(payload.hurtEntityID());

        if(hurtEntity != null) {
            DamageSource dSource = new DamageSource(payload.dType(),null,serverLevel.getEntity(payload.attackerEntityID()));
            hurtEntity.hurt(dSource, payload.damage());
        }
    }
}
