package com.schnozz.identitiesmod.networking.payloads;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerSummons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.IntFunction;

/**
 * A team-wide order from the necromancer. One payload for all orders, so adding a new
 * order later is just a new enum value and a new switch case, with no extra registration.
 */
public record NecroOrderPayload(Order order) implements CustomPacketPayload {

    public enum Order {
        RECALL,     // clear targets, go back to following
        TELEPORT;   // teleport to the necromancer and clear targets

        // An out-of-range id from a bad client falls back to RECALL instead of crashing the server.
        private static final IntFunction<Order> BY_ID =
                ByIdMap.continuous(Order::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

        public static final StreamCodec<ByteBuf, Order> STREAM_CODEC =
                ByteBufCodecs.idMapper(BY_ID, Order::ordinal);
    }

    public static final Type<NecroOrderPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "necro_order"));

    public static final StreamCodec<ByteBuf, NecroOrderPayload> STREAM_CODEC =
            Order.STREAM_CODEC.map(NecroOrderPayload::new, NecroOrderPayload::order);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NecroOrderPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!"Necromancer".equals(player.getData(ModDataAttachments.POWER_TYPE))) return;

            // Each order reports what it did and how many summons it reached, so you can
            // tell from the action bar which order the server actually ran.
            String message = switch (payload.order()) {
                case RECALL -> {
                    int count = NecromancerSummons.recallTeam(player);
                    yield count == 0 ? "Recall: no summons out" : "Recalled " + count + " summon(s)";
                }
                case TELEPORT -> {
                    int count = NecromancerSummons.teleportTeamToPlayer(player);
                    yield count == 0 ? "Teleport: no summons found" : "Teleported " + count + " summon(s) to you";
                }
            };

            player.displayClientMessage(Component.literal(message), true);
        });
    }
}