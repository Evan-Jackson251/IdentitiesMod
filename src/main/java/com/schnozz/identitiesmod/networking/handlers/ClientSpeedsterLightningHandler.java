package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.SpeedsterLightningSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientSpeedsterLightningHandler {
    public static void handle(SpeedsterLightningSync payload, IPayloadContext context) {
        LocalPlayer player = Minecraft.getInstance().player;
        player.setData(ModDataAttachments.SPEEDSTER_LIGHTNING,payload.state());
    }
}
