package com.schnozz.identitiesmod.events.power_events.dragon;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.entities.ModEntities;
import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import com.schnozz.identitiesmod.entities.custom_entities.PlayerCloneEntity;
import com.schnozz.identitiesmod.networking.payloads.DragonSpawnPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.DRAGON_SHIFT;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientDragonEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer dragonPlayer = Minecraft.getInstance().player;
        if (dragonPlayer == null) return;
        Level level = dragonPlayer.level();

        if(dragonPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Dragon")){
            //spawn dragon
            if(DRAGON_SHIFT.get().consumeClick()){
                PacketDistributor.sendToServer(new DragonSpawnPayload(dragonPlayer.getId()));
            }
        }
    }
}
