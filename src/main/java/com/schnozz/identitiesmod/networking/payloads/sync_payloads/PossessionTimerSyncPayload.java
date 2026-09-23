package com.schnozz.identitiesmod.networking.payloads.sync_payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PossessionTimerSyncPayload(int possessed) implements CustomPacketPayload {
    public static final Type<PossessionTimerSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "possession_sync"));

    public static final StreamCodec<ByteBuf, PossessionTimerSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PossessionTimerSyncPayload::possessed,
            PossessionTimerSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}