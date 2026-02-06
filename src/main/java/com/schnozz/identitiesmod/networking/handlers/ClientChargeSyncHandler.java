package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ChargeSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientChargeSyncHandler {
    public static void handle(ChargeSyncPayload payload, IPayloadContext context) {
        LocalPlayer player = Minecraft.getInstance().player;
        player.setData(ModDataAttachments.CHARGE,payload.charge());
    }
}
