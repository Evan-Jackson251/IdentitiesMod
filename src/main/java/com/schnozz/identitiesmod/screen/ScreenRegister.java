package com.schnozz.identitiesmod.screen;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.menu.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ScreenRegister {

    @SubscribeEvent
    static void onScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.NOMICON.get(), NomiconScreen::new);
    }
}
