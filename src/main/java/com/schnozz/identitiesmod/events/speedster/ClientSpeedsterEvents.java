package com.schnozz.identitiesmod.events.speedster;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.networking.payloads.PotionLevelPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.SpeedsterLightningSync;
import com.schnozz.identitiesmod.screen.icon.CooldownIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.SPEEDSTER_LIGHTNING_MAPPING;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientSpeedsterEvents {
    //countdown variable for lightning state
    private static int stateCount = -1;
    private static final int STATE_DURATION = 120;
    //cd final variables
    private static CooldownIcon LIGHTNING_STATE_ICON = new CooldownIcon(10, 10, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/lightning_icon.png"));
    private static final int LIGHTNING_STATE_CD = 600;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer speedPlayer = Minecraft.getInstance().player;
        if (speedPlayer == null) return;
        Level level = speedPlayer.level();
        if(!level.isClientSide()) return;


        String power = speedPlayer.getData(ModDataAttachments.POWER_TYPE);
        if(power.equals("Speedster"))
        {
            //Turns on lightning state
            if(SPEEDSTER_LIGHTNING_MAPPING.get().consumeClick() && stateCount == -1 && !speedPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "lightning_state_cd"),0))
            {
                speedPlayer.setData(ModDataAttachments.SPEEDSTER_LIGHTNING,1);
                PacketDistributor.sendToServer(new SpeedsterLightningSync(1));
                stateCount = 0;
                LIGHTNING_STATE_ICON = new CooldownIcon(10, 10, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/blue_lightning_icon.png"));

                speedPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,STATE_DURATION,4,false,true,true));
                PacketDistributor.sendToServer(new PotionLevelPayload(MobEffects.MOVEMENT_SPEED,4,STATE_DURATION));
                speedPlayer.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,STATE_DURATION,3,false,true,true));
                PacketDistributor.sendToServer(new PotionLevelPayload(MobEffects.DIG_SPEED,3,STATE_DURATION));
            }
            //Turns off lightning state
            if(speedPlayer.getData(ModDataAttachments.SPEEDSTER_LIGHTNING) == 1)
            {
                stateCount++;
                if(stateCount >= STATE_DURATION)
                {
                    stateCount = -1;
                    speedPlayer.setData(ModDataAttachments.SPEEDSTER_LIGHTNING,0);
                    PacketDistributor.sendToServer(new SpeedsterLightningSync(0));

                    LIGHTNING_STATE_ICON = new CooldownIcon(10, 10, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/lightning_icon.png"));

                    long currentTime = Minecraft.getInstance().level.getGameTime();
                    CooldownAttachment atachment = new CooldownAttachment();
                    atachment.getAllCooldowns().putAll(speedPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                    atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "lightning_state_cd"), currentTime, LIGHTNING_STATE_CD);
                    speedPlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                    PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, LIGHTNING_STATE_CD), ResourceLocation.fromNamespaceAndPath("identitiesmod", "lightning_state_cd"), false));
                    LIGHTNING_STATE_ICON.setCooldown(new Cooldown(currentTime, LIGHTNING_STATE_CD));
                }
            }
        }
    }
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        if(!Minecraft.getInstance().player.getData(ModDataAttachments.POWER_TYPE).equals("Speedster"))
        {
            return;
        }
        long gameTime = Minecraft.getInstance().level.getGameTime();
        GuiGraphics graphics = event.getGuiGraphics();
        LIGHTNING_STATE_ICON.render(graphics, gameTime);
    }
}
