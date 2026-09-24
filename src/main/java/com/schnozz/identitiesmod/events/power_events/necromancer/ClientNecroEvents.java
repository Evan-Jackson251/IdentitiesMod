package com.schnozz.identitiesmod.events.power_events.necromancer;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.NecroDespawnPayload;
import com.schnozz.identitiesmod.networking.payloads.NecroSummonPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.PRIMARY_MAPPING;
import static com.schnozz.identitiesmod.keymapping.ModMappings.UTILITY_MAPPING;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientNecroEvents {

    @SubscribeEvent
    public static void OnClientTick(ClientTickEvent.Post event)
    {
        LocalPlayer p = Minecraft.getInstance().player;
        if(p == null || !p.level().isClientSide() || !p.hasData(ModDataAttachments.POWER_TYPE)) return;
        String power = p.getData(ModDataAttachments.POWER_TYPE);

        //Summon next Entity from list
        if(power.equals("Necromancer") && PRIMARY_MAPPING.get().consumeClick())
        {
            PacketDistributor.sendToServer(new NecroSummonPayload(p.getId()));
        }

        //Despawn entity looking at within 8 blocks
        if(power.equals("Necromancer") && UTILITY_MAPPING.get().consumeClick())
        {
            PacketDistributor.sendToServer(new NecroDespawnPayload(p.getId()));
        }
    }
}
