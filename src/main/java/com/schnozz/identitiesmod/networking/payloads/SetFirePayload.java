package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetFirePayload(int ticks, int entityId) implements CustomPacketPayload {
    public static final Type<SetFirePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "set_fire_payload"));

    public static final StreamCodec<ByteBuf, SetFirePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SetFirePayload::ticks,
            ByteBufCodecs.INT,
            SetFirePayload::entityId,
            SetFirePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}