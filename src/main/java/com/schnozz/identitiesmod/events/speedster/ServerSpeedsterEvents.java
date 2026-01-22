package com.schnozz.identitiesmod.events.speedster;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerSpeedsterEvents {
    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event)
    {
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
    }
}
