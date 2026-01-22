package com.schnozz.identitiesmod.events.speedster;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.SpeedsterLightningSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.SPEEDSTER_LIGHTNING_MAPPING;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientSpeedsterEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer speedPlayer = Minecraft.getInstance().player;
        if (speedPlayer == null) return;
        Level level = speedPlayer.level();
        if(!level.isClientSide()) return;

        String power = speedPlayer.getData(ModDataAttachments.POWER_TYPE);
        if(power.equals("Speedster"))
        {
            if(SPEEDSTER_LIGHTNING_MAPPING.get().consumeClick())
            {
                System.out.println("Mapping works");
                speedPlayer.setData(ModDataAttachments.SPEEDSTER_LIGHTNING,1);
                PacketDistributor.sendToServer(new SpeedsterLightningSync(1));
            }
        }

    }
}
