package com.schnozz.identitiesmod.events.power_events.immortal;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.PotionLevelPayload;
import com.schnozz.identitiesmod.networking.payloads.PotionTogglePayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.LivesSyncPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.network.PacketDistributor;


@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerImmortalEvent {
    //ADD SOUNDS
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event)
    {
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Immortal"))
        {
            ServerPlayer immortalPlayer = (ServerPlayer)event.getEntity();
            if(immortalPlayer.getData(ModDataAttachments.LIVES) > 0)
            {
                event.setCanceled(true);
                livesLogic(immortalPlayer);

                immortalPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING,60,0));
                PacketDistributor.sendToPlayer(immortalPlayer,new PotionLevelPayload(MobEffects.GLOWING,0, 60));

                if(immortalPlayer.getData(ModDataAttachments.LIVES) == 1)//resistance
                {
                    PacketDistributor.sendToServer(new PotionTogglePayload(MobEffects.FIRE_RESISTANCE,0));
                    immortalPlayer.setHealth(10F);
                    immortalPlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10F);
                }
                else if(immortalPlayer.getData(ModDataAttachments.LIVES) == 2)//ice
                {
                    immortalPlayer.setHealth(10F);
                    immortalPlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10F);
                }
                else if(immortalPlayer.getData(ModDataAttachments.LIVES) == 3)//darkness
                {
                    immortalPlayer.setHealth(10F);
                    immortalPlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10F);
                }

                immortalPlayer.setData(ModDataAttachments.LIVES,immortalPlayer.getData(ModDataAttachments.LIVES)-1);
                PacketDistributor.sendToPlayer(immortalPlayer,new LivesSyncPayload(immortalPlayer.getData(ModDataAttachments.LIVES)));
                immortalPlayer.clearFire();
            }
            else{
                immortalPlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(17F);
            }
        }
    }
    //resets max health and lives
    @SubscribeEvent
    public static void onRest(SleepFinishedTimeEvent event)
    {
        ServerLevel level = (ServerLevel)event.getLevel();
        for (Entity entity : level.getAllEntities()) { // Or level.getEntities()
            if (entity instanceof Player immortalPlayer && immortalPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Immortal")) {
                immortalPlayer.removeAllEffects();
                immortalPlayer.setData(ModDataAttachments.LIVES,3);
                PacketDistributor.sendToPlayer((ServerPlayer)immortalPlayer,new LivesSyncPayload(immortalPlayer.getData(ModDataAttachments.LIVES)));

                immortalPlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20F);
                immortalPlayer.setHealth(20F);
            }
        }
    }
    @SubscribeEvent
    public static void livingDamage(LivingDamageEvent.Pre event)
    {
        LivingEntity target = event.getEntity();
        if(event.getSource().getEntity() instanceof Player immortalPlayer && immortalPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Immortal"))
        {
            if(immortalPlayer.getData(ModDataAttachments.LIVES) == 2){
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS,50,2));
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER,60,0));
            }
            else if(immortalPlayer.getData(ModDataAttachments.LIVES) == 1){
                target.setTicksFrozen(target.getTicksFrozen()+80);
            }
        }
    }
    //login sync
    @SubscribeEvent
    public static void playerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Immortal") && event.getEntity() instanceof ServerPlayer immortalPlayer)
        {
            if(immortalPlayer.getData(ModDataAttachments.LIVES) == 1)
            {
                //server side effects
                immortalPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,MobEffectInstance.INFINITE_DURATION,0));
                //client payloads
                PacketDistributor.sendToPlayer(immortalPlayer,new PotionLevelPayload(MobEffects.FIRE_RESISTANCE,0, MobEffectInstance.INFINITE_DURATION));
            }
            PacketDistributor.sendToPlayer(immortalPlayer,new LivesSyncPayload(immortalPlayer.getData(ModDataAttachments.LIVES)));
        }
    }
    //stops totem usage
    @SubscribeEvent
    public static void onItemUse(LivingUseTotemEvent event) {
        Entity entity = event.getEntity();
        if(entity.getData(ModDataAttachments.POWER_TYPE).equals("Immortal")) {
            event.setCanceled(true);
        }
    }

    public static void livesLogic(ServerPlayer immortalPlayer)
    {
        if(immortalPlayer.getData(ModDataAttachments.LIVES) == 1)
        {
            //server side effects
            immortalPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,MobEffectInstance.INFINITE_DURATION,2));
            immortalPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,MobEffectInstance.INFINITE_DURATION,0));
            immortalPlayer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,MobEffectInstance.INFINITE_DURATION,2));
            //client payloads
            PacketDistributor.sendToPlayer(immortalPlayer,new PotionLevelPayload(MobEffects.MOVEMENT_SLOWDOWN,2, MobEffectInstance.INFINITE_DURATION));
            PacketDistributor.sendToPlayer(immortalPlayer,new PotionLevelPayload(MobEffects.WEAKNESS,0, MobEffectInstance.INFINITE_DURATION));
            PacketDistributor.sendToPlayer(immortalPlayer,new PotionLevelPayload(MobEffects.DAMAGE_RESISTANCE,2, MobEffectInstance.INFINITE_DURATION));
        }
        if(immortalPlayer.getData(ModDataAttachments.LIVES) == 2)
        {
            //server side effects
            immortalPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,MobEffectInstance.INFINITE_DURATION,0));
            //client payloads
            PacketDistributor.sendToPlayer(immortalPlayer,new PotionLevelPayload(MobEffects.FIRE_RESISTANCE,0, MobEffectInstance.INFINITE_DURATION));
        }
        if(immortalPlayer.getData(ModDataAttachments.LIVES) == 3)
        {
            //server side effects
            immortalPlayer.addEffect(new MobEffectInstance(MobEffects.SATURATION,100,0));
            //client payloads
            PacketDistributor.sendToPlayer(immortalPlayer,new PotionLevelPayload(MobEffects.SATURATION,0, 100));
        }
    }
}
