package com.schnozz.identitiesmod.networking.payloads.sync_payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpeedsterLightningSync(int state) implements CustomPacketPayload {
    public static final Type<SpeedsterLightningSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "speedster_lightning_sync"));

    public static final StreamCodec<ByteBuf, SpeedsterLightningSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SpeedsterLightningSync::state,
            SpeedsterLightningSync::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

