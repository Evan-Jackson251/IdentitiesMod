package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record BlackHolePayload(Vec3 pos, double time) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BlackHolePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "black_hole_payload"));

    public static final StreamCodec<ByteBuf,Vec3> VEC_3_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, Vec3::x,
                    ByteBufCodecs.DOUBLE, Vec3::y,
                    ByteBufCodecs.DOUBLE, Vec3::z,
                    Vec3::new
            );

    public static final StreamCodec<ByteBuf, BlackHolePayload> STREAM_CODEC = StreamCodec.composite(
            VEC_3_CODEC,
            BlackHolePayload::pos,
            ByteBufCodecs.DOUBLE,
            BlackHolePayload::time,
            BlackHolePayload::new
    );


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}