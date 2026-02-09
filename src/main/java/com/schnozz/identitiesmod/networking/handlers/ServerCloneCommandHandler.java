package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.entities.custom_entities.PlayerCloneEntity;
import com.schnozz.identitiesmod.networking.payloads.CloneCommandPayload;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerCloneCommandHandler() {
    public static void handle(CloneCommandPayload payload, IPayloadContext context) {
        PlayerCloneEntity clone = (PlayerCloneEntity)context.player().level().getEntity(payload.cloneId());
        if(payload.command() == 1)//Attack Entity
        {
            if(payload.targetEntityId() >= 0){
                LivingEntity entity = (LivingEntity)context.player().level().getEntity(payload.targetEntityId());
                clone.attackTarget(entity);
            }
        }
        else if(payload.command() == 2){//Untarget Entity
            clone.undoAttackTarget();
        }
        else if(payload.command() == 3){//Peaceful Mode
            clone.peacefulTargeting();
        }
        else if(payload.command() == 4){//Aggresive Mode
            clone.aggressiveTargeting();
        }
        else if(payload.command() == 5){//Follow Player
            clone.followPlayer(context.player());
        }
    }
}