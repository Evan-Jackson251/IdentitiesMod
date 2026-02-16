package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.leveldata.PowerSavedData;
import com.schnozz.identitiesmod.networking.payloads.PowerTakenPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.PowersTakenSyncPayload;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPowerTakenHandler {
    public static void handle(PowerTakenPayload payload, IPayloadContext context) {
        ServerLevel level = (ServerLevel)context.player().level();
        PowerSavedData powersTaken = PowerSavedData.get(level);
        if(!powersTaken.getPowersTaken().contains(payload.power()))
        {
            powersTaken.addPowerTaken(payload.power());
            PacketDistributor.sendToAllPlayers(new PowersTakenSyncPayload(powersTaken.getPowersTaken()));
        }
    }
}
