package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.AvailablePowersSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientAvailablePowersSyncHandler() {
    public static void handle(AvailablePowersSyncPayload payload, IPayloadContext context) {
        Minecraft.getInstance().execute(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.setData(ModDataAttachments.AVAILABLE_POWERS, payload.attachment());
            }
        });
    }
}