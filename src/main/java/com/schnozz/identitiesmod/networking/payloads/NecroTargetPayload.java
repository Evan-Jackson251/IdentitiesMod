package com.schnozz.identitiesmod.networking.payloads;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerSummons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent when the necromancer presses the set-target key. Carries no data: the server
 * already knows who sent it (context.player()) and does the raycast itself.
 */
public record NecroTargetPayload() implements CustomPacketPayload {
    public static final NecroTargetPayload INSTANCE = new NecroTargetPayload();

    public static final Type<NecroTargetPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "necro_target"));

    public static final StreamCodec<ByteBuf, NecroTargetPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NecroTargetPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!"Necromancer".equals(player.getData(ModDataAttachments.POWER_TYPE))) return;

            LivingEntity target = NecromancerSummons.findLookTarget(player, NecromancerSummons.TARGET_RANGE);
            if (target == null) {
                player.displayClientMessage(Component.literal("No target in sight"), true);
                return;
            }

            int count = NecromancerSummons.setTeamTarget(player, target);
            if (count == 0) {
                player.displayClientMessage(Component.literal("No summons out"), true);
            } else {
                player.displayClientMessage(
                        Component.literal("Team attacking ").append(target.getDisplayName()), true);
            }
        });
    }
}
