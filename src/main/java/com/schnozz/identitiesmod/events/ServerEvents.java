package com.schnozz.identitiesmod.events;


import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.sounds.ModSounds;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Random;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.DEDICATED_SERVER)
public class ServerEvents {
    private static long tickCounter = 0;

    //teleport combat tracking
    @SubscribeEvent
    public static void onEntityDamage(LivingIncomingDamageEvent event)
    {
        if(event.getEntity().level().isClientSide) return;
        if(event.getEntity() instanceof ServerPlayer hurtPlayer)
        {
            //cd set
            long currentTime = hurtPlayer.level().getGameTime();
            CooldownAttachment newAtachment = new CooldownAttachment();


            if(event.getSource().getDirectEntity() != null && event.getSource().getDirectEntity() instanceof Player)
            {
                newAtachment.getAllCooldowns().putAll(hurtPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                newAtachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "teleport_combat_tracked_cd"), currentTime, 2400);
                hurtPlayer.setData(ModDataAttachments.COOLDOWN, newAtachment);
                PacketDistributor.sendToPlayer(hurtPlayer, new CooldownSyncPayload(new Cooldown(currentTime, 2400), ResourceLocation.fromNamespaceAndPath("identitiesmod", "teleport_combat_tracked_cd"), false));

            }
            else{

                newAtachment.getAllCooldowns().putAll(hurtPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                newAtachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "teleport_combat_tracked_cd"), currentTime, 400);
                hurtPlayer.setData(ModDataAttachments.COOLDOWN, newAtachment);
                PacketDistributor.sendToPlayer(hurtPlayer, new CooldownSyncPayload(new Cooldown(currentTime, 400), ResourceLocation.fromNamespaceAndPath("identitiesmod", "teleport_combat_tracked_cd"), false));
            }

        }
        if(event.getSource().getDirectEntity() != null && event.getSource().getDirectEntity().isAlive()  ) {
            if (event.getSource().getDirectEntity() instanceof ServerPlayer angryPlayer) {
                //cd set
                long currentTime = angryPlayer.level().getGameTime();
                CooldownAttachment newAtachment = new CooldownAttachment();


                if(event.getEntity() instanceof Player)
                {
                    newAtachment.getAllCooldowns().putAll(angryPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                    newAtachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "teleport_combat_tracked_cd"), currentTime, 2400);
                    angryPlayer.setData(ModDataAttachments.COOLDOWN, newAtachment);
                    PacketDistributor.sendToPlayer(angryPlayer, new CooldownSyncPayload(new Cooldown(currentTime, 2400), ResourceLocation.fromNamespaceAndPath("identitiesmod", "teleport_combat_tracked_cd"), false));
                }
                else{
                    newAtachment.getAllCooldowns().putAll(angryPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                    newAtachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "teleport_combat_tracked_cd"), currentTime, 400);
                    angryPlayer.setData(ModDataAttachments.COOLDOWN, newAtachment);
                    PacketDistributor.sendToPlayer(angryPlayer, new CooldownSyncPayload(new Cooldown(currentTime, 400), ResourceLocation.fromNamespaceAndPath("identitiesmod", "teleport_combat_tracked_cd"), false));
                }

            }
        }
    }

    //Scoreboard displays
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {

        //adds kills below player nametag
        Scoreboard scoreboard = event.getServer().getScoreboard();
        String objectiveName = "playerKills";

        Objective objective = scoreboard.getObjective(objectiveName);

        if (objective == null) {
            objective = scoreboard.addObjective(
                    objectiveName,
                    ObjectiveCriteria.KILL_COUNT_PLAYERS,
                    Component.literal("Kills"),
                    ObjectiveCriteria.RenderType.INTEGER,
                    false,
                    null
            );
        }

        scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, objective);

        //adds deaths to tab list
        objectiveName = "playerDeaths";

        objective = scoreboard.getObjective(objectiveName);

        if (objective == null) {
            objective = scoreboard.addObjective(
                    objectiveName,
                    ObjectiveCriteria.DEATH_COUNT,
                    Component.literal("Deaths"),
                    ObjectiveCriteria.RenderType.INTEGER,
                    false,
                    null
            );
        }

        scoreboard.setDisplayObjective(DisplaySlot.LIST, objective);
    }

    //Cord broadcast logic and timing
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event)
    {
        MinecraftServer server = event.getServer();

        tickCounter++;
        if (tickCounter >= 36000) {
            tickCounter = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if(!player.getTags().contains("AFK")) {

                    BlockPos pos = player.blockPosition();
                    Random random = new Random();

                    String message = "Player: " + player.getName().getString() + " (" + player.getData(ModDataAttachments.POWER_TYPE) + ") " + " is in the " + player.level().dimension().location() + " around " + (pos.getX() + random.nextInt(30, 251) + " , " + (pos.getZ() + random.nextInt(30, 251)));
                    sendBroadcast(message, server);
                }
            }
        }

    }
    //Auto AFK removal
    @SubscribeEvent
    private static void playerMove(MovementInputUpdateEvent event)
    {
        if(event.getEntity().getTags().contains(Component.literal("AFK"))){
            event.getEntity().removeTag("AFK");
            event.getEntity().sendSystemMessage(Component.literal("NO LONGER AFK"));
        }
    }
    //Cords broadcast
    private static void sendBroadcast(String message, MinecraftServer server) {
        // Loop through all server players and send chat
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if(!player.getTags().contains("AFK"))
                {
                    player.sendSystemMessage(Component.literal("[SERVER BROADCAST] " + message).withColor(0xFFFF00));
                }
            }
        }
    }
}
