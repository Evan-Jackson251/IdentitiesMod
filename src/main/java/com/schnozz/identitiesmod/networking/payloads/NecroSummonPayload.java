package com.schnozz.identitiesmod.networking.payloads;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerSummons;
import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerTeam;
import com.schnozz.identitiesmod.leveldata.UUIDSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NecroSummonPayload(int userID) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NecroSummonPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "necro_summon_payload"));

    public static final StreamCodec<ByteBuf, NecroSummonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            NecroSummonPayload::userID,
            NecroSummonPayload::new
    );


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NecroSummonPayload payload, IPayloadContext context)
    {
        Player player = context.player();
        MinecraftServer server = player.level().getServer();
        if(server == null) return;
        UUIDSavedData list  = UUIDSavedData.get(server);

        //checks team before summoning
        NecromancerTeam team = player.getData(ModDataAttachments.NECROMANCER_TEAM);
        if(team.getMembers().isEmpty()) return;

        NecromancerSummons.nextStored(team).ifPresent(member -> NecromancerSummons.summon((ServerPlayer) player, member));




    }
}
