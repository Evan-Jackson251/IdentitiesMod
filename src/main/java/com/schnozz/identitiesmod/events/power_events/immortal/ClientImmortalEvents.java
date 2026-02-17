package com.schnozz.identitiesmod.events.power_events.immortal;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.icons.CooldownIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientImmortalEvents {
    private static CooldownIcon Immortal_Icon = new CooldownIcon(128,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/totem_icon.png"));

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer immortalPlayer = Minecraft.getInstance().player;
        if (immortalPlayer == null) return;
        Level level = immortalPlayer.level();
        if (!level.isClientSide()) return;

        if(immortalPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Immortal")){

            if(immortalPlayer.getData(ModDataAttachments.LIVES) == 3){
                Immortal_Icon = new CooldownIcon(128,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/totem_icon.png"));
            }
            if(immortalPlayer.getData(ModDataAttachments.LIVES) == 2){
                Immortal_Icon = new CooldownIcon(128,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/dark_cave_icon.png"));
            }
            if(immortalPlayer.getData(ModDataAttachments.LIVES) == 1){
                Immortal_Icon = new CooldownIcon(128,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/ice_icon.png"));
            }
            if(immortalPlayer.getData(ModDataAttachments.LIVES) == 0){
                Immortal_Icon = new CooldownIcon(128,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/hangman_icon.png"));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Player p = Minecraft.getInstance().player;
        if(!p.getData(ModDataAttachments.POWER_TYPE).equals("Immortal"))
        {
            return;
        }
        long gameTime = Minecraft.getInstance().level.getGameTime();
        GuiGraphics graphics = event.getGuiGraphics();

        Immortal_Icon.render(graphics,gameTime);
    }
}
