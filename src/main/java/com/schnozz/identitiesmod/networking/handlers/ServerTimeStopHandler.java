package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.TimeStopSyncPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerTimeStopHandler {
    public static void handle(TimeStopSyncPayload payload, IPayloadContext context) {
        Player player = context.player();
        player.setData(ModDataAttachments.TIME_STOP_STATE, payload.state());
    }
}
