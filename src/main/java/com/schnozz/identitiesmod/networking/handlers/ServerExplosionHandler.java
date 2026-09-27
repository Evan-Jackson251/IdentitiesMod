package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.ExplosionPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerExplosionHandler {
    public static void handle(ExplosionPayload payload, IPayloadContext context){
        Level level = context.player().level();
        if(level.getEntity(payload.entityID()) == null){return;}
        level.explode(
                level.getEntity(payload.entityID()),
                payload.x(),
                payload.y(),
                payload.z(),
                payload.power(),
                Level.ExplosionInteraction.BLOCK
        );
    }
}
