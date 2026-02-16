package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.PowerSyncPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPowerSyncHandler {
    public static void handle(PowerSyncPayload payload, IPayloadContext context) {
        context.player().setData(ModDataAttachments.POWER_TYPE, payload.power());
    }
}
