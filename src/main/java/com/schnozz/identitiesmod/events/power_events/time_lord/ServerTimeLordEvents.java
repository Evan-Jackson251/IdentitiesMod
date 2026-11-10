package com.schnozz.identitiesmod.events.power_events.time_lord;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.damage_sources.ModDamageTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerTimeLordEvents {
    private static boolean timeStopped = false;
    private static int stopperID = -1;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event)
    {
        if(event.getEntity() instanceof Player player){
            if(!timeStopped && player.getData(ModDataAttachments.TIME_STOP_STATE) == 1)
            {
                timeStopped = true;
                stopperID = player.getId();

                MinecraftServer server = player.level().getServer();
                assert server != null;
                CommandSourceStack source = server.createCommandSourceStack().withSuppressedOutput();
                server.getCommands().performPrefixedCommand(source,"tick freeze");
            }
            if(player.getId() == stopperID)
            {
                if(player.getData(ModDataAttachments.TIME_STOP_STATE) == 0)
                {
                    timeStopped = false;
                    stopperID = -1;
                    player.setInvulnerable(false);

                    MinecraftServer server = player.level().getServer();
                    assert server != null;
                    CommandSourceStack source = server.createCommandSourceStack().withSuppressedOutput();
                    server.getCommands().performPrefixedCommand(source,"tick unfreeze");
                }
                else{
                    player.setInvulnerable(true);
                }

            }
            else if(timeStopped)
            {
                player.setYRot(player.yRotO);
                player.setXRot(player.xRotO);
                event.setCanceled(true);
            }
        }
    }

}
