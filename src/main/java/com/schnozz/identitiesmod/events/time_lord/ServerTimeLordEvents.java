package com.schnozz.identitiesmod.events.time_lord;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.damage_sources.ModDamageTypes;
import com.schnozz.identitiesmod.networking.payloads.EntityDamagePayload;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerTimeLordEvents {
    private static boolean timeStopped = false;
    private static int stopperID = -1;

    public static Map<Integer, Float> Time_Stop_Damage = new HashMap<>();
    //stops all entites but time lord
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event)
    {
        if(event.getEntity().getData(ModDataAttachments.TIME_STOP_STATE) == 1)
        {
            timeStopped = true;
            stopperID = event.getEntity().getId();
        }
        if(event.getEntity().getId() == stopperID)
        {
            if(event.getEntity().getData(ModDataAttachments.TIME_STOP_STATE) == 0)
            {
                timeStopped = false;
                stopperID = -1;
                timeStopDamage(event.getEntity().level(), stopperID);
            }
        }
        else if(timeStopped)
        {
            if (event.getEntity() instanceof Player player) {
                player.setDeltaMovement(0,0,0);
                player.hurtMarked = true;
            }
            event.getEntity().setYRot(event.getEntity().yRotO);
            event.getEntity().setXRot(event.getEntity().xRotO);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event)
    {
        if(timeStopped && event.getEntity().getId() != stopperID){
            if(Time_Stop_Damage.get(event.getEntity().getId()) != null)
            {
                Time_Stop_Damage.put(event.getEntity().getId(),event.getAmount() + Time_Stop_Damage.get(event.getEntity().getId()));
            }
            else {
                Time_Stop_Damage.put(event.getEntity().getId(),event.getAmount());
            }
            event.setCanceled(true);
        }
    }

    public static void timeStopDamage(Level level, int timeID)
    {
        for(Map.Entry<Integer,Float> damage: Time_Stop_Damage.entrySet())
        {
            Holder<DamageType> damageTypeHolder =
                    level.registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(ModDamageTypes.TIME_DAMAGE);
            Entity hurtEntity = level.getEntity(damage.getKey());
            Entity timeEntity = level.getEntity(timeID);
            DamageSource dSource = new DamageSource(damageTypeHolder,timeEntity,hurtEntity);

            hurtEntity.hurt(dSource,damage.getValue());

        }
        Time_Stop_Damage.clear();
    }
}
