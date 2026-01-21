package com.schnozz.identitiesmod.events.gravity;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.damage_sources.ModDamageTypes;
import com.schnozz.identitiesmod.IdentitiesMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerGravityEvents {
    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        DamageSource source = event.getEntity().getLastDamageSource();
        if(source == null) return;
        if (source.is(ModDamageTypes.GRAVITY_POWER_DAMAGE)) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        Entity entity = event.getEntity();
        if(entity.getData(ModDataAttachments.POWER_TYPE).equals("Gravity")) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        Entity entity = event.getEntity();
        if(entity.getData(ModDataAttachments.POWER_TYPE).equals("Gravity") && event.getItemStack().getItem() == Items.ENDER_PEARL) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event)
    {
        if(event.getSource().getDirectEntity() instanceof Player gravityPlayer && gravityPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Gravity"))
        {
            long currentTime = gravityPlayer.level().getGameTime();
            if(gravityPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "pull_strength_cd"), currentTime))
            {
                if(event.getSource().type().msgId().equals("player"))
                {
                    event.setAmount(event.getAmount()+2.5F);
                }
            }
        }
        if(event.getSource().getDirectEntity() != null && event.getSource().getEntity() instanceof Player gravityPlayer && gravityPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Gravity"))
        {
            long currentTime = gravityPlayer.level().getGameTime();
            if(gravityPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "push_strength_cd"), currentTime))
            {
                if(event.getSource().type().msgId().equals("arrow"))
                {
                    event.setAmount(event.getAmount()+2.5F);
                }
            }
        }
    }
}
