package com.schnozz.identitiesmod.events.viltrumite;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.items.BoundingBoxVisualizer;
import com.schnozz.identitiesmod.mob_effects.ModEffects;
import com.schnozz.identitiesmod.networking.payloads.*;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.screen.icon.CooldownIcon;
import com.schnozz.identitiesmod.sounds.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.List;

import static com.schnozz.identitiesmod.keymapping.ModMappings.*;
/*
Viltrumite Plan:
    1. Change dash to big aoe punch without stun
    2. Remove flight and custom weapons
    3. Add permanent str and spd level 1
    4. Add temp resistance 2 on cooldown
    5. Think about grab and how the kit feels
 */
@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientViltrumiteEvents {
    //damage constants
    private static final float CHOKE_DAMAGE = 3.0F;
    //integer
    private static int stunDuration = 35;
    //timers
    private static int dashDuration = 0;
    private static int stunTimer = 0;
    private static int dashMisses = 0;
    //flight speed double
    private static double flightSpeed = 0.35;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer viltrumitePlayer = Minecraft.getInstance().player;
        if(viltrumitePlayer == null || !viltrumitePlayer.level().isClientSide() || !viltrumitePlayer.hasData(ModDataAttachments.POWER_TYPE)) return;
        String power = viltrumitePlayer.getData(ModDataAttachments.POWER_TYPE);

        if (power.equals("Viltrumite")) {
            //key presses
            if(VILTRUMITE_GRAB_MAPPING.get().consumeClick() && !viltrumitePlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "grab_cd"), 0))
            {
                //if(viltrumitePlayer.getMainHandItem().getItem() == ItemRegistry.FAST_POWER_GAUNTLET || viltrumitePlayer.getMainHandItem().getItem() == ItemRegistry.STRONG_POWER_GAUNTLET) {
                    findEntity(Minecraft.getInstance().player);
                //}
            }
            if(VILTRUMITE_CHOKE_MAPPING.get().consumeClick() && !viltrumitePlayer.getData(ModDataAttachments.VILTRUMITE_STATES).getViltrumiteState(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"flight")) && !viltrumitePlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "choke_dash_cd"), 0))
            {
                //if(viltrumitePlayer.getMainHandItem().getItem() == ItemRegistry.FAST_POWER_GAUNTLET || viltrumitePlayer.getMainHandItem().getItem() == ItemRegistry.STRONG_POWER_GAUNTLET) {
                    chokeDash(viltrumitePlayer);
                    viltrumitePlayer.getData(ModDataAttachments.COOLDOWN).setCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "choke_dash_cd"), viltrumitePlayer.level().getGameTime(), 500);
                    PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(viltrumitePlayer.level().getGameTime(), 500), ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "choke_dash_cd"), false));
                    CHOKE_DASH_CDICON.setCooldown(new Cooldown(viltrumitePlayer.level().getGameTime(), 500));
                //}
            }
            if(VILTRUMITE_FLIGHT_MAPPING.get().consumeClick())
            {
                //if(viltrumitePlayer.getMainHandItem().getItem() == ItemRegistry.FAST_POWER_GAUNTLET || viltrumitePlayer.getMainHandItem().getItem() == ItemRegistry.STRONG_POWER_GAUNTLET) {
                    if (!viltrumitePlayer.getData(ModDataAttachments.VILTRUMITE_STATES).getViltrumiteState(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "flight"))) {
                        viltrumitePlayer.getData(ModDataAttachments.VILTRUMITE_STATES).setViltrumiteState(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "flight"), true);
                    } else {
                        viltrumitePlayer.getData(ModDataAttachments.VILTRUMITE_STATES).setViltrumiteState(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "flight"), false);
                    }
                    if (viltrumitePlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "fly_cd"), 0)) {
                        viltrumitePlayer.sendSystemMessage(Component.literal("COMBAT LOGGED").withStyle(ChatFormatting.RED));
                    }
                //}
            }

            //combat logged flight turn off
            if(viltrumitePlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "fly_cd"), 0))
            {
                viltrumitePlayer.getData(ModDataAttachments.VILTRUMITE_STATES).setViltrumiteState(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"flight"),false);
            }
            //flight movement
            if(viltrumitePlayer.getData(ModDataAttachments.VILTRUMITE_STATES).getViltrumiteState(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"flight")))
            {
                flight(viltrumitePlayer);
            }
            //dash timer and choke hit
            if(dashDuration > 0 && dashDuration < 30)
            {
                dashDuration++;
                if(!chokeDamage(viltrumitePlayer))
                {
                    dashMisses++;
                }
            }
            if(dashMisses >= 20 && viltrumitePlayer != null && !viltrumitePlayer.isDeadOrDying())
            {
                viltrumitePlayer.addEffect(new MobEffectInstance(ModEffects.STUN, stunDuration, 0,false,true,true));
                PacketDistributor.sendToServer(new StunPayload(viltrumitePlayer.getId(), stunDuration));
                stunTimer = 1;
                dashMisses = 0;
            }
            if(stunTimer > 0 && stunTimer <= stunDuration)
            {
                stunTimer++;
            }
            if(stunTimer > stunDuration)
            {
                viltrumitePlayer.removeEffect(ModEffects.STUN);
                stunTimer = 0;
            }
        }
    }

    private static final CooldownIcon cooldownIcon = new CooldownIcon(10, 10, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/viltrumitegrabcd_icon.png"));
    private static final CooldownIcon CHOKE_DASH_CDICON = new CooldownIcon(10, 30, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/viltrumite_choke_dash_cd_icon.png"));
    //fix grab with ridable entities by preventing ride function
    private static boolean findEntity (Player player)
    {
        Vec3 look = player.getLookAngle();
        Vec3 start = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 frontCenter = start.add(look.scale(2.0)); // 2 blocks in front

        AABB aabb = new AABB(frontCenter, frontCenter).inflate(1.0); // Expand to 2x2x2 area
        BoundingBoxVisualizer.showAABB(player.level(), aabb);
        List<Entity> entities = player.level().getEntities(player, aabb, e -> !(e == player));

        if (!entities.isEmpty()) {
            Entity closest = entities.stream()
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                    .orElse(null);

            if (closest != null) {

                PacketDistributor.sendToServer(new EntityBoxPayload(closest.saveWithoutId(new CompoundTag()))); // packet only gets sent if a entity was found
                PacketDistributor.sendToServer(new SoundPayload(ModSounds.GRAB_SOUND.get(),5F));
                return true;
            }
        }
        long currentTime = player.level().getGameTime();
        CooldownAttachment newAtachment = new CooldownAttachment();
        newAtachment.getAllCooldowns().putAll(player.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
        newAtachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "grab_cd"), currentTime, 200);
        player.setData(ModDataAttachments.COOLDOWN, newAtachment);
        cooldownIcon.setCooldown(new Cooldown(currentTime, 200));
        PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, 200), ResourceLocation.fromNamespaceAndPath("identitiesmod", "grab_cd"), false));
        return false;

        // do nothing or anything you want to do if it fails put an else statement -- this wasn't chatgpt btw (I can tell. Your comment above says "a" instead of "an")
    }

    private static boolean chokeDamage(Player viltrumitePlayer)
    {
        Level level = viltrumitePlayer.level();

        List<Entity> entities = level.getEntities(viltrumitePlayer, viltrumitePlayer.getBoundingBox().inflate(1), (entity) -> {
            return entity instanceof LivingEntity && !entity.isSpectator() && entity != viltrumitePlayer;
        });
        BoundingBoxVisualizer.showAABB(level, viltrumitePlayer.getBoundingBox().inflate(1));

        Entity target = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            double distance = viltrumitePlayer.distanceTo(entity);
            if (distance < closestDistance) {
                closestDistance = distance;
                target = entity;
            }
        }

        if (target instanceof LivingEntity) {
            Holder<DamageType> placeHolderDamageType = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.PLAYER_ATTACK);
            PacketDistributor.sendToServer(new EntityDamagePayload(target.getId(),viltrumitePlayer.getId(), CHOKE_DAMAGE,placeHolderDamageType));
            dashDuration = 0;

            PacketDistributor.sendToServer(new VelocityPayload(viltrumitePlayer.getId(),0,0,0));
            PacketDistributor.sendToServer(new VelocityPayload(target.getId(),0,0,0));

            return true;
        }
        return false;
    }

    private static void chokeDash(Player viltrumitePlayer)
    {
        Vec3 angle = viltrumitePlayer.getLookAngle();
        double rx = angle.x; double ry = angle.y; double rz = angle.z;
        double vx = 3.0 * rx; double vy = 2.0 * ry; double vz = 3.0 * rz;
        PacketDistributor.sendToServer(new VelocityPayload(viltrumitePlayer.getId(),vx,vy,vz));
        dashDuration = 1;
        dashMisses = 1;
    }

    private static void flight(Player viltrumitePlayer)
    {
        Vec3 angle = viltrumitePlayer.getLookAngle();
        Vec3 flightVec = angle.multiply(flightSpeed,flightSpeed,flightSpeed);
        viltrumitePlayer.setDeltaMovement(flightVec);
    }

    public static void setIconCooldown(Cooldown cod)
    {
        cooldownIcon.setCooldown(cod);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        if(!Minecraft.getInstance().player.getData(ModDataAttachments.POWER_TYPE).equals("Viltrumite"))
        {
            return;
        }

        long gameTime = Minecraft.getInstance().level.getGameTime();
        GuiGraphics graphics = event.getGuiGraphics();
        cooldownIcon.render(graphics, gameTime);
        CHOKE_DASH_CDICON.render(graphics, gameTime);
    }

}
