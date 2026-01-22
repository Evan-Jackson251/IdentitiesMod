package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.SpeedsterLightningSync;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerSpeedsterLightningHandler {
    public static void handle(SpeedsterLightningSync payload, IPayloadContext context) {
        Player player = context.player();
        player.setData(ModDataAttachments.SPEEDSTER_LIGHTNING, payload.state());
    }
}
