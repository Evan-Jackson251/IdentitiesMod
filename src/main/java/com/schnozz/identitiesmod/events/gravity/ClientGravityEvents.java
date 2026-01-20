package com.schnozz.identitiesmod.events.gravity;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.damage_sources.ModDamageTypes;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.mob_effects.ModEffects;
import com.schnozz.identitiesmod.networking.payloads.*;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.screen.icon.CooldownIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

import static com.schnozz.identitiesmod.keymapping.ModMappings.*;

/*
Gravity power plan:
    1. Delete everything
    2. Add anvil drop
    3. Add cyclone attack
    4. Add meteor
*/

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGravityEvents {
    //chaos target find global variables
    private static Entity target;
    private static double closestDistance;
    //entity list for pull
    private static List<Entity> entitiesInBox;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer gravityPlayer = Minecraft.getInstance().player;
        if (gravityPlayer == null) return;
        Level level = gravityPlayer.level();
        if(!level.isClientSide()) return;

        String power = gravityPlayer.getData(ModDataAttachments.POWER_TYPE);
        if (power.equals("Gravity")) {
            //gravity push
            if (GRAVITY_PUSH_MAPPING.get().consumeClick() && !gravityPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "gravity.ctrlcd"), 0)) {
                push(gravityPlayer);
                PUSH_COOLDOWN_ICON.setCooldown(new Cooldown(level.getGameTime(), 240));
                PULL_COOLDOWN_ICON.setCooldown(new Cooldown(level.getGameTime(), 240));
                gravityPlayer.getData(ModDataAttachments.COOLDOWN).setCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "gravity.ctrlcd"), level.getGameTime(), 240);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(level.getGameTime(), 240), ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "gravity.ctrlcd"), false));
            }
            //gravity pull
            else if (GRAVITY_PULL_MAPPING.get().consumeClick() && !gravityPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "gravity.ctrlcd"), 0)) {
                pull(gravityPlayer);
                PULL_COOLDOWN_ICON.setCooldown(new Cooldown(level.getGameTime(), 240));
                PUSH_COOLDOWN_ICON.setCooldown(new Cooldown(level.getGameTime(), 240));
                gravityPlayer.getData(ModDataAttachments.COOLDOWN).setCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "gravity.ctrlcd"), level.getGameTime(), 240);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(level.getGameTime(), 240), ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "gravity.ctrlcd"), false));
            }
            //gravity meteor creation and set both position and movement
            else if(GRAVITY_METEOR_MAPPING.get().consumeClick()) //EVAN THIS NEEDS COOLDOWN
            {
                //MeteorEntity newMeteor = new MeteorEntity(,level);
            }

        }
    }
    //cooldown icons
    private static final CooldownIcon PUSH_COOLDOWN_ICON = new CooldownIcon(10, 10, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/gravitypushcd_icon.png"));
    private static final CooldownIcon PULL_COOLDOWN_ICON = new CooldownIcon(10, 30, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/gravitypullcd_icon.png"));

    //ability methods
    public static void push(Player gravityPlayer)
    {
        Level level = gravityPlayer.level();

        double xMin = gravityPlayer.getX() - 10.0; double yMin = gravityPlayer.getY() - 10.0; double zMin = gravityPlayer.getZ() - 10.0;
        double xMax = gravityPlayer.getX() + 10.0; double yMax = gravityPlayer.getY() + 10.0; double zMax = gravityPlayer.getZ() + 10.0;
        AABB gravityForceBB = new AABB(xMin,yMin,zMin,xMax,yMax,zMax);

        List<Entity> entitiesInBox = level.getEntities(gravityPlayer, gravityForceBB);
        for (Entity entity : entitiesInBox) {
            Vec3 angle = gravityPlayer.getLookAngle();
            double rx = angle.x; double ry = angle.y; double rz = angle.z;
            double vX = 3.0 * rx; double vY = 1.2 * ry; double vZ = 3.0 * rz;
            PacketDistributor.sendToServer(new VelocityPayload(entity.getId(),vX,vY,vZ));
        }

        long currentTime = Minecraft.getInstance().level.getGameTime();

        CooldownAttachment newAtachment = new CooldownAttachment();
        newAtachment.getAllCooldowns().putAll(gravityPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
        newAtachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "push_strength_cd"), currentTime, 110);
        gravityPlayer.setData(ModDataAttachments.COOLDOWN, newAtachment);
        PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, 110), ResourceLocation.fromNamespaceAndPath("identitiesmod", "push_strength_cd"), false));
    }
    public static void pull(Player gravityPlayer)
    {
        Level level = gravityPlayer.level();

        double xMin = gravityPlayer.getX() - 20.0; double yMin = gravityPlayer.getY() - 20.0; double zMin = gravityPlayer.getZ() - 20.0;
        double xMax = gravityPlayer.getX() + 20.0; double yMax = gravityPlayer.getY() + 20.0; double zMax = gravityPlayer.getZ() + 20.0;
        AABB gravityForceBB = new AABB(xMin,yMin,zMin,xMax,yMax,zMax);

        entitiesInBox = level.getEntities(gravityPlayer, gravityForceBB);
        for (Entity entity : entitiesInBox) {
            double dx = entity.getX() - gravityPlayer.getX(); double dy = entity.getY() - gravityPlayer.getY(); double dz = entity.getZ() - gravityPlayer.getZ();
            double vX = -dx/3; double vY = -dy/6; double vZ = -dz/3;
            PacketDistributor.sendToServer(new VelocityPayload(entity.getId(),vX,vY,vZ));
        }

        long currentTime = Minecraft.getInstance().level.getGameTime();

        CooldownAttachment newAtachment = new CooldownAttachment();
        newAtachment.getAllCooldowns().putAll(gravityPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
        newAtachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "pull_strength_cd"), currentTime, 110);
        gravityPlayer.setData(ModDataAttachments.COOLDOWN, newAtachment);
        PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, 110), ResourceLocation.fromNamespaceAndPath("identitiesmod", "pull_strength_cd"), false));
    }

    public static void meteor()
    {

    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        if(!Minecraft.getInstance().player.getData(ModDataAttachments.POWER_TYPE).equals("Gravity"))
        {
            return;
        }

        long gameTime = Minecraft.getInstance().level.getGameTime();
        GuiGraphics graphics = event.getGuiGraphics();
        PUSH_COOLDOWN_ICON.render(graphics, gameTime);
        PULL_COOLDOWN_ICON.render(graphics, gameTime);
    }

}
