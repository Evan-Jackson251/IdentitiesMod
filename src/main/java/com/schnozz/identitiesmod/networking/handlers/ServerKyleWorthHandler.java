package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.leveldata.FarmValueSavedData;
import com.schnozz.identitiesmod.networking.payloads.KyleWorthPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerKyleWorthHandler {
    public static void handle(KyleWorthPayload payload, IPayloadContext context)
    {
        ServerPlayer player = (ServerPlayer) context.player();
        Entity placeholder = player.level().getEntity(payload.id());
        if (placeholder instanceof Player kyle) {
            long farmValue = FarmValueSavedData.get(kyle.level().getServer()).getValue();
            kyle.sendSystemMessage(Component.literal("Kyle Worth: " + farmValue).withStyle(ChatFormatting.DARK_GREEN,ChatFormatting.BOLD));
        }
    }
}
