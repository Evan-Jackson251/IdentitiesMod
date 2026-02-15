package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.sync_payloads.PowersTakenSyncPayload;
import com.schnozz.identitiesmod.util.ClientPowerTakenData;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPowersTakenSyncHandler {
    public static void handle(PowersTakenSyncPayload payload, IPayloadContext context) {
        Minecraft.getInstance().execute(() -> {
            ClientPowerTakenData.setPowers(payload.powersTaken());
        });
    }
}
