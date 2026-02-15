package com.schnozz.identitiesmod.networking.payloads.sync_payloads;

import com.schnozz.identitiesmod.attachments.AvailablePowersAttachment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AvailablePowersSyncPayload(AvailablePowersAttachment attachment) implements CustomPacketPayload {
    public static final Type<AvailablePowersSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "available_powers_sync"));

    public static final StreamCodec<ByteBuf, AvailablePowersSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(AvailablePowersAttachment.CODEC),
            AvailablePowersSyncPayload::attachment,
            AvailablePowersSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}