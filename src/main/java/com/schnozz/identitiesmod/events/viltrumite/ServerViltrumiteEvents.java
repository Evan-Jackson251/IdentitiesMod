package com.schnozz.identitiesmod.events.viltrumite;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.items.ItemRegistry;
import com.schnozz.identitiesmod.mob_effects.ModEffects;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.networking.payloads.CDPayload;
import com.schnozz.identitiesmod.networking.payloads.PotionLevelPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.GrabSyncPayload;
import com.schnozz.identitiesmod.sounds.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerViltrumiteEvents {

    private static final int GRAB_CD = 350;
    private static final int STUN_LENGTH = 30;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event)
    {
        if(event.getEntity().level() instanceof ServerLevel level && event.getEntity().hasData(ModDataAttachments.POWER_TYPE) && event.getEntity().getData(ModDataAttachments.POWER_TYPE.get()).equals("Viltrumite"))
        {
            if(!event.getEntity().getData(ModDataAttachments.ENTITY_HELD).isEmpty())
            {
                //sets held entity position
                Player viltrumitePlayer = event.getEntity();
                Entity target = level.getEntity(viltrumitePlayer.getData(ModDataAttachments.ENTITY_HELD).getUUID("UUID"));

                if(target == null){return;}
                if(!target.isAlive()){return;}
                //sets position in front and turns off gravity
                Vec3 targetPos = viltrumitePlayer.getEyePosition().add(viltrumitePlayer.getLookAngle().scale(1.75));
                Vec3 grabPos = new Vec3(targetPos.x, targetPos.y - 1, targetPos.z);
                target.setPos(grabPos);
                if(target instanceof ServerPlayer player)
                {
                    PacketDistributor.sendToPlayer(player, new GrabSyncPayload(grabPos));
                }
                target.setDeltaMovement(0,0,0);
                target.hurtMarked = true;

                target.setNoGravity(true);
                //sets mob aggro to viltrumite
                if(!(target instanceof Player) && target instanceof Mob mob)
                {
                    mob.setTarget(event.getEntity());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Viltrumite")) {
            Player viltrumitePlayer = event.getEntity();
            ItemStack stack = event.getItemStack();

            if(event.getEntity().level() instanceof ServerLevel level && !viltrumitePlayer.getData(ModDataAttachments.ENTITY_HELD).isEmpty())
            {
                //add stun
                LivingEntity target = (LivingEntity) level.getEntity(viltrumitePlayer.getData(ModDataAttachments.ENTITY_HELD).getUUID("UUID"));
                target.addEffect(new MobEffectInstance(ModEffects.STUN, STUN_LENGTH, 0,false,true,true));
                //let go
                target.setNoGravity(false);;
                viltrumitePlayer.setData(ModDataAttachments.ENTITY_HELD.get(), new CompoundTag());//sends an empty tag
                long startTime = level.getGameTime();
                Cooldown cd = new Cooldown(startTime, GRAB_CD);
                CooldownAttachment cdAttach = viltrumitePlayer.getData(ModDataAttachments.COOLDOWN);
                ResourceLocation key = ResourceLocation.fromNamespaceAndPath("identitiesmod", "grab_cd");
                ClientViltrumiteEvents.setIconCooldown(cd);
                cdAttach.setCooldown(key, startTime, GRAB_CD);
                PacketDistributor.sendToPlayer((ServerPlayer) viltrumitePlayer, new CooldownSyncPayload(cd,key, false  ));
            }
        }
    }

    @SubscribeEvent
    public static void onKillBread(PlayerEvent.Clone event)
    {
        if(event.getEntity().level().isClientSide) return;
        if(event.getEntity().level() instanceof ServerLevel level)
        {
            if(event.getOriginal().getData(ModDataAttachments.POWER_TYPE).equals("Viltrumite"))
            {
                if(event.isWasDeath())
                {
                    //drops held entity
                    if(event.getOriginal().hasData(ModDataAttachments.ENTITY_HELD) && event.getOriginal().getData(ModDataAttachments.ENTITY_HELD).hasUUID("UUID"))
                    {
                        Objects.requireNonNull(level.getEntity(event.getOriginal().getData(ModDataAttachments.ENTITY_HELD).getUUID("UUID"))).setNoGravity(false);
                    }
                }
                //perm strength 1
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, MobEffectInstance.INFINITE_DURATION, 0, false, true,true));
                PacketDistributor.sendToPlayer((ServerPlayer)event.getEntity(),new PotionLevelPayload(MobEffects.DAMAGE_BOOST,0,MobEffectInstance.INFINITE_DURATION));
                //perm speed 1
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, MobEffectInstance.INFINITE_DURATION, 0, false, true,true));
                PacketDistributor.sendToPlayer((ServerPlayer)event.getEntity(),new PotionLevelPayload(MobEffects.MOVEMENT_SPEED,0,MobEffectInstance.INFINITE_DURATION));
            }
        }
    }

    @SubscribeEvent
    public static void onBreadDisconnect(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if(event.getEntity().level().isClientSide) return;
        if(event.getEntity().level() instanceof ServerLevel level && event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Viltrumite"))
        {
            if(event.getEntity().hasData(ModDataAttachments.ENTITY_HELD) && event.getEntity().getData(ModDataAttachments.ENTITY_HELD).hasUUID("UUID"))
            {
                Objects.requireNonNull(level.getEntity(event.getEntity().getData(ModDataAttachments.ENTITY_HELD).getUUID("UUID"))).setNoGravity(false);
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if(event.getEntity().level().isClientSide) return;
        if(event.getEntity() instanceof ServerPlayer p && !event.getEntity().getData(ModDataAttachments.ENTITY_HELD).isEmpty() && event.getEntity().level() instanceof ServerLevel level && event.getEntity().getData(ModDataAttachments.POWER_TYPE.get()).equals("Viltrumite"))
        {
            Entity target = level.getEntity(p.getData(ModDataAttachments.ENTITY_HELD).getUUID("UUID"));
            if(target == null) return;
            target.setNoGravity(false);
            target.setDeltaMovement(p.getLookAngle().x * 1, p.getLookAngle().y * 0.75, p.getLookAngle().z * 1);
            p.setData(ModDataAttachments.ENTITY_HELD.get(), new CompoundTag());//sends an empty tag
            p.getData(ModDataAttachments.COOLDOWN).setCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "grab_cd"), level.getGameTime(), GRAB_CD);
            PacketDistributor.sendToPlayer(p, new CooldownSyncPayload(new Cooldown(level.getGameTime(), GRAB_CD), ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "grab_cd"), false));
            PacketDistributor.sendToPlayer(p, new CDPayload(new Cooldown(level.getGameTime(), GRAB_CD)));
        }

    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        Entity entity = event.getEntity();
        DamageSource source = event.getSource();
        DamageType type = source.type();
        String typeString = type.msgId();

        if(entity.getData(ModDataAttachments.POWER_TYPE).equals("Viltrumite")) {
            if(typeString.equals("minecraft:arrow"))
            {
                event.setAmount(event.getOriginalAmount()*4F);
            }
            if(typeString.equals("minecraft:magic") || typeString.equals("minecraft:indirectmagic"))
            {
                event.setAmount(event.getOriginalAmount()*2.5F);
            }
            if(typeString.equals("minecraft:explosion") || typeString.equals("minecraft:player.explosion"))
            {
                event.setAmount(event.getOriginalAmount()*1.5F);
            }
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event)
    {
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Viltrumite"))
        {
            if(event.getDamageSource().getDirectEntity() instanceof AbstractArrow)
            {
                event.setShieldDamage(80);
            }
        }
    }
}
