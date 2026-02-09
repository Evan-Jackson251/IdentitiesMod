package com.schnozz.identitiesmod.events.power_events.clone;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.entities.custom_entities.PlayerCloneEntity;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ClonesSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerCloneEvents {
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event)
    {
        if(event.getEntity() instanceof PlayerCloneEntity clone)
        {
            ServerPlayer cloner = (ServerPlayer)event.getEntity().level().getEntity(clone.getCreatorId());
            if(cloner==null){
                return;
            }
            cloner.getData(ModDataAttachments.CLONES).removeClone(clone.getId());
            PacketDistributor.sendToPlayer(cloner,new ClonesSyncPayload(cloner.getData(ModDataAttachments.CLONES)));
        }
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event)
    {
        System.out.println("CLONE EVENT");
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Clone"))
        {
            ServerPlayer clonePlayer = (ServerPlayer)event.getEntity();
            PacketDistributor.sendToPlayer(clonePlayer,new ClonesSyncPayload(clonePlayer.getData(ModDataAttachments.CLONES)));
            System.out.println("CLONES (SYNC): " + clonePlayer.getData(ModDataAttachments.CLONES).getClones());
        }
    }
    @SubscribeEvent
    public static void onRelog(PlayerEvent.PlayerLoggedInEvent event)
    {
        System.out.println("LOGIN EVENT");
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Clone"))
        {
            ServerPlayer clonePlayer = (ServerPlayer)event.getEntity();
            PacketDistributor.sendToPlayer(clonePlayer,new ClonesSyncPayload(clonePlayer.getData(ModDataAttachments.CLONES)));
            System.out.println("CLONES (SYNC): " + clonePlayer.getData(ModDataAttachments.CLONES).getClones());
        }
    }
}
