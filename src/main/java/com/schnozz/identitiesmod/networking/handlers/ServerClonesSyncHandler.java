package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ClonesSyncPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerClonesSyncHandler() {
    public static void handle(ClonesSyncPayload payload, IPayloadContext context) {
        context.player().setData(ModDataAttachments.CLONES, payload.attachment());
    }
}
