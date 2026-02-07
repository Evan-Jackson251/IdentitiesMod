package com.schnozz.identitiesmod.events.power_events.trapper;

import com.schnozz.identitiesmod.IdentitiesMod;
import net.minecraft.world.scores.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerTrapperEvents {


    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event)
    {
        Scoreboard scoreboard = event.getServer().getScoreboard();
        String teamName = "hidden_name";

        PlayerTeam team = scoreboard.getPlayerTeam(teamName);

        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setNameTagVisibility(Team.Visibility.NEVER);
        }

    }
}
