package com.schnozz.identitiesmod.events.power_events.clone;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.entities.custom_entities.PlayerCloneEntity;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ClonesSyncPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerCloneEvents {
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
    public static void onLeavingGame(PlayerEvent.PlayerLoggedOutEvent event)
    {
        System.out.println("LOGOUT EVENT");
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Clone"))
        {
            ServerPlayer clonePlayer = (ServerPlayer)event.getEntity();
            clonePlayer.getData(ModDataAttachments.CLONES).clearClones();
            //PacketDistributor.sendToPlayer(clonePlayer,new ClonesSyncPayload(clonePlayer.getData(ModDataAttachments.CLONES)));
            //System.out.println("CLONES (SYNC): " + clonePlayer.getData(ModDataAttachments.CLONES).getClones());
        }
    }
    @SubscribeEvent
    public static void onJoiningGame(PlayerEvent.PlayerLoggedInEvent event)
    {
        System.out.println("LOGIN EVENT");
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Clone"))
        {
            ServerPlayer clonePlayer = (ServerPlayer)event.getEntity();
            ServerLevel level = (ServerLevel)event.getEntity().level();
            for (Entity entity : level.getAllEntities()) { // Or level.getEntities()
                if(entity instanceof PlayerCloneEntity)
                {
                    clonePlayer.getData(ModDataAttachments.CLONES).addClone(entity.getId());
                    PacketDistributor.sendToPlayer(clonePlayer,new ClonesSyncPayload(clonePlayer.getData(ModDataAttachments.CLONES)));
                }
            }
        }
    }

}
