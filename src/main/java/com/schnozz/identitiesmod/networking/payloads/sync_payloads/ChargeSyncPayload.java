package com.schnozz.identitiesmod.networking.payloads.sync_payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChargeSyncPayload(double charge) implements CustomPacketPayload {
    public static final Type<ChargeSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "charge_sync"));

    public static final StreamCodec<ByteBuf, ChargeSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            ChargeSyncPayload::charge,
            ChargeSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}