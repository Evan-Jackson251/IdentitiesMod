package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.PossessionEntitySyncPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerPossessionEntitySyncHandler() {
    public static void handle(PossessionEntitySyncPayload payload, IPayloadContext context) {
        context.player().setData(ModDataAttachments.POSSESSER_ENTITY, payload.possessor());
    }
}