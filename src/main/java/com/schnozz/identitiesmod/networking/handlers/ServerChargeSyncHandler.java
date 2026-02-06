package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ChargeSyncPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerChargeSyncHandler {
    public static void handle(ChargeSyncPayload payload, IPayloadContext context) {
        Player player = context.player();
        player.setData(ModDataAttachments.CHARGE, payload.charge());
    }
}
