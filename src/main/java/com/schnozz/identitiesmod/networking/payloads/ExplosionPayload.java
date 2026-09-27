package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ExplosionPayload(int entityID, double x, double y, double z, float power) implements CustomPacketPayload {
    public static final Type<ExplosionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "explosion_payload"));

    public static final StreamCodec<ByteBuf, ExplosionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ExplosionPayload::entityID,
            ByteBufCodecs.DOUBLE,
            ExplosionPayload::x,
            ByteBufCodecs.DOUBLE,
            ExplosionPayload::y,
            ByteBufCodecs.DOUBLE,
            ExplosionPayload::z,
            ByteBufCodecs.FLOAT,
            ExplosionPayload::power,
            ExplosionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}