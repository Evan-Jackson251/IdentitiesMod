package com.schnozz.identitiesmod.events.power_events.clone;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.ClonePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.CLONE_MAPPING;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientCloneEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer clonePlayer = Minecraft.getInstance().player;
        if (clonePlayer == null) return;
        Level level = clonePlayer.level();
        if(!level.isClientSide()) return;

        String power = clonePlayer.getData(ModDataAttachments.POWER_TYPE);

        if(power.equals("clone"))
        {
            //CLONE MAPPING
            if(CLONE_MAPPING.get().consumeClick())
            {
                PacketDistributor.sendToServer(new ClonePayload(clonePlayer.getId()));
            }

        }
    }

}