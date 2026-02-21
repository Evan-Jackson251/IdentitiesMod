package com.schnozz.identitiesmod.events.power_events.dragon;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import com.schnozz.identitiesmod.mob_effects.ModEffects;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ChargeSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerDragonEvents {
    //while dragon shifted, cannot use items
    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        Entity entity = event.getEntity();
        if (entity.getData(ModDataAttachments.POWER_TYPE).equals("Dragon") && entity.isPassenger() && entity.getVehicle() instanceof DragonEntity) {
            event.setCanceled(true);
        }
    }

    //no damage if player attacks while dragon shifted
    //breath charge increased on damage
    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
//        if (event.getSource().getEntity() instanceof ServerPlayer dragonPlayer && dragonPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Dragon")) {
//            if (event.getSource().typeHolder().equals(DamageTypes.PLAYER_ATTACK)) {
//                if (dragonPlayer.isPassenger() && dragonPlayer.getVehicle() instanceof DragonEntity) {
//                    event.setCanceled(true);
//                }
//            } else {
//                double currentCharge = dragonPlayer.getData(ModDataAttachments.CHARGE);
//                double increase = (double) event.getAmount() / 2;
//                double newCharge = currentCharge + increase;
//                newCharge = Math.round(newCharge * 100.0) / 100.0;
//                if (newCharge > 100) {
//                    newCharge = 100;
//                }
//                dragonPlayer.setData(ModDataAttachments.CHARGE, newCharge);
//                PacketDistributor.sendToPlayer(dragonPlayer, new ChargeSyncPayload(newCharge));
//            }
//        }
        if (event.getSource().getEntity() instanceof DragonEntity dragon) {
            //return if dragon breath is dealing the damage
            if (event.getSource().is(DamageTypes.IN_FIRE)) return;
            //increase charge
            for(Entity entity: dragon.getPassengers()){
                if(entity instanceof ServerPlayer dragonPlayer && dragonPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Dragon"))
                {
                    double currentCharge = dragonPlayer.getData(ModDataAttachments.CHARGE);
                    double increase = (double) event.getAmount() / 2;
                    double newCharge = currentCharge + increase;
                    newCharge = Math.round(newCharge * 100.0) / 100.0;
                    if (newCharge > 100) {
                        newCharge = 100;
                    }
                    dragonPlayer.setData(ModDataAttachments.CHARGE, newCharge);
                    PacketDistributor.sendToPlayer(dragonPlayer, new ChargeSyncPayload(newCharge));

                    System.out.println("Charge: " + dragonPlayer.getData(ModDataAttachments.CHARGE));
                }
            }
        }
    }

    //on dragon death, debuff
//    @SubscribeEvent
//    public static void onLivingDeath(LivingDeathEvent event) {
//        if (event.getEntity() instanceof DragonEntity dragon) {
//            if (!dragon.getPassengers().isEmpty()) {
//                for (Entity entity : dragon.getPassengers()) {
//                    if (entity instanceof Player dragonPlayer && dragonPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Dragon")) {
//                        dragonPlayer.removeEffect(MobEffects.WEAKNESS);
//                        dragonPlayer.removeEffect(ModEffects.SUPER_INVISIBILITY);
//                        dragonPlayer.removeEffect(MobEffects.DAMAGE_RESISTANCE);
//
//                        MobEffectInstance slow = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 18000, 0);
//                        MobEffectInstance weak = new MobEffectInstance(MobEffects.WEAKNESS, 18000, 0);
//                        dragonPlayer.addEffect(slow, entity);
//                        dragonPlayer.addEffect(weak, entity);
//
//                        dragonPlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(6F);//reset in client
//                    }
//                }
//            }
//        }
//    }
}
