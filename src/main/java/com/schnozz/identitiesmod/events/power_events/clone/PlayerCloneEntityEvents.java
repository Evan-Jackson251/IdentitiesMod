package com.schnozz.identitiesmod.events.power_events.clone;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.entities.ModEntities;
import com.schnozz.identitiesmod.entities.custom_entities.PlayerCloneEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.MOD)

public class PlayerCloneEntityEvents {
    @SubscribeEvent
    public static void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.PLAYER_CLONE.get(), PlayerCloneEntity.createAttributes().build());
    }
}