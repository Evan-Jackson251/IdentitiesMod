package com.schnozz.identitiesmod.blocks;

import com.mojang.serialization.MapCodec;
import com.schnozz.identitiesmod.leveldata.FarmValueSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FarmerCore extends BaseEntityBlock {
    public static final MapCodec<FarmerCore> CODEC = simpleCodec(FarmerCore::new);

    public FarmerCore(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }


    /// /// block entity stuff

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity)
    {
        if(!level.isClientSide)
        {
            if(entity instanceof ItemEntity itemEntity)
            {
                ItemStack stack = ((ItemEntity) itemEntity).getItem();

                if(stack.getItem() == Items.WHEAT)
                {
                    FarmValueSavedData data = FarmValueSavedData.get(level.getServer());
                    data.addToValue(4 * stack.getCount());
                    itemEntity.discard();
                }
                else if(stack.getItem() == Items.WHEAT_SEEDS)
                {
                    FarmValueSavedData data = FarmValueSavedData.get(level.getServer());
                    data.addToValue(1 * stack.getCount());
                    itemEntity.discard();
                }
                else if(stack.getItem() == Items.HAY_BLOCK)
                {
                    FarmValueSavedData data = FarmValueSavedData.get(level.getServer());
                    data.addToValue(36 * stack.getCount());
                    itemEntity.discard();
                }
                else if(stack.getItem() == Items.BREAD)
                {
                    FarmValueSavedData data = FarmValueSavedData.get(level.getServer());
                    data.addToValue(12 * stack.getCount());
                    itemEntity.discard();
                }

                if(FarmValueSavedData.get(level.getServer()).getValue() > 40000)
                {
                    FarmValueSavedData.get(level.getServer()).setValue(40000);
                }
            }
        }
        super.stepOn(level, pos, state, entity);
    }



    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston )
    {
        super.onRemove(state, level, pos, newState, movedByPiston);
    }


    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
