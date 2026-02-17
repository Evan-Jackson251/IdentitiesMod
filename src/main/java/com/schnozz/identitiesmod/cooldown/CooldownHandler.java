package com.schnozz.identitiesmod.cooldown;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CooldownHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            long currentTime = level.getGameTime();
            CooldownAttachment cdMapAttached = player.getData(ModDataAttachments.COOLDOWN);
            CooldownAttachment newAttachment = new CooldownAttachment();
            newAttachment.getAllCooldowns().putAll(cdMapAttached.getAllCooldowns());

            if (!cdMapAttached.getAllCooldowns().isEmpty()) {
                List<ResourceLocation> toRemove = new ArrayList<>();

                for (Map.Entry<ResourceLocation, Cooldown> entry : cdMapAttached.getAllCooldowns().entrySet()) {
                    if (currentTime >= entry.getValue().startTime() + entry.getValue().duration()) {
                        toRemove.add(entry.getKey());
                    }
                }

                for (ResourceLocation key : toRemove) {
                    PacketDistributor.sendToPlayer(player, new CooldownSyncPayload(cdMapAttached.getAllCooldowns().get(key), key, true));
                    newAttachment.getAllCooldowns().remove(key); // SAFE: happens after iteration
                }

                player.setData(ModDataAttachments.COOLDOWN, newAttachment);
            }
        }
    }
}
