package com.schnozz.identitiesmod.events.power_events.necromancer;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.leveldata.UUIDSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class NecroUtils {
    //checks the hashmap to see if the mob is controlled by this specific player
    public static boolean NecroMobCheck(Entity e, ServerPlayer player, MinecraftServer server)
    {
        UUIDSavedData list = UUIDSavedData.get(server);
        return player.getData(ModDataAttachments.POWER_TYPE).equals("Necromancer") && e instanceof Monster m && list.getMobs(player.getUUID()).contains(m.getUUID());
    }
}
