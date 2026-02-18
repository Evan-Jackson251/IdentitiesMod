package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.GravityPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerGravityHandler {
    public static void handle(GravityPayload payload, IPayloadContext context)
    {
        ServerPlayer player = (ServerPlayer) context.player();

        Entity entity = player.level().getEntity(payload.entityID());
        if(entity == null) return;
        entity.push(payload.fx(),payload.fy(),payload.fz());
    }
}
