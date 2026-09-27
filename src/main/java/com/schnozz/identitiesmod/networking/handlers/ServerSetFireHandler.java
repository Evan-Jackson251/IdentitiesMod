package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.SetFirePayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;

public class ServerSetFireHandler {
    public static void handle(SetFirePayload payload, IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();

        Objects.requireNonNull(level.getEntity(payload.entityId())).setRemainingFireTicks(payload.ticks());
    }
}
