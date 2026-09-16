package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.networking.payloads.DripstoneDropPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class ServerDripstoneDropHandler {
    public static void handle(DripstoneDropPayload payload, IPayloadContext context)
    {
        ServerPlayer player = (ServerPlayer) context.player();
        Vec3 centerPos = payload.pos();

        BlockState dripState = Blocks.POINTED_DRIPSTONE.defaultBlockState()
                .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP)
                .setValue(PointedDripstoneBlock.WATERLOGGED, false);

        int radius = 1;
        for(int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                Vec3 tempPos = centerPos.add(i,15,j);
                BlockPos blockPos = BlockPos.containing(tempPos);
                FallingBlockEntity dripStone = FallingBlockEntity.fall(player.level(), blockPos, dripState);
                dripStone.setHurtsEntities(4.00F,60);
            }
        }

//        Entity entity = player.level().getEntity(payload.userID());
//        if(entity == null) return;
//        int radius = 3;
//        for(int i = -radius; i <= radius; i++) {
//            for(int j = -radius; j <= radius; j++) {
//                if(!((i>=-1 && i<=1)&&(j>=-1 && j<=1)))
//                {
//                    Vec3 anvilPos = entity.position().add(i, 10, j);
//                    BlockPos pos = BlockPos.containing(anvilPos);
//
//                    BlockState dripState = Blocks.POINTED_DRIPSTONE.defaultBlockState()
//                            .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP)
//                            .setValue(PointedDripstoneBlock.WATERLOGGED, false);
//
//                    FallingBlockEntity anvil = FallingBlockEntity.fall(player.level(), pos, dripState);
//                    anvil.setHurtsEntities(4.00F,40);
//                }
//            }
//        }
    }
}
