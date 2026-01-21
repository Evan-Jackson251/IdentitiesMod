package com.schnozz.identitiesmod.events.gravity;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.networking.payloads.*;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.screen.icon.CooldownIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static com.schnozz.identitiesmod.keymapping.ModMappings.*;

/*
Gravity power plan:
    1. Add grav arrow visuals
    2. Add meteor ult (charged by dealing damage with other abilities)
*/

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGravityEvents {
    //cooldown icons
    private static final CooldownIcon CYCLONE_COOLDOWN_ICON = new CooldownIcon(10, 10, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/gravitycyclonecd_icon.png"));
    private static final CooldownIcon DRIPSTONE_COOLDOWN_ICON = new CooldownIcon(10, 30, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/dripstone.png"));
    private static final CooldownIcon ARROW_COOLDOWN_ICON = new CooldownIcon(10, 50, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/arrow_icon.png"));
    //cyclone timer variable
    private static int cycloneProgress = -1;
    //entity list within distance
    private static List<Entity> entitiesInBox;
    private static List<BlockState> blocksInBox;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer gravityPlayer = Minecraft.getInstance().player;
        if (gravityPlayer == null) return;
        Level level = gravityPlayer.level();
        if(!level.isClientSide()) return;

        String power = gravityPlayer.getData(ModDataAttachments.POWER_TYPE);
        if (power.equals("Gravity")) {
            //dripstone drop
            if(GRAVITY_DRIPSTONE_MAPPING.get().consumeClick() && !gravityPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "dripstone_cd"),0)) {
                long currentTime = Minecraft.getInstance().level.getGameTime();
                int cd = 600;
                CooldownAttachment atachment = new CooldownAttachment();
                atachment.getAllCooldowns().putAll(gravityPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "dripstone_cd"), currentTime, cd);
                gravityPlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, cd), ResourceLocation.fromNamespaceAndPath("identitiesmod", "dripstone_cd"), false));
                DRIPSTONE_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime, cd));

                dripstoneDrop(gravityPlayer);
            }
            //cyclone
            else if(GRAVITY_CYCLONE_MAPPING.get().consumeClick() && !gravityPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "cyclone_cd"),0)) {
                cycloneProgress = 0;

                long currentTime = Minecraft.getInstance().level.getGameTime();
                int cd = 300;
                CooldownAttachment atachment = new CooldownAttachment();
                atachment.getAllCooldowns().putAll(gravityPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "cyclone_cd"), currentTime, cd);
                gravityPlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, cd), ResourceLocation.fromNamespaceAndPath("identitiesmod", "cyclone_cd"), false));
                CYCLONE_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime, cd));
            }
            //arrow
            else if(GRAVITY_ARROW_MAPPING.get().consumeClick() && !gravityPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "gravity_arrow_cd"),0))
            {
                long currentTime = Minecraft.getInstance().level.getGameTime();
                int cd = 250;
                CooldownAttachment atachment = new CooldownAttachment();
                atachment.getAllCooldowns().putAll(gravityPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "gravity_arrow_cd"), currentTime, cd);
                gravityPlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, cd), ResourceLocation.fromNamespaceAndPath("identitiesmod", "gravity_arrow_cd"), false));
                ARROW_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime, cd));
                arrow(gravityPlayer);
            }
            //meteor creation and set both position and movement
            else if(GRAVITY_METEOR_MAPPING.get().consumeClick()) //EVAN THIS NEEDS COOLDOWN
            {
                //MeteorEntity newMeteor = new MeteorEntity(,level);
            }

            //cyclone in progress if cooldown not done
            if(cycloneProgress <= 60 && cycloneProgress>=0){
                cyclone(gravityPlayer);
                cycloneProgress++;
            }
            else{
                cycloneProgress = -1;
            }
        }
    }

    public static void arrow(Player gravityPlayer)
    {
        PacketDistributor.sendToServer(new GravityArrowPayload(gravityPlayer.getId()));
    }
    public static void dripstoneDrop(Player gravityPlayer)
    {
        PacketDistributor.sendToServer(new DripstoneDropPayload(gravityPlayer.getId()));

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
        CYCLONE_COOLDOWN_ICON.render(graphics, gameTime);
        DRIPSTONE_COOLDOWN_ICON.render(graphics, gameTime);
        ARROW_COOLDOWN_ICON.render(graphics, gameTime);
    }

}
