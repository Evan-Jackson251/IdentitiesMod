package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DragonSpawnPayload(int userID) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DragonSpawnPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "dragon_spawn_payload"));

    public static final StreamCodec<ByteBuf, DragonSpawnPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            DragonSpawnPayload::userID,
            DragonSpawnPayload::new
    );


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}