package com.schnozz.identitiesmod.events.power_events.clone;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.entities.custom_entities.PlayerCloneEntity;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ClonesSyncPayload;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerCloneEvents {
//    @SubscribeEvent
//    public static void onPlayerClone(PlayerEvent.Clone event)
//    {
//        System.out.println("CLONE EVENT");
//        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Clone"))
//        {
//            ServerPlayer clonePlayer = (ServerPlayer)event.getEntity();
//            PacketDistributor.sendToPlayer(clonePlayer,new ClonesSyncPayload(clonePlayer.getData(ModDataAttachments.CLONES)));
//            System.out.println("CLONES (SYNC): " + clonePlayer.getData(ModDataAttachments.CLONES).getClones());
//        }
//    }
    @SubscribeEvent
    public static void onLeavingGame(PlayerEvent.PlayerLoggedOutEvent event)
    {
        System.out.println("LOGOUT EVENT");
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Clone"))
        {
            ServerPlayer clonePlayer = (ServerPlayer)event.getEntity();
            PacketDistributor.sendToPlayer(clonePlayer,new ClonesSyncPayload(clonePlayer.getData(ModDataAttachments.CLONES)));
            System.out.println("CLONES (LOGOUT): " + clonePlayer.getData(ModDataAttachments.CLONES).getClones());
            clonePlayer.getData(ModDataAttachments.CLONES).clearClones();
        }
    }
    @SubscribeEvent
    public static void onJoiningGame(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Clone"))
        {
            System.out.println("LOGIN EVENT");
            ServerPlayer clonePlayer = (ServerPlayer)event.getEntity();
            clonePlayer.server.tell(new TickTask(
                    clonePlayer.server.getTickCount() + 1,
                    () -> {
                        ServerLevel level = clonePlayer.serverLevel();
                        for (Entity entity : level.getAllEntities()) { // Or level.getEntities()
                            if (entity instanceof PlayerCloneEntity) {
                                clonePlayer.getData(ModDataAttachments.CLONES).addClone(entity.getId());
                                PacketDistributor.sendToPlayer(clonePlayer, new ClonesSyncPayload(clonePlayer.getData(ModDataAttachments.CLONES)));
                            }
                        }
                        System.out.println("CLONES (LOGIN): " + clonePlayer.getData(ModDataAttachments.CLONES).getClones());
                    }
            ));
        }
    }

}
