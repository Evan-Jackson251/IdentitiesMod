package com.schnozz.identitiesmod.events;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.keymapping.ModMappings;
import com.schnozz.identitiesmod.screen.custom.LifestealerScreen;
import com.schnozz.identitiesmod.screen.custom.PowerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientAdvancementEvents {
    private static boolean powerScreenOpen = false;
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event)
    {
        Player p = event.getEntity();
        if(p == null || !p.level().isClientSide()) return;
        String power = p.getData(ModDataAttachments.POWER_TYPE);
        if(ModMappings.POWER_SCREEN.get().consumeClick()) {
            PowerScreen newPowerScreen = new PowerScreen(Component.literal("Power Screen"));
            Minecraft.getInstance().setScreen(newPowerScreen);
            powerScreenOpen = true;
        }
    }
    public static void resetPowerScreen()
    {
        powerScreenOpen = false;
    }
}
