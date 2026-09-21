package com.schnozz.identitiesmod.events.power_events.gravity;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.cooldown.CooldownUtil;
import com.schnozz.identitiesmod.damage_sources.ModDamageTypes;
import com.schnozz.identitiesmod.items.BoundingBoxVisualizer;
import com.schnozz.identitiesmod.networking.payloads.*;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ChargeSyncPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.icons.ChargeIcon;
import com.schnozz.identitiesmod.icons.CooldownIcon;
import com.schnozz.identitiesmod.sounds.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
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
Gravity power plan:
    1. Add black hole
*/

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGravityEvents {
    //cooldown icons
    private static final CooldownIcon CYCLONE_COOLDOWN_ICON = new CooldownIcon(88,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/cyclone.png"));
    private static final CooldownIcon DRIPSTONE_COOLDOWN_ICON = new CooldownIcon(108,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/dripstone.png"));
    private static final CooldownIcon ARROW_COOLDOWN_ICON = new CooldownIcon(128,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/arrow_icon.png"));
    private static final ChargeIcon CHARGE_ICON = new ChargeIcon(332,259,32,ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/black_hole_icon.png"),0);

    //final cooldown values
    private static final int DRIPSTONE_CD = 600;
    private static final int CYCLONE_CD = 300;
    private static final int ARROW_CD = 300;

    //timer variables
    private static int cycloneProgress = -1;
    private static int blackHoleProgress = -1;
    private static final int CYCLONE_DURATION = 60;
    private static final int BLACK_HOLE_DURATION = 180;

    //entity list within distance
    private static List<Entity> entitiesInBox;
    private static List<BlockState> blocksInBox;

    //range variables
    private static final int BLACK_HOLE_RANGE = 25;
    private static final int DRIP_STONE_RANGE = 25;

    //black hole temp variable
    private static Vec3 holePos = null;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer gravityPlayer = Minecraft.getInstance().player;
        if (gravityPlayer == null) return;
        Level level = gravityPlayer.level();
        if(!level.isClientSide()) return;

        String power = gravityPlayer.getData(ModDataAttachments.POWER_TYPE);
        if (power.equals("Gravity")) {
            CHARGE_ICON.setCharge(gravityPlayer.getData(ModDataAttachments.CHARGE));
            //dripstone drop
            if(UTILITY_MAPPING.get().consumeClick() && !gravityPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "dripstone_cd"),0)) {
                long currentTime = Minecraft.getInstance().level.getGameTime();

                CooldownUtil.SetCooldown(gravityPlayer, "dripstone_cd", currentTime, DRIPSTONE_CD);
                DRIPSTONE_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime, DRIPSTONE_CD));

                Vec3 dripPos = getDripPosition(gravityPlayer);
                level.addAlwaysVisibleParticle(ParticleTypes.CLOUD,dripPos.x,dripPos.y,dripPos.z,0,0,0);
                dripstoneDrop(dripPos);
            }
            //cyclone
            else if(SECONDARY_MAPPING.get().consumeClick() && !gravityPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "cyclone_cd"),0)) {
                cycloneProgress = 0;

                long currentTime = Minecraft.getInstance().level.getGameTime();

                CooldownAttachment atachment = new CooldownAttachment();
                atachment.getAllCooldowns().putAll(gravityPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "cyclone_cd"), currentTime, CYCLONE_CD);
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "gravity_arrow_cd"), currentTime, CYCLONE_CD);


                gravityPlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, CYCLONE_CD), ResourceLocation.fromNamespaceAndPath("identitiesmod", "cyclone_cd"), false));

                CYCLONE_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime, CYCLONE_CD));
                ARROW_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime,ARROW_CD));

                PacketDistributor.sendToServer(new SoundPayload(ModSounds.WIND_BLOWING_SOUND.get(),10F));
            }
            //arrow
            else if(PRIMARY_MAPPING.get().consumeClick() && !gravityPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "gravity_arrow_cd"),0))
            {
                long currentTime = Minecraft.getInstance().level.getGameTime();
                CooldownAttachment atachment = new CooldownAttachment();
                atachment.getAllCooldowns().putAll(gravityPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "gravity_arrow_cd"), currentTime, ARROW_CD);
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "cyclone_cd"), currentTime, ARROW_CD);

                gravityPlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, ARROW_CD), ResourceLocation.fromNamespaceAndPath("identitiesmod", "gravity_arrow_cd"), false));

                ARROW_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime, ARROW_CD));
                CYCLONE_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime,CYCLONE_CD));

                arrow(gravityPlayer);
                PacketDistributor.sendToServer(new SoundPayload(SoundEvents.ARROW_SHOOT,20F));
            }
            //black hole
            else if(SPECIAL_MAPPING.get().consumeClick() && CHARGE_ICON.getFull())
            {
                holePos = getHolePosition(gravityPlayer);
                level.addAlwaysVisibleParticle(ParticleTypes.CLOUD,holePos.x,holePos.y,holePos.z,0,0,0);

                blackHoleProgress = 0;
                CHARGE_ICON.setCharge(0);
                gravityPlayer.setData(ModDataAttachments.CHARGE,0.0);
                PacketDistributor.sendToServer(new ChargeSyncPayload(0.0));
            }

            //cyclone in progress if cooldown not done
            if(cycloneProgress <= CYCLONE_DURATION && cycloneProgress>=0){
                cyclone(gravityPlayer);
                cycloneProgress++;
            }
            else{
                cycloneProgress = -1;
            }

            //black hole in progress if cooldown not down
            if(holePos != null && blackHoleProgress <= BLACK_HOLE_DURATION && blackHoleProgress>=0){
                blackHolePull(holePos,blackHoleProgress/20.0);
                blackHoleProgress++;
            }else if(blackHoleProgress > BLACK_HOLE_DURATION){

                blackHoleProgress = -1;
            }
            else{
                blackHoleProgress = -1;
            }



        }
    }

    public static void arrow(Player gravityPlayer)
    {
        PacketDistributor.sendToServer(new GravityArrowPayload(gravityPlayer.getId()));
    }
    public static Vec3 getDripPosition(Player gravityPlayer) //w name
    {
        Vec3 scaledLookAngle = gravityPlayer.getLookAngle().scale(DRIP_STONE_RANGE);
        Vec3 eyePos = gravityPlayer.getEyePosition();
        Vec3 endPos = eyePos.add(scaledLookAngle);

        BlockHitResult blockHit = gravityPlayer.level().clip(new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                gravityPlayer
        ));

        if(blockHit.getType() != HitResult.Type.MISS){
            return blockHit.getLocation();
        }
        return endPos;
    }
    public static void dripstoneDrop(Vec3 pos)
    {
        PacketDistributor.sendToServer(new DripstoneDropPayload(pos));
    }
    public static void cyclone(Player gravityPlayer)
    {
        Level level = gravityPlayer.level();

        double xMin = gravityPlayer.getX() - 15.0; double yMin = gravityPlayer.getY() - 15.0; double zMin = gravityPlayer.getZ() - 15.0;
        double xMax = gravityPlayer.getX() + 15.0; double yMax = gravityPlayer.getY() + 15.0; double zMax = gravityPlayer.getZ() + 15.0;
        AABB gravityForceBB = new AABB(xMin,yMin,zMin,xMax,yMax,zMax);

        entitiesInBox = level.getEntities(gravityPlayer, gravityForceBB);
        for (Entity entity : entitiesInBox) {
            if(entity instanceof LivingEntity) {
                double dx = entity.getX() - gravityPlayer.getX();
                double dy = entity.getY() - gravityPlayer.getY();
                double dz = entity.getZ() - gravityPlayer.getZ();
                double vX = -dx / 6;
                double vY = -dy / 12;
                double vZ = -dz / 6;
                PacketDistributor.sendToServer(new VelocityPayload(entity.getId(), vX, vY, vZ));
                if (entity instanceof LivingEntity living && !living.hasEffect(MobEffects.WEAKNESS)) {
                    PacketDistributor.sendToServer(new WeaknessEffectPayload(entity.getId(), 70));
                }
            }
        }
    }
    public static Vec3 getHolePosition(Player gravityPlayer) //w name
    {
        Vec3 scaledLookAngle = gravityPlayer.getLookAngle().scale(BLACK_HOLE_RANGE);
        Vec3 eyePos = gravityPlayer.getEyePosition();
        Vec3 endPos = eyePos.add(scaledLookAngle);

        AABB aabb = new AABB(eyePos, endPos);
        //BoundingBoxVisualizer.showAABB(gravityPlayer.level(), aabb);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                gravityPlayer,
                eyePos,
                endPos,
                aabb,
                entity -> !entity.isSpectator(),
                eyePos.distanceToSqr(endPos)
        );
        if(entityHit != null && entityHit.getType() != HitResult.Type.MISS){
            return entityHit.getLocation();
        }

        BlockHitResult blockHit = gravityPlayer.level().clip(new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                gravityPlayer
        ));
        if(blockHit.getType() != HitResult.Type.MISS){
            return blockHit.getLocation().add(0,1,0);
        }

        return endPos;
    }
    public static void blackHolePull(Vec3 holePos, double time)
    {
        PacketDistributor.sendToServer(new BlackHolePayload(holePos, time));
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Player p = Minecraft.getInstance().player;
        if(!p.getData(ModDataAttachments.POWER_TYPE).equals("Gravity"))
        {
            return;
        }

        long gameTime = Minecraft.getInstance().level.getGameTime();
        GuiGraphics graphics = event.getGuiGraphics();
        CYCLONE_COOLDOWN_ICON.render(graphics, gameTime);
        DRIPSTONE_COOLDOWN_ICON.render(graphics, gameTime);
        ARROW_COOLDOWN_ICON.render(graphics, gameTime);
        CHARGE_ICON.render(graphics);
    }

}
