package com.schnozz.identitiesmod.events.power_events.lifestealer;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.keymapping.ModMappings;
import com.schnozz.identitiesmod.screen.LifestealerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientScreenEvents {
    private static boolean lifeScreenOpen = false;
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event)
    {
        Player p = event.getEntity();
        if(p == null || !p.level().isClientSide() || !p.hasData(ModDataAttachments.POWER_TYPE)) return;
        String power = p.getData(ModDataAttachments.POWER_TYPE);
        if(power.equals("Lifestealer") && !lifeScreenOpen && ModMappings.LIFESTEALER_MAPPING.get().consumeClick()) {
            LifestealerScreen newLifeScreen = new LifestealerScreen(Component.literal("Lifestealer Screen"));
            Minecraft.getInstance().setScreen(newLifeScreen);
            lifeScreenOpen = true;
        }
    }

    public static void resetLifeScreen()
    {
        lifeScreenOpen = false;
    }

}

