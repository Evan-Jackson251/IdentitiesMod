package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MaxHealthPayload(int maxHealth) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MaxHealthPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "max_health_payload"));

    public static final StreamCodec<ByteBuf, MaxHealthPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MaxHealthPayload::maxHealth,
            MaxHealthPayload::new
    );


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}