package com.schnozz.identitiesmod.events.power_events.adaptation;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.AdaptationAttachment;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.AdaptationSyncPayload;
import com.schnozz.identitiesmod.sounds.ModSounds;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerAdaptationEvents {
    private static final float NO_CAP = 0.00F;
    private static final float EXPLOSION_CAP = 0.1F;
    private static final float MOB_CAP = 0.25F;
    private static final float DEFAULT_CAP = 0.4F;
    private static final float UNADAPT_CAP = 2.0F;

    private static final float HEAT_ADAPT_DEGREE = 0.01F;
    private static final float EXPLOSION_ADAPT_DEGREE = 0.13F;

    private static boolean changedValue;

    @SubscribeEvent
    public static void onEntityDamage(LivingIncomingDamageEvent event) {

        if(event.getEntity().isDeadOrDying()){return;} //make sure it fixed error

        Entity entity = event.getEntity();
        DamageSource source = event.getSource();

        String damageSourceString = source.getMsgId();
        damageSourceString = damageSourceString.toLowerCase();
        ResourceLocation sourceLocation = ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,damageSourceString);

        if(entity.getData(ModDataAttachments.POWER_TYPE).equals("Adaptation") && entity.getData(ModDataAttachments.ADAPTION).getAdaptationValue(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"offensive")) == 1 && event.getEntity().level() instanceof ServerLevel serverLevel) {
            changedValue = false;

            DustColorTransitionOptions particle = new DustColorTransitionOptions(
                    new Vector3f(0.4f, 0.1f, 0.9f),  // From: purple (RGB 0–1)
                    new Vector3f(0.3f, 0.8f, 0.9f),  // To: teal-ish
                    1.0f);

            Player adapter = (Player) entity;
            boolean alreadyAdapted = false;

            String[] heatIds = adapter.getData(ModDataAttachments.ADAPTION).heatSourceMessageIds;
            String[] dotIds = adapter.getData(ModDataAttachments.ADAPTION).dotSourceMessageIds;
            String[] exploIds = adapter.getData(ModDataAttachments.ADAPTION).explosionSourceMessageIds;

            if(zeroDamage(adapter,damageSourceString,event)){event.setCanceled(true);}

            for(String id: heatIds)
            {
                if(damageSourceString.equals(id))
                {
                    damageCorrect(adapter,sourceLocation,event);
                    groupSourcesAdapt(adapter, heatIds, NO_CAP, HEAT_ADAPT_DEGREE);
                    alreadyAdapted = true;
                    particle = new DustColorTransitionOptions(
                            new Vector3f(1.0f, 0.0f, 0.0f),
                            new Vector3f(1.0f, 1.0f, 0.0f),
                            1.0f);
                }
            }
            for(String id: dotIds)
            {
                if(damageSourceString.equals(id))
                {
                    damageCorrect(adapter,sourceLocation,event);
                    groupSourcesAdapt(adapter, heatIds, DEFAULT_CAP, adapter.getData(ModDataAttachments.ADAPTION).adaptationDegree);
                    alreadyAdapted = true;
                    particle = new DustColorTransitionOptions(
                            new Vector3f(1.0f, 0.1f, 0.0f),
                            new Vector3f(0.0f, 0.1f, 1.0f),
                            1.0f);
                }
            }
            for(String id: exploIds)
            {
                if(damageSourceString.equals(id))
                {
                    damageCorrect(adapter,sourceLocation,event);
                    groupSourcesAdapt(adapter, heatIds, EXPLOSION_CAP, EXPLOSION_ADAPT_DEGREE);
                    alreadyAdapted = true;
                    particle = new DustColorTransitionOptions(
                            new Vector3f(0.0f, 0.0f, 0.0f),
                            new Vector3f(1.0f, 1.0f, 1.0f),
                            1.0f);
                }
            }
            if(damageSourceString.equals("lava"))
            {
                damageCorrect(adapter,sourceLocation,event);
                decreaseAdaptValue(adapter,damageSourceString, NO_CAP, HEAT_ADAPT_DEGREE);
                alreadyAdapted = true;
                particle = new DustColorTransitionOptions(
                        new Vector3f(1.0f, 0.0f, 0.0f),
                        new Vector3f(1.0f, 1.0f, 0.0f),
                        1.0f);
            }
            if(damageSourceString.equals("drown"))
            {
                damageCorrect(adapter,sourceLocation,event);
                decreaseAdaptValue(adapter,damageSourceString, NO_CAP);
                alreadyAdapted = true;
            }
            if(damageSourceString.equals("mob"))
            {
                damageCorrect(adapter,sourceLocation,event);
                decreaseAdaptValue(adapter,damageSourceString, MOB_CAP);
                alreadyAdapted = true;
                particle = new DustColorTransitionOptions(
                        new Vector3f(0.0f, 1.0f, 0.0f),
                        new Vector3f(0.0f, 0.6f, 0.6f),
                        1.0f);
            }
            if(!alreadyAdapted) //default adaptation to singular source with default cap
            {
                damageCorrect(adapter,sourceLocation,event);
                decreaseAdaptValue(adapter,damageSourceString, DEFAULT_CAP);
            }
            if(changedValue)
            {
                serverLevel.playSound(null, adapter.getOnPos(), ModSounds.ADAPTATION_SOUND.get(), SoundSource.PLAYERS);
                serverLevel.sendParticles(
                        particle,  // Potion-like particle
                        adapter.getX(), adapter.getY() + 1.2, adapter.getZ(), // Position
                        60, // count
                        1, 1, 1,
                        0.2 // how far they go
                );
            }


        }
        else if(entity.getData(ModDataAttachments.POWER_TYPE).equals("Adaptation"))
        {
            event.setAmount(event.getAmount()+1.5F);
        }
        else if(event.getSource().getDirectEntity() != null && event.getSource().getDirectEntity() instanceof ServerPlayer adapter && adapter.getData(ModDataAttachments.POWER_TYPE).equals("Adaptation"))
        {
            if(adapter.getData(ModDataAttachments.ADAPTION).getAdaptationValue(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"offensive")) == 0)
            {
                float value = 1 - adapter.getData(ModDataAttachments.ADAPTION).getAdaptationValue(sourceLocation);
                event.setAmount(event.getAmount()+(value*4F));
            }
        }
    }
    //decrease or increase damage taken by adapter based on the adaptation value
    private static void damageCorrect(Player adapter, ResourceLocation sourceLocation, LivingIncomingDamageEvent event)
    {
        float damagePercent = adapter.getData(ModDataAttachments.ADAPTION).getAdaptationValue(sourceLocation);
        float amount = event.getAmount()*damagePercent;
        event.setAmount(amount);
    }

    //return true if damage taken by adapter will be zero after adaptation value is applied
    private static boolean zeroDamage(Player adapter, String sourceString, LivingIncomingDamageEvent event)
    {
        ResourceLocation sourceLocation = ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,sourceString);
        float damageChange = adapter.getData(ModDataAttachments.ADAPTION).getAdaptationValue(sourceLocation);
        return damageChange == 0.00F;
    }
    //increments adaptation value of a source by adaptation degree on damage taken
    private static void decreaseAdaptValue(Player adapter, String sourceString, float cap)
    {
        ResourceLocation sourceLocation = ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,sourceString);
        float adaptDegree = adapter.getData(ModDataAttachments.ADAPTION).adaptationDegree;
        float newAdaptationValue = adapter.getData(ModDataAttachments.ADAPTION).getAdaptationValue(sourceLocation);
        if((newAdaptationValue-adaptDegree)>=cap)
        {
            newAdaptationValue -= adaptDegree;
            increaseAdaptationValue(adapter,sourceString, UNADAPT_CAP);
            changedValue = true;
        }
        else {
            newAdaptationValue = cap;
        }

        adapter.getData(ModDataAttachments.ADAPTION).setAdaptationValue(sourceLocation,newAdaptationValue);

        PacketDistributor.sendToPlayer((ServerPlayer)adapter, new AdaptationSyncPayload(adapter.getData(ModDataAttachments.ADAPTION)));
    }
    //override of changeAdaptValue with a different adaptation degree
    private static void decreaseAdaptValue(Player adapter, String sourceString, float cap, float customAdaptDegree)
    {
        ResourceLocation sourceLocation = ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,sourceString);
        float newAdaptationValue = adapter.getData(ModDataAttachments.ADAPTION).getAdaptationValue(sourceLocation);
        if((newAdaptationValue-customAdaptDegree)>=cap)
        {
            newAdaptationValue -= customAdaptDegree;
            increaseAdaptationValue(adapter,sourceString, UNADAPT_CAP);
            changedValue = true;
        }
        else {
            newAdaptationValue = cap;
        }
        adapter.getData(ModDataAttachments.ADAPTION).setAdaptationValue(sourceLocation,newAdaptationValue);

        PacketDistributor.sendToPlayer((ServerPlayer)adapter, new AdaptationSyncPayload(adapter.getData(ModDataAttachments.ADAPTION)));
    }
    //when adaptation value is decreased for one source, it is increased for another
    private static void increaseAdaptationValue(Player adapter, String sourceString, float cap)
    {
        String[][] importantGroups = adapter.getData(ModDataAttachments.ADAPTION).importantSourceMessageIdGroups;
        String[] idGroup = {};
        int random;
        boolean validGroup = false;
        while(!validGroup)
        {
            random = (int) (Math.random()*importantGroups.length);
            validGroup = true;
            idGroup = importantGroups[random];
            for(String id:idGroup)
            {
                if(id.equals(sourceString))
                {
                    validGroup = false;
                }
            }
        }
        groupSourcesUnadapt(adapter,idGroup,cap);
    }
    //pre:damage is taken from a damage over source
    //post:increments adaptation value by adaptation degree for every source in group
    private static void groupSourcesAdapt(Player adapter, String[] ids, float cap, float customAdaptDegree)
    {
        for(String id: ids)
        {
            decreaseAdaptValue(adapter,id,cap,customAdaptDegree);
        }
    }
    //pre:damage is taken from a damage over source
    //post:deincrements adaptation value
    private static void groupSourcesUnadapt(Player adapter, String[] ids, float cap)
    {
        ResourceLocation tempLocation;
        for(String id: ids)
        {
            tempLocation = ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,id);
            float newAdaptationValue = adapter.getData(ModDataAttachments.ADAPTION).getAdaptationValue(tempLocation);
            float adaptDegree = adapter.getData(ModDataAttachments.ADAPTION).adaptationDegree/2;
            if((newAdaptationValue+adaptDegree)<=cap)
            {
                newAdaptationValue += adaptDegree;
            }
            else {
                newAdaptationValue = cap;
            }
            adapter.getData(ModDataAttachments.ADAPTION).setAdaptationValue(tempLocation,newAdaptationValue);

            AdaptationAttachment adaptation = adapter.getData(ModDataAttachments.ADAPTION);
            PacketDistributor.sendToPlayer((ServerPlayer)adapter, new AdaptationSyncPayload(adaptation));
        }
    }
}
