package com.schnozz.identitiesmod.events.power_events.necromancer;


import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.datacomponent.ChargeRecord;
import com.schnozz.identitiesmod.datacomponent.CompoundTagListRecord;
import com.schnozz.identitiesmod.datacomponent.ModDataComponentRegistry;
import com.schnozz.identitiesmod.goals.FollowEntityAtDistanceGoal;
import com.schnozz.identitiesmod.items.ItemRegistry;
import com.schnozz.identitiesmod.leveldata.KillTallySavedData;
import com.schnozz.identitiesmod.leveldata.UUIDSavedData;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerNecroEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        Entity killer = event.getSource().getEntity();
        if(killer == null) return;
        UUIDSavedData list = UUIDSavedData.get(killer.getServer());
        MinecraftServer server = killer.getServer();


        //tally goes up when a necromancers mob kills
        if(list.getOwnerOf(killer.getUUID()).isPresent())
        {
            ServerPlayer p = server.getPlayerList().getPlayer(list.getOwnerOf(killer.getUUID()).get());
            KillTallySavedData.get(p.server).addKill(p.getUUID(), victim);
            return;
        }

        //tally goes up when he gets a kill
        if (!(killer instanceof ServerPlayer player)) return;
        if (!"Necromancer".equals(player.getData(ModDataAttachments.POWER_TYPE))) return;

        KillTallySavedData.get(player.server).addKill(player.getUUID(), victim);
    }

    //puts away team when you log off
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if(player.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer"))
            {
                NecromancerSummons.despawnAll(player);
            }
        }
    }


    //checks if these were necromancer mobs and cancels their drops
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event)
    {
        UUIDSavedData list = UUIDSavedData.get(event.getEntity().getServer());
        if(list.getOwnerOf(event.getEntity().getUUID()).isPresent())
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onXpDrop(LivingExperienceDropEvent event)
    {
        UUIDSavedData list = UUIDSavedData.get(event.getEntity().getServer());
        if(list.getOwnerOf(event.getEntity().getUUID()).isPresent())
        {
            event.setCanceled(true);
        }
    }


    // checking for times when necromancer entities shouldnt hurt each other
    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event)
    {
        if(event.getEntity().level().isClientSide) {return;}

        MinecraftServer server = event.getEntity().getServer();
        UUIDSavedData list = UUIDSavedData.get(server);

        Entity victim = event.getEntity();
        Entity attacker = event.getSource().getDirectEntity();

        if(attacker == null) return;

        Optional<UUID> victimOwner = list.getOwnerOf(victim.getUUID());
        Optional<UUID> attackerOwner = list.getOwnerOf(attacker.getUUID());

        //checks to make sure necromancer cant be hurt by army
        if (victimOwner.isPresent() && victimOwner.get().equals(attacker.getUUID())) {
            event.setCanceled(true);
        }
        if (attackerOwner.isPresent() && attackerOwner.get().equals(victim.getUUID())) {
            event.setCanceled(true);
        }
        if (victimOwner.isPresent() && victimOwner.equals(attackerOwner)) {
            event.setCanceled(true);
        }


    }

}
