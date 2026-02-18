package com.schnozz.identitiesmod.networking.payloads;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record KillPayload(int entityID) implements CustomPacketPayload {
    public static final Type<KillPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "kill_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, KillPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            KillPayload::entityID,
            KillPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}