package com.schnozz.identitiesmod.events.time_lord;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerTimeLordEvents {
    private static boolean timeStoped = false;
    private static int stopperID = -1;

    //private static HashMap<Integer>
    //stops all entites but time lord
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event)
    {
        if(event.getEntity().getData(ModDataAttachments.TIME_STOP_STATE) == 1)
        {
            timeStoped = true;
            stopperID = event.getEntity().getId();
        }
        if(event.getEntity().getId() == stopperID)
        {
            if(event.getEntity().getData(ModDataAttachments.TIME_STOP_STATE) == 0)
            {
                timeStoped = false;
                stopperID = -1;
            }
            return;
        }
        else if(timeStoped)
        {
            if (event.getEntity() instanceof Player player) {
                player.setDeltaMovement(0,0,0);
                player.hurtMarked = true;
            }
            event.getEntity().setYRot(event.getEntity().yRotO);
            event.getEntity().setXRot(event.getEntity().xRotO);
            event.setCanceled(true);
        }

    }
}
