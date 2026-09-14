package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EmeraldGolemSpawnPayload(int userID) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EmeraldGolemSpawnPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "emerald_golem_spawn_payload"));

    public static final StreamCodec<ByteBuf, EmeraldGolemSpawnPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            EmeraldGolemSpawnPayload::userID,
            EmeraldGolemSpawnPayload::new
    );


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}