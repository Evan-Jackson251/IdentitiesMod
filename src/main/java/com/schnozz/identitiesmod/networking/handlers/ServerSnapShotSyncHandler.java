package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.sync_payloads.SnapShotSyncPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerSnapShotSyncHandler {
    public static void handle(SnapShotSyncPayload payload, IPayloadContext context) {
        payload.snapShot().applySnapshot(context.player().level());
    }
}