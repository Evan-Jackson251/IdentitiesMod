package com.schnozz.identitiesmod.events.power_events.adaptation;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.networking.payloads.SoundPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.AdaptationSyncPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.screen.icon.CooldownIcon;
import com.schnozz.identitiesmod.sounds.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.ADAPTATION_SWITCH_MAPPING;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class   ClientAdaptationEvents {
//    private static final AdapterProgressBar DIAMOND_SWORD_BAR = new AdapterProgressBar(10,10, 18, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/diamond_sword_icon.png"),ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"player_attack"),0.65F);
//    private static final AdapterProgressBar FLAME_BAR = new AdapterProgressBar(10,33, 18, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/flame_icon.png"),ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"infire"),1F);
//    private static final AdapterProgressBar POTION_BAR = new AdapterProgressBar(10,56, 18, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/potion_of_harming_icon.png"),ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"magic"),0.65F);
//    private static final AdapterProgressBar EXPLOSION_BAR = new AdapterProgressBar(10,79, 18, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/explosion_icon.png"),ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"explosion.player"),0.9F);
//    private static final AdapterProgressBar ARROW_BAR = new AdapterProgressBar(10,102, 18, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/arrow_icon.png"),ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"arrow"),0.65F);

    private static final CooldownIcon switchIcon = new CooldownIcon(10, 10, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/adaptation_icon.png"));
    private static final int SWITCH_CD = 3000;
    private static int switchTimer = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer adaptationPlayer = Minecraft.getInstance().player;
        if (adaptationPlayer == null) return;
        Level level = adaptationPlayer.level();
        if(!level.isClientSide()) return;

        if(adaptationPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Adaptation"))
        {
            if(ADAPTATION_SWITCH_MAPPING.get().consumeClick() && !adaptationPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "adaptation.switchcd"), 0))
            {
                switchAdaptation(adaptationPlayer);
                switchTimer = 1;

                adaptationPlayer.getData(ModDataAttachments.COOLDOWN).setCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "adaptation.switchcd"), level.getGameTime(), SWITCH_CD);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(level.getGameTime(), SWITCH_CD), ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "adaptation.switchcd"), false));
                switchIcon.setCooldown(new Cooldown(Minecraft.getInstance().level.getGameTime(), SWITCH_CD));
            }

            if(switchTimer > 0 && switchTimer < 600)
            {
                switchTimer++;
            }
            else if(switchTimer > 0)
            {
                adaptationPlayer.getData(ModDataAttachments.ADAPTION).setAdaptationValue(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"offensive"),1);
                PacketDistributor.sendToServer(new AdaptationSyncPayload(adaptationPlayer.getData(ModDataAttachments.ADAPTION)));
            }
        }
    }
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        if(!Minecraft.getInstance().player.getData(ModDataAttachments.POWER_TYPE).equals("Adaptation"))
        {
            return;
        }
        switchIcon.render(event.getGuiGraphics(),Minecraft.getInstance().level.getGameTime());
        //DIAMOND_SWORD_BAR.render(event.getGuiGraphics());
        //FLAME_BAR.render(event.getGuiGraphics());
        //POTION_BAR.render(event.getGuiGraphics());
        //EXPLOSION_BAR.render(event.getGuiGraphics());
        //ARROW_BAR.render(event.getGuiGraphics());
    }

    public static void switchAdaptation(Player adaptationPlayer)
    {
        PacketDistributor.sendToServer(new SoundPayload(ModSounds.MAHORAGA_THEME_SOUND.get(),10F));
        // 0 is on, 1 is off
        adaptationPlayer.getData(ModDataAttachments.ADAPTION).setAdaptationValue(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"offensive"),0);
        PacketDistributor.sendToServer(new AdaptationSyncPayload(adaptationPlayer.getData(ModDataAttachments.ADAPTION)));
    }
}
