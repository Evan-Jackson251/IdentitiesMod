package com.schnozz.identitiesmod.networking.payloads.sync_payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;

public record PowersTakenSyncPayload(ArrayList<String> powersTaken) implements CustomPacketPayload {
    public static final Type<PowersTakenSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "powers_taken_sync"));

    public static final StreamCodec<ByteBuf, PowersTakenSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(
                    ArrayList::new,
                    ByteBufCodecs.STRING_UTF8,
                    50
            ),
            PowersTakenSyncPayload::powersTaken,
            PowersTakenSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}