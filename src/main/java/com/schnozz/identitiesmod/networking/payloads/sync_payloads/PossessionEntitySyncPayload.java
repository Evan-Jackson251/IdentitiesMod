package com.schnozz.identitiesmod.networking.payloads.sync_payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record PossessionEntitySyncPayload(UUID possessor) implements CustomPacketPayload {
    public static final Type<PossessionEntitySyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "possessor_entity_sync"));

    public static final StreamCodec<ByteBuf, PossessionEntitySyncPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            PossessionEntitySyncPayload::possessor,
            PossessionEntitySyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}