package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.PossessionTimerSyncPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerPossessionTimerSyncHandler() {
    public static void handle(PossessionTimerSyncPayload payload, IPayloadContext context) {
        context.player().setData(ModDataAttachments.POSSESSION_TIMER, payload.possessed());
    }
}