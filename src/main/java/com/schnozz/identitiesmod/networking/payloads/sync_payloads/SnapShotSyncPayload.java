package com.schnozz.identitiesmod.networking.payloads.sync_payloads;

import com.schnozz.identitiesmod.util.EntitySnapshot;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SnapShotSyncPayload(EntitySnapshot snapShot) implements CustomPacketPayload {
    public static final Type<SnapShotSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "snap_shot_sync_payload"));

    public static final StreamCodec<ByteBuf, SnapShotSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    EntitySnapshot.STREAM_CODEC,
                    SnapShotSyncPayload::snapShot,
                    SnapShotSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}