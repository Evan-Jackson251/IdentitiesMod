package com.schnozz.identitiesmod.items.item_classes;


import com.schnozz.identitiesmod.datacomponent.ChargeRecord;
import com.schnozz.identitiesmod.datacomponent.ModDataComponentRegistry;
import com.schnozz.identitiesmod.leveldata.FarmValueSavedData;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public class Scythe extends SwordItem {


    public Scythe(Tier tier, Properties properties) {
        super(tier, properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {

        tooltipComponents.add(Component.literal("Bonus Damage: " + stack.getOrDefault(ModDataComponentRegistry.CHARGE, new ChargeRecord(0)).charge()));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos initPos = context.getClickedPos();
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if(!level.isClientSide && player instanceof ServerPlayer serverPlayer && player.getData(ModDataAttachments.POWER_TYPE).equals("Kyle"))
        {
            List<BlockPos> setTillList = getBlocksToTill(1, initPos, serverPlayer);

            for(BlockPos posi : setTillList)
            {
                if(!serverPlayer.level().getBlockState(posi).is(Blocks.FARMLAND))
                {
                    if(serverPlayer.level().getBlockState(posi).is(Blocks.DIRT) || serverPlayer.level().getBlockState(posi).is(Blocks.COARSE_DIRT) || serverPlayer.level().getBlockState(posi).is(Blocks.GRASS_BLOCK))
                    {
                        level.setBlock(posi, Blocks.FARMLAND.defaultBlockState(), 3);
                    }
                }
            }


        }

        return InteractionResult.SUCCESS;
    }



    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(!target.level().isClientSide && attacker.getData(ModDataAttachments.POWER_TYPE).equals("Kyle") && attacker instanceof Player player && !player.getCooldowns().isOnCooldown(this))
        {
            long farmValue = FarmValueSavedData.get(attacker.level().getServer()).getValue();
            DamageSource bonusDamageSource = new DamageSource(attacker.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.PLAYER_ATTACK),attacker,attacker,null);
            float bonusDamage = Math.min((float)(farmValue/2000),20F);

            attacker.getMainHandItem().set(ModDataComponentRegistry.CHARGE, new ChargeRecord((int) bonusDamage));
            AABB hurtBox = (new AABB(attacker.position(), target.position())).inflate(3,1,1.5);


            List<Entity> entities = attacker.level().getEntities(attacker, hurtBox, e -> !(e == attacker));
            for(Entity buh : entities)
            {
                buh.hurt(bonusDamageSource, (float) attacker.getMainHandItem().get(ModDataComponentRegistry.CHARGE).charge());
            }


            player.getCooldowns().addCooldown(this, 18);

        }




        return true;
    }




    public static List<BlockPos> getBlocksToTill(int range, BlockPos initalBlockPos, ServerPlayer player) {
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
