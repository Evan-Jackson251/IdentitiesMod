package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record CloneCommandPayload(int command, int cloneId, int targetEntityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CloneCommandPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "clone_command_payload"));

    public static final StreamCodec<ByteBuf, CloneCommandPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            CloneCommandPayload::command,
            ByteBufCodecs.INT,
            CloneCommandPayload::cloneId,
            ByteBufCodecs.INT,
            CloneCommandPayload::targetEntityId,
            CloneCommandPayload::new
    );


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}