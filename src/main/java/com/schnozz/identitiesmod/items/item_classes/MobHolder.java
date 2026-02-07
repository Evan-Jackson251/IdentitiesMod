package com.schnozz.identitiesmod.items.item_classes;

import com.schnozz.identitiesmod.datacomponent.ChargeRecord;
import com.schnozz.identitiesmod.datacomponent.CompoundTagListRecord;
import com.schnozz.identitiesmod.datacomponent.ModDataComponentRegistry;
import com.schnozz.identitiesmod.entities.custom_entities.ThrownMobHolder;
import com.schnozz.identitiesmod.leveldata.UUIDSavedData;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.extensions.IItemExtension;

import java.util.ArrayList;
import java.util.List;

public class MobHolder extends Item  implements IItemExtension {

    private static UUIDSavedData command_list;


    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {

        tooltipComponents.add(Component.literal("Charges: " + stack.getOrDefault(ModDataComponentRegistry.CHARGE, new ChargeRecord(0)).charge()));
        tooltipComponents.add(Component.literal("Entities: " + stack.getOrDefault(ModDataComponentRegistry.HELD_LIST, new CompoundTagListRecord(new ArrayList<>())).entries().size()));
    }

    public MobHolder(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand){
        if(!player.level().isClientSide && player.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer"))
        {
            command_list = UUIDSavedData.get(player.level().getServer());
            if(command_list.getUUIDList().contains(target.getUUID()))
            {
                command_list.removeUUID(target.getUUID());
                CompoundTag tag = new CompoundTag();
                player.getMainHandItem().update(ModDataComponentRegistry.HELD_LIST, new CompoundTagListRecord(List.of()), listRecord -> {
                    List<CompoundTag> updated = new ArrayList<>(listRecord.entries());
                    target.save(tag);
                    updated.add(tag);
                    return new CompoundTagListRecord(List.copyOf(updated));
                });
                System.out.println(player.getMainHandItem().get(ModDataComponentRegistry.HELD_LIST).entries().size());
                target.remove(Entity.RemovalReason.DISCARDED);
                command_list.addUUID(tag.getUUID("UUID"));

            }
        }
        return InteractionResult.SUCCESS;


    }

    @Override
    public void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        if(itemEntity.level().isClientSide) return;
        command_list = UUIDSavedData.get(itemEntity.level().getServer());
        int x = itemEntity.getItem().get(ModDataComponentRegistry.HELD_LIST).entries().size();
        x = x*1;
        for(int i = 0; i < x; i++)
        {

            if(itemEntity.getItem().get(ModDataComponentRegistry.HELD_LIST).entries().get(i).hasUUID("UUID"))
            {
                command_list.removeUUID(itemEntity.getItem().get(ModDataComponentRegistry.HELD_LIST).entries().get(i).getUUID("UUID"));
            }

        }

        super.onDestroyed(itemEntity, damageSource);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENDER_PEARL_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        player.getCooldowns().addCooldown(this, 20);
        if (!level.isClientSide && player.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer") &&  player.getMainHandItem().getOrDefault(ModDataComponentRegistry.CHARGE, new ChargeRecord(0)).charge() > 0) {
            ThrownMobHolder thrownMobHolder = new ThrownMobHolder(level, player);
            thrownMobHolder.setItem(itemstack);
            thrownMobHolder.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(thrownMobHolder);
            player.getMainHandItem().set(ModDataComponentRegistry.CHARGE, new ChargeRecord(player.getMainHandItem().getOrDefault(ModDataComponentRegistry.CHARGE, new ChargeRecord(0)).charge() - 1));
        }
        return InteractionResultHolder.success(itemstack);
    }



    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        List<CompoundTag> heldEntities = new ArrayList<>(context.getItemInHand().getOrDefault(ModDataComponentRegistry.HELD_LIST.get(), new CompoundTagListRecord(new ArrayList<>())).entries());
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if(!heldEntities.isEmpty() && player.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer"))
        {
            addEntity(player, pos);
        }
        return InteractionResult.SUCCESS;
    }








    public void addEntity(Player player, BlockPos pos)
    {
        if(!player.level().isClientSide)
        {
            List<CompoundTag> heldEntities = new ArrayList<>(player.getMainHandItem().getOrDefault(ModDataComponentRegistry.HELD_LIST.get(), new CompoundTagListRecord(new ArrayList<>())).entries());
            command_list = UUIDSavedData.get(player.level().getServer());
            CompoundTag tag = heldEntities.removeLast();
            System.out.println(heldEntities.size());
            player.getMainHandItem().set(ModDataComponentRegistry.HELD_LIST, new CompoundTagListRecord(List.copyOf(heldEntities)));
            ResourceLocation entityId = ResourceLocation.tryParse(tag.getString("id"));
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
            Entity entity = type.create(player.level());
            entity.load(tag);
            entity.moveTo(pos.getX(), pos.getY() + 2, pos.getZ(), player.getXRot(), player.getYRot());
            if(!command_list.getUUIDList().contains(entity.getUUID()))
            {
                command_list.addUUID(entity.getUUID());
            }
            player.level().addFreshEntity(entity);


        }
    }

}
