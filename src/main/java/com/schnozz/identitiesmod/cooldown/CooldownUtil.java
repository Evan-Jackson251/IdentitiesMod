package com.schnozz.identitiesmod.cooldown;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class CooldownUtil {
    public static void SetCooldown(LocalPlayer player, String cdString, long currentTime, long duration)
    {
        CooldownAttachment attachment = new CooldownAttachment();
        attachment.getAllCooldowns().putAll(player.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
        attachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", cdString), currentTime, duration);
        player.setData(ModDataAttachments.COOLDOWN, attachment);
        PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, duration), ResourceLocation.fromNamespaceAndPath("identitiesmod", cdString), false));
    }
}
