package com.schnozz.identitiesmod.events.kyle;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.leveldata.FarmValueSavedData;
import com.schnozz.identitiesmod.networking.payloads.KyleWorthPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.KYLE_WORTH_MAPPING;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientKyleEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer kyle = Minecraft.getInstance().player;
        if (kyle == null) return;
        Level level = kyle.level();
        if(!level.isClientSide()) return;

        String power = kyle.getData(ModDataAttachments.POWER_TYPE);
        if (power.equals("Kyle")) {
            if(KYLE_WORTH_MAPPING.get().consumeClick())
            {
                PacketDistributor.sendToServer(new KyleWorthPayload(kyle.getId()));
            }
        }
    }
}
