package com.schnozz.identitiesmod.events.power_events.speedster;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.PotionLevelPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerSpeedsterEvents {
    //lightning effect and immunity
    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event)
    {
        if(event.getSource().is(DamageTypes.LIGHTNING_BOLT))
        {
            System.out.println("DETECTED");
            if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Speedster"))
            {
                event.setCanceled(true);
            }
        }
        if(event.getSource().getEntity() == null)
        {
            return;
        }
        if(event.getSource().getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Speedster"))
        {
            ServerPlayer speedster = (ServerPlayer)event.getSource().getEntity();
            if(speedster.level().isClientSide()){
                return;
            }
            ServerLevel level = (ServerLevel)speedster.level();
            if(speedster.getData(ModDataAttachments.SPEEDSTER_LIGHTNING) == 1)
            {
                EntityType.LIGHTNING_BOLT.spawn(level, event.getEntity().getOnPos(), MobSpawnType.TRIGGERED);
            }
        }
        DamageSource source = event.getSource();
        DamageType type = source.type();
        String typeString = type.msgId();
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Speedster")) {
            if(typeString.equals("minecraft:player"))
            {
                event.setAmount(event.getOriginalAmount()*1.3F);
            }
        }
    }
    //Permanent effect refresh on death and spawn
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event)
    {
        if(event.getEntity().level().isClientSide) return;
        if(event.getEntity().level() instanceof ServerLevel level) {
            if (event.getOriginal().getData(ModDataAttachments.POWER_TYPE).equals("Speedster")) {
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, MobEffectInstance.INFINITE_DURATION, 2, false, true,true));
                PacketDistributor.sendToPlayer((ServerPlayer)event.getEntity(),new PotionLevelPayload(MobEffects.MOVEMENT_SPEED,2,MobEffectInstance.INFINITE_DURATION));
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, MobEffectInstance.INFINITE_DURATION, 1, false, true,true));
                PacketDistributor.sendToPlayer((ServerPlayer)event.getEntity(),new PotionLevelPayload(MobEffects.DIG_SPEED,1,MobEffectInstance.INFINITE_DURATION));
            }
        }
    }
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        Entity entity = event.getEntity();
        if(entity.getData(ModDataAttachments.POWER_TYPE).equals("Speedster")) {
            if(event.getDistance() <= 8)
            {
                event.setCanceled(true);
            }
        }
    }
}
