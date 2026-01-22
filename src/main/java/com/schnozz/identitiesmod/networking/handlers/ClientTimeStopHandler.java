package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.TimeStopSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientTimeStopHandler {
    public static void handle(TimeStopSyncPayload payload, IPayloadContext context) {
        LocalPlayer player = Minecraft.getInstance().player;
        player.setData(ModDataAttachments.TIME_STOP_STATE,payload.state());
    }
}
