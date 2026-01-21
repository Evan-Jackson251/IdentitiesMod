package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WeaknessEffectPayload(int targetId, int duration) implements CustomPacketPayload{
    public static final CustomPacketPayload.Type<WeaknessEffectPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "weakness_effect_payload"));

    public static final StreamCodec<ByteBuf, WeaknessEffectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            WeaknessEffectPayload::targetId,
            ByteBufCodecs.VAR_INT,
            WeaknessEffectPayload::duration,
            WeaknessEffectPayload::new
    );


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}