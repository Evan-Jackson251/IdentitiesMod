package com.schnozz.identitiesmod.events.gravity;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.damage_sources.ModDamageTypes;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ChargeSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerGravityEvents {
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
        if(event.getSource().getEntity() instanceof ServerPlayer p)
        {
            double currentCharge = p.getData(ModDataAttachments.CHARGE);
            double increase = (double)event.getAmount()/3;
            double newCharge = currentCharge + increase;
            newCharge = Math.round(newCharge*100.0)/100.0;
            if(newCharge>100){newCharge=100;}
            p.setData(ModDataAttachments.CHARGE,newCharge);
            PacketDistributor.sendToPlayer(p, new ChargeSyncPayload(newCharge));
        }
        if(event.getSource().is(DamageTypes.ARROW))
        {
            if(event.getSource().getDirectEntity().getTags().contains("Gravity Arrow")) {
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,2,false,true,true));
            }
        }
    }
}
