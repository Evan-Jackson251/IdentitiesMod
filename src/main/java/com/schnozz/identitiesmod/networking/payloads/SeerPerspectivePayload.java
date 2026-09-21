package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SeerPerspectivePayload(UUID id) implements CustomPacketPayload {
    public static final Type<SeerPerspectivePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "seer_perspective_payload"));

    public static final StreamCodec<ByteBuf, SeerPerspectivePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            SeerPerspectivePayload::id,
            SeerPerspectivePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}