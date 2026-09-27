package com.schnozz.identitiesmod.networking.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HealPayload(int entityId,  float health) implements CustomPacketPayload {
    public static final Type<HealPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "heal_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HealPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            HealPayload::entityId,
            ByteBufCodecs.FLOAT,
            HealPayload::health,
            HealPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}