package com.schnozz.identitiesmod.mixin;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BlockItem.class)
public abstract class SeedUseMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void customWheatSeedUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = context.getItemInHand();
        // Only override for WHEAT_SEEDS
        if (stack.is(Items.WHEAT_SEEDS)) {
            // Custom behavior here
            Player player = context.getPlayer();
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();

            if (level.isClientSide) return;
            if(player instanceof ServerPlayer serverPlayer && player.getData(ModDataAttachments.POWER_TYPE).equals("Kyle")) {


                List<BlockPos> setTillList = getBlocksToPlace(2, pos, serverPlayer);

                for (BlockPos posi : setTillList) {
                    if (serverPlayer.level().getBlockState(posi).is(Blocks.FARMLAND)) {
                        BlockPos abovePos = posi.above();
                        BlockState aboveState = level.getBlockState(abovePos);
                        boolean isEmpty = aboveState.isAir();

                        if (isEmpty && !stack.isEmpty()) {

                            level.setBlock(abovePos, Blocks.WHEAT.defaultBlockState(), 3);
                            stack.shrink(1);
                        }

                    }
                }
                cir.setReturnValue(InteractionResult.SUCCESS); // cancel vanilla and run your logic
            }
        }
    }

    private static List<BlockPos> getBlocksToPlace(int range, BlockPos initalBlockPos, ServerPlayer player) {
        List<BlockPos> positions = new ArrayList<>();

        BlockHitResult traceResult = player.level().clip(new ClipContext(player.getEyePosition(1f),
                (player.getEyePosition(1f).add(player.getViewVector(1f).scale(6f))),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if(traceResult.getType() == HitResult.Type.MISS) {
            return positions;
        }

        if(traceResult.getDirection() == Direction.DOWN || traceResult.getDirection() == Direction.UP) {
            for(int x = -range; x <= range; x++) {
                for(int y = -range; y <= range; y++) {
                    positions.add(new BlockPos(initalBlockPos.getX() + x, initalBlockPos.getY(), initalBlockPos.getZ() + y));
                }
            }
        }

        if(traceResult.getDirection() == Direction.NORTH || traceResult.getDirection() == Direction.SOUTH) {
            for(int x = -range; x <= range; x++) {
                for(int y = -range; y <= range; y++) {
                    positions.add(new BlockPos(initalBlockPos.getX() + x, initalBlockPos.getY() + y, initalBlockPos.getZ()));
                }
            }
        }

        if(traceResult.getDirection() == Direction.EAST || traceResult.getDirection() == Direction.WEST) {
            for(int x = -range; x <= range; x++) {
                for(int y = -range; y <= range; y++) {
                    positions.add(new BlockPos(initalBlockPos.getX(), initalBlockPos.getY() + y, initalBlockPos.getZ() + x));
                }
            }
        }

        return positions;
    }
}
