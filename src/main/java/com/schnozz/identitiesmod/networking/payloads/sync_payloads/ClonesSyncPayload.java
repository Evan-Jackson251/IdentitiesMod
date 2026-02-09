package com.schnozz.identitiesmod.networking.payloads.sync_payloads;

import com.schnozz.identitiesmod.attachments.AdaptationAttachment;
import com.schnozz.identitiesmod.attachments.CloneAttachment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClonesSyncPayload(CloneAttachment attachment) implements CustomPacketPayload {
    public static final Type<ClonesSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "clones_sync"));

    public static final StreamCodec<ByteBuf, ClonesSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(CloneAttachment.CODEC),
            ClonesSyncPayload::attachment,
            ClonesSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}