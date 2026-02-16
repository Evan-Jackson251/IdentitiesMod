package com.schnozz.identitiesmod.events.power_events.clone;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.networking.payloads.CloneCommandPayload;
import com.schnozz.identitiesmod.networking.payloads.ClonePayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

import static com.schnozz.identitiesmod.keymapping.ModMappings.*;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientCloneEvents {
    private static final CooldownIcon CLONE_COOLDOWN_ICON = new CooldownIcon(10, 10, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/clone_icon.png"));
    private static final int CLONE_COOLDOWN = 500;
    private static int cloneLimit = 2;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer clonePlayer = Minecraft.getInstance().player;
        if (clonePlayer == null) return;
        Level level = clonePlayer.level();
        if(!level.isClientSide()) return;

        String power = clonePlayer.getData(ModDataAttachments.POWER_TYPE);

        if(power.equals("Clone"))
        {
            //CLONE MAPPING
            if(CLONE_MAPPING.get().consumeClick() && !overCloneLimit(clonePlayer) && !clonePlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "clone_cd"),0))
            {
                PacketDistributor.sendToServer(new ClonePayload(clonePlayer.getId()));

                long currentTime = Minecraft.getInstance().level.getGameTime();
                CooldownAttachment atachment = new CooldownAttachment();
                atachment.getAllCooldowns().putAll(clonePlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "clone_cd"), currentTime, CLONE_COOLDOWN);
                clonePlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, CLONE_COOLDOWN), ResourceLocation.fromNamespaceAndPath("identitiesmod", "clone_cd"), false));
                CLONE_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime, CLONE_COOLDOWN));
            }
            if(TELEPORT_CLONE_MAPPING.get().consumeClick())
            {
                for(Integer cloneId: clonePlayer.getData(ModDataAttachments.CLONES).getClones())
                {
                    if(level.getEntity(cloneId) != null) {
                        PacketDistributor.sendToServer(new CloneCommandPayload(1,cloneId,clonePlayer.position()));
                    }
                }
            }
            if(KILL_CLONE_MAPPING.get().consumeClick())
            {
                for(Integer cloneId: clonePlayer.getData(ModDataAttachments.CLONES).getClones())
                {
                    if(level.getEntity(cloneId) != null) {
                        PacketDistributor.sendToServer(new CloneCommandPayload(2,cloneId,clonePlayer.position()));
                    }
                }
            }
            if(PEACEFUL_MODE_MAPPING.get().consumeClick())
            {
                for(Integer cloneId: clonePlayer.getData(ModDataAttachments.CLONES).getClones())
                {
                    if(level.getEntity(cloneId) != null) {
                        PacketDistributor.sendToServer(new CloneCommandPayload(3,cloneId,clonePlayer.position()));
                    }
                }
                System.out.println("CLONES: " + clonePlayer.getData(ModDataAttachments.CLONES).getClones());
            }
            if(AGGRESIVE_MODE_MAPPING.get().consumeClick())
            {
                for(Integer cloneId: clonePlayer.getData(ModDataAttachments.CLONES).getClones())
                {
                    if(level.getEntity(cloneId) != null) {
                        PacketDistributor.sendToServer(new CloneCommandPayload(4,cloneId,clonePlayer.position()));
                    }
                }
            }
            if(FOLLOW_MAPPING.get().consumeClick())
            {
                for(Integer cloneId: clonePlayer.getData(ModDataAttachments.CLONES).getClones())
                {
                    if(level.getEntity(cloneId) != null) {
                        PacketDistributor.sendToServer(new CloneCommandPayload(5,cloneId,clonePlayer.position()));
                    }
                }
            }
            if(UNFOLLOW_MAPPING.get().consumeClick())
            {
                for(Integer cloneId: clonePlayer.getData(ModDataAttachments.CLONES).getClones())
                {
                    if(level.getEntity(cloneId) != null) {
                        PacketDistributor.sendToServer(new CloneCommandPayload(6,cloneId,clonePlayer.position()));
                    }
                }
            }
        }
    }
    public static boolean overCloneLimit(Player clonePlayer)
    {
        int cloneCount = 0;
        ArrayList<Integer> cloneList = clonePlayer.getData(ModDataAttachments.CLONES).getClones();
        for(Integer cloneId: cloneList)
        {
            if(clonePlayer.level().getEntity(cloneId) != null)
            {
                cloneCount++;
            }
        }
        return cloneCount>=cloneLimit;
    }
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Player p = Minecraft.getInstance().player;
        if(!p.getData(ModDataAttachments.POWER_TYPE).equals("Clone"))
        {
            return;
        }

        long gameTime = Minecraft.getInstance().level.getGameTime();
        GuiGraphics graphics = event.getGuiGraphics();
        CLONE_COOLDOWN_ICON.render(graphics, gameTime);
    }

}