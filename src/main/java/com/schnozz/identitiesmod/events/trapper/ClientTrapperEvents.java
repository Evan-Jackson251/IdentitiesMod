package com.schnozz.identitiesmod.events.trapper;


import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.mob_effects.ModEffects;
import com.schnozz.identitiesmod.networking.payloads.PotionLevelPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.screen.icon.CooldownIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.TRAPPER_MAPPING;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientTrapperEvents {
    private static final CooldownIcon SUPER_INVISCD_ICON = new CooldownIcon(10, 10, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/superinviscd_icon.png"));

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        LocalPlayer p = Minecraft.getInstance().player;
        if(p == null || !p.level().isClientSide() || !p.hasData(ModDataAttachments.POWER_TYPE)) return;
        String power = p.getData(ModDataAttachments.POWER_TYPE);

        if (power.equals("Trapper") && TRAPPER_MAPPING.get().consumeClick() && !p.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "invis_cd"), 0)) {

            PacketDistributor.sendToServer(new PotionLevelPayload(ModEffects.SUPER_INVISIBILITY, 1, 200));

            //cd set
            p.getData(ModDataAttachments.COOLDOWN).setCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "invis_cd"), p.level().getGameTime(), 700);
            PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(p.level().getGameTime(), 700), ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "invis_cd"), false));
            SUPER_INVISCD_ICON.setCooldown(new Cooldown(p.level().getGameTime(), 700));


        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        if(!Minecraft.getInstance().player.getData(ModDataAttachments.POWER_TYPE).equals("Trapper"))
        {
            return;
        }

        long gameTime = Minecraft.getInstance().level.getGameTime();
        GuiGraphics graphics = event.getGuiGraphics();
        SUPER_INVISCD_ICON.render(graphics, gameTime);
    }

}
