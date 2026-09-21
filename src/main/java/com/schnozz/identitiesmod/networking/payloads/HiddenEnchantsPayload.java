package com.schnozz.identitiesmod.networking.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;

public record HiddenEnchantsPayload(int containerId, List<List<EnchantmentInstance>> options) implements CustomPacketPayload {
    public static final Type<HiddenEnchantsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "hidden_enchants_payload"));

    // One enchantment: its registry holder and level.
    private static final StreamCodec<
            RegistryFriendlyByteBuf, EnchantmentInstance
            > ENCHANTMENT_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT),
            instance -> instance.enchantment,
            ByteBufCodecs.VAR_INT,
            instance -> instance.level,
            EnchantmentInstance::new
    );

    // All enchantments for one table option.
    private static final StreamCodec<
            RegistryFriendlyByteBuf, List<EnchantmentInstance>
            > OPTION_CODEC =
            ENCHANTMENT_CODEC.apply(ByteBufCodecs.list());

    // The three table options; 3 is the maximum list size.
    private static final StreamCodec<
            RegistryFriendlyByteBuf, List<List<EnchantmentInstance>>
            > OPTIONS_CODEC =
            OPTION_CODEC.apply(ByteBufCodecs.list(3));

    public static final StreamCodec<RegistryFriendlyByteBuf, HiddenEnchantsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            HiddenEnchantsPayload::containerId,
            OPTIONS_CODEC,
            HiddenEnchantsPayload:: options,
            HiddenEnchantsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}