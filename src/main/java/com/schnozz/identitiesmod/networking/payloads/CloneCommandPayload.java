package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

public record CloneCommandPayload(int command, int cloneId, Vec3 pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CloneCommandPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "clone_command_payload"));

    public static final StreamCodec<ByteBuf, Vec3> VEC3_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new
    );

    public static final StreamCodec<ByteBuf, CloneCommandPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            CloneCommandPayload::command,
            ByteBufCodecs.INT,
            CloneCommandPayload::cloneId,
            VEC3_CODEC,
            CloneCommandPayload::pos,
            CloneCommandPayload::new
    );


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}