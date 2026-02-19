package com.schnozz.identitiesmod.events.power_events.dragon;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerDragonEvents {
    //while dragon shifted, cannot use items
    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        Entity entity = event.getEntity();
        if(entity.getData(ModDataAttachments.POWER_TYPE).equals("Dragon") && entity.isPassenger() && entity.getVehicle() instanceof DragonEntity) {
            event.setCanceled(true);
        }
    }
    //no damage if player attacks while dragon shifted
    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if(event.getSource().getEntity() instanceof Player dragonPlayer && dragonPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Dragon"))
        {
            if(dragonPlayer.isPassenger() && dragonPlayer.getVehicle() instanceof DragonEntity) {
                event.setCanceled(true);
            }
        }

    }
}
