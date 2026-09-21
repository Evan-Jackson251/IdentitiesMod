package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.SeerPerspectivePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerSeerPerspectiveHandler {
    public static void handle(SeerPerspectivePayload payload, IPayloadContext context)
    {
        ServerPlayer player = (ServerPlayer) context.player();
        PacketDistributor.sendToPlayer(player,new SeerPerspectivePayload(payload.id()));
    }
}