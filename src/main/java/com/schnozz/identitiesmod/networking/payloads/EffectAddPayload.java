package com.schnozz.identitiesmod.networking.payloads;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public record EffectAddPayload(int id,Holder<MobEffect> effect, int level, int duration) implements CustomPacketPayload {
    public static final Type<EffectAddPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "effect_add_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EffectAddPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            EffectAddPayload::id,
            ByteBufCodecs.holder(Registries.MOB_EFFECT, ByteBufCodecs.registry(Registries.MOB_EFFECT)),
            EffectAddPayload::effect,
            ByteBufCodecs.INT,
            EffectAddPayload::level,
            ByteBufCodecs.INT,
            EffectAddPayload::duration,
            EffectAddPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}