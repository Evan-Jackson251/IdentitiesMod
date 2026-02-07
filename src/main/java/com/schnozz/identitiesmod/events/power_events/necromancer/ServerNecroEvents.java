package com.schnozz.identitiesmod.events.power_events.necromancer;


import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.datacomponent.ChargeRecord;
import com.schnozz.identitiesmod.datacomponent.CompoundTagListRecord;
import com.schnozz.identitiesmod.datacomponent.ModDataComponentRegistry;
import com.schnozz.identitiesmod.goals.FollowEntityAtDistanceGoal;
import com.schnozz.identitiesmod.items.ItemRegistry;
import com.schnozz.identitiesmod.leveldata.UUIDSavedData;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerNecroEvents {





    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event)
    {
        if(event.getEntity().level().isClientSide) {return;}
        MinecraftServer server = event.getEntity().getServer();
        UUIDSavedData command_list = UUIDSavedData.get(server);



        if(event.getEntity().level() instanceof ServerLevel level && event.getEntity() instanceof Monster monster && event.getSource().getDirectEntity() instanceof Player p && p.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer") && command_list.getUUIDList().size() < 10 && !command_list.getUUIDList().contains(monster.getUUID()))
        {
            CompoundTag oldData = new CompoundTag();
            monster.save(oldData);
            oldData.remove("UUID");
            oldData.remove("Health");

            Entity entity = monster.getType().create(level);
            if(entity instanceof Monster newMonster)
            {
                newMonster.load(oldData);
                newMonster.setPos(monster.position());
                newMonster.setXRot(monster.getXRot());
                newMonster.setYRot(monster.getYRot());
                newMonster.goalSelector.removeAllGoals(wrappedGoal ->
                        wrappedGoal instanceof RandomStrollGoal
                );
                newMonster.targetSelector.removeAllGoals(goal -> true);
                newMonster.setTarget(null);
                //set new data here for entity - not chat skibidi

                command_list.addUUID(newMonster.getUUID());
                level.addFreshEntity(newMonster);
                newMonster.setCanPickUpLoot(true);
                newMonster.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMonster.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.7);
                newMonster.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(newMonster.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) * 1.2);
                newMonster.heal(500);
            }


        }

        if(event.getEntity() instanceof Monster monster && command_list.getUUIDList().contains(monster.getUUID()))
        {
            command_list.removeUUID(monster.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event)
    {
        if(event.getEntity().level().isClientSide) {return;}
        MinecraftServer server = event.getEntity().getServer();
        UUIDSavedData command_list = UUIDSavedData.get(server);

        if(((event.getEntity() instanceof Player p && p.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer")) || (event.getEntity() instanceof Monster m && command_list.getUUIDList().contains(m.getUUID()))) && event.getSource().getDirectEntity() instanceof Monster monster && command_list.getUUIDList().contains(monster.getUUID()) )
        {
            event.setCanceled(true);
        }

        if((event.getEntity() instanceof Monster monster && command_list.getUUIDList().contains(monster.getUUID())) && ((event.getSource().getDirectEntity() instanceof Player p && p.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer")) || (event.getSource().getDirectEntity() instanceof Monster m && command_list.getUUIDList().contains(m.getUUID()))))
        {
            event.setCanceled(true);
        }

        if((event.getEntity() instanceof Monster monster && command_list.getUUIDList().contains(monster.getUUID())) && (event.getSource().getDirectEntity() instanceof AbstractArrow a && a.getOwner() instanceof Player p && p.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer")))
        {
            event.setCanceled(true);
        }

        if((event.getEntity() instanceof Monster monster && command_list.getUUIDList().contains(monster.getUUID())) && (event.getSource().getDirectEntity() instanceof AbstractArrow a && a.getOwner() instanceof Monster m && command_list.getUUIDList().contains(m.getUUID())))
        {
            event.setCanceled(true);
        }

        if(event.getSource().getDirectEntity() instanceof LivingEntity entity && (event.getEntity() instanceof Monster monster && command_list.getUUIDList().contains(monster.getUUID())) && !((event.getSource().getDirectEntity() instanceof Player p && p.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer")) || (event.getSource().getDirectEntity() instanceof Monster m && command_list.getUUIDList().contains(m.getUUID()))))
        {
            monster.setTarget(entity);
        }


    }




    @SubscribeEvent
    public static void onSetAttackTarget(LivingChangeTargetEvent event)
    {
        if(event.getEntity().level().isClientSide) {return;}
        MinecraftServer server = event.getEntity().getServer();
        UUIDSavedData command_list = UUIDSavedData.get(server);

        if(((event.getNewAboutToBeSetTarget() instanceof Player p && p.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer")) || (event.getNewAboutToBeSetTarget() instanceof Monster m && command_list.getUUIDList().contains(m.getUUID()))) && event.getEntity() instanceof Monster monster && command_list.getUUIDList().contains(monster.getUUID()) )
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if(event.getEntity().level().isClientSide) return;
        // Get the item stack that was crafted
        ItemStack craftedItem = event.getCrafting();
        // Check if the crafted item is a MobHolder
        if (craftedItem.getItem() == ItemRegistry.MOB_HOLDER.get()) {

            // Get the crafting matrix (ingredients)
            Container container = event.getInventory();

            // Check if Ender Pearl is in the crafting matrix
            boolean hasEnderPearl = false;
            int currentCharges = 0;
            CompoundTagListRecord currentRecord = null;

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.getItem() == Items.ENDER_PEARL) {
                    hasEnderPearl = true;
                    break;
                }
            }

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.getItem() == ItemRegistry.MOB_HOLDER.get()) {
                    currentCharges = stack.getOrDefault(ModDataComponentRegistry.CHARGE, new ChargeRecord(0)).charge();
                    currentRecord = stack.getOrDefault(ModDataComponentRegistry.HELD_LIST, new CompoundTagListRecord(new ArrayList<>()));
                    break;
                }
            }

            // If the recipe included an Ender Pearl, increase the charge value
            if (hasEnderPearl) {
                craftedItem.set(ModDataComponentRegistry.CHARGE, new ChargeRecord(currentCharges + 1));
                craftedItem.set(ModDataComponentRegistry.HELD_LIST, currentRecord);
            }
        }
    }


    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event)
    {
        if(event.getEntity().level().isClientSide) {return;}
        MinecraftServer server = event.getEntity().getServer();
        UUIDSavedData command_list = UUIDSavedData.get(server);

        if(event.getEntity() instanceof Monster monster &&  command_list.getUUIDList().contains(monster.getUUID()))
        {
            monster.goalSelector.removeAllGoals(wrappedGoal ->
                    wrappedGoal instanceof RandomStrollGoal
            );
            monster.targetSelector.removeAllGoals(goal -> true);
            monster.setTarget(null);
            for(Player p : server.getLevel(ServerLevel.OVERWORLD).players())
            {
                if(p.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer"))
                {
                    monster.goalSelector.addGoal(3, new FollowEntityAtDistanceGoal(monster, p,2D, 5f));
                }
            }
            //add the follow player goal and any others depending on type
        }
    }
}
