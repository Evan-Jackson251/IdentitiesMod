package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SwingPayload(UUID possessed) implements CustomPacketPayload {
    public static final Type<SwingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "swing_payload"));

    public static final StreamCodec<ByteBuf, SwingPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            SwingPayload::possessed,
            SwingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}