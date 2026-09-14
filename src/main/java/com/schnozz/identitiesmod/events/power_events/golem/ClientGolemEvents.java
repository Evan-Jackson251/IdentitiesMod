package com.schnozz.identitiesmod.events.power_events.golem;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.EmeraldGolemSpawnPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import static com.schnozz.identitiesmod.keymapping.ModMappings.PRIMARY_MAPPING;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGolemEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer golemPlayer = Minecraft.getInstance().player;
        if (golemPlayer == null) return;
        Level level = golemPlayer.level();
        if(!level.isClientSide()) return;

        if(golemPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Golem") && PRIMARY_MAPPING.get().consumeClick())
        {
            PacketDistributor.sendToServer(new EmeraldGolemSpawnPayload(golemPlayer.getId()));
        }
    }
}
