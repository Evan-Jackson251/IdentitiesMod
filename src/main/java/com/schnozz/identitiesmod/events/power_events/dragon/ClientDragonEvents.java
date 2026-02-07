package com.schnozz.identitiesmod.events.power_events.dragon;

import com.schnozz.identitiesmod.IdentitiesMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientDragonEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

    }
}
