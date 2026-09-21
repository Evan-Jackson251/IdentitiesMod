package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.SeerPerspectivePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientSeerPerspectiveHandler {
    public static void handle(SeerPerspectivePayload payload, IPayloadContext context) {
        Minecraft.getInstance().execute(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                Player targetPlayer = player.level().getPlayerByUUID(payload.id());
                if(targetPlayer == null){return;}
                Minecraft.getInstance().setCameraEntity(targetPlayer);
            }
        });
    }
}
