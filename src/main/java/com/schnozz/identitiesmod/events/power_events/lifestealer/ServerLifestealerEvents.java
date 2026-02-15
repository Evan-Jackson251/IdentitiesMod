package com.schnozz.identitiesmod.events.power_events.lifestealer;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerLifestealerEvents {
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event)
    {
        if(event.getEntity().level().isClientSide){return;}

        //for when lifestealer kills the player gain 1 heart
        if(event.getEntity() instanceof Player && event.getSource().getDirectEntity() != null && event.getSource().getDirectEntity() instanceof ServerPlayer player && player.getData(ModDataAttachments.POWER_TYPE).equals("Lifestealer")){
                int h = Math.min(10, player.getData(ModDataAttachments.HEALTH_NEEDED) + 2);
                player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20 + h);
                player.setData(ModDataAttachments.HEALTH_NEEDED, h);
        }

        // for when lifestealer dies to a player lose 1 heart
        if(event.getEntity() instanceof ServerPlayer player && player.getData(ModDataAttachments.POWER_TYPE).equals("Lifestealer")) {

            if (event.getSource().getDirectEntity() != null && event.getSource().getDirectEntity() instanceof Player p) {
                int h = Math.max(-4, player.getData(ModDataAttachments.HEALTH_NEEDED) - 2);
                player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20 + h);
                player.setData(ModDataAttachments.HEALTH_NEEDED, h);

            }
        }
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event)
    {
        if(event.isWasDeath() && event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Lifestealer"))
        {
            Player lifeStealer = event.getEntity();
            lifeStealer.setData(ModDataAttachments.LIFESTEALER_BUFFS,event.getOriginal().getData(ModDataAttachments.LIFESTEALER_BUFFS));
        }
    }
    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof ServerPlayer player && player.getData(ModDataAttachments.POWER_TYPE).equals("Lifestealer") && (event.getItemStack().getItem() == Items.POTION || event.getItemStack().getItem() == Items.SPLASH_POTION || event.getItemStack().getItem() == Items.LINGERING_POTION))
        {
            event.setCanceled(true);
        }
    }
}
