package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.GravityArrowPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerGravityArrowHandler {
    public static void handle(GravityArrowPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();

        Entity entity = player.level().getEntity(payload.userID());
        if(entity == null){return;}

        if(entity instanceof Player gravPlayer)
        {
            Vec3 arrowPos = gravPlayer.getEyePosition().add(gravPlayer.getLookAngle().scale(1.5));
            Arrow arrow = new Arrow(EntityType.ARROW, gravPlayer.level());

            if (gravPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Gravity")) {
                arrow.setOwner(gravPlayer);
            }

            arrow.setPos(arrowPos);
            arrow.setNoGravity(true);
            arrow.setDeltaMovement(gravPlayer.getLookAngle().scale(10));
            gravPlayer.level().addFreshEntity(arrow);
        }

    }
}
