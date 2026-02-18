package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetNoGravityPayload(int entityID, boolean noGravity) implements CustomPacketPayload {
    public static final Type<SetNoGravityPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "set_no_gravity_payload"));


    public static final StreamCodec<ByteBuf, SetNoGravityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SetNoGravityPayload::entityID,
            ByteBufCodecs.BOOL,
            SetNoGravityPayload::noGravity,
            SetNoGravityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}