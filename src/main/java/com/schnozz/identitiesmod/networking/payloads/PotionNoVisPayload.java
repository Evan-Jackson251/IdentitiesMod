package com.schnozz.identitiesmod.networking.payloads;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public record PotionNoVisPayload(Holder<MobEffect> effect, int level, int duration) implements CustomPacketPayload {
    public static final Type<PotionNoVisPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "potion_no_vis_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PotionNoVisPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holder(Registries.MOB_EFFECT, ByteBufCodecs.registry(Registries.MOB_EFFECT)),
            PotionNoVisPayload::effect,
            ByteBufCodecs.INT,
            PotionNoVisPayload::level,
            ByteBufCodecs.INT,
            PotionNoVisPayload::duration,
            PotionNoVisPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}