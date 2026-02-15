package com.schnozz.identitiesmod.events;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.AvailablePowersSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerAdvancementEvents {
    private static final int[] REQS_NEEDED = {5,1,4,1,3,2,1,1,2,3,1,1}; //in alphabetical order
    @SubscribeEvent
    public static void onAchievement(AdvancementEvent.AdvancementEarnEvent event)
    {
        String advancementId = event.getAdvancement().id().toString();
        System.out.println("ADVANCEMENT: " + advancementId);

        ServerPlayer player = (ServerPlayer)event.getEntity();
        //Immortal power
        if (advancementId.equals("minecraft:adventure/totem_of_undying")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Immortal");
            powerAvailable(player, "Immortal", 5);
        } else if (advancementId.equals("minecraft:adventure/sleep_in_bed")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Immortal");
            powerAvailable(player, "Immortal", 5);
        } else if (advancementId.equals("minecraft:nether/charge_respawn_anchor")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Immortal");
            powerAvailable(player, "Immortal", 5);
        }
        //Gravity power
        else if (advancementId.equals("minecraft:adventure/fall_from_world_height")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Gravity");
            powerAvailable(player, "Immortal", 5);
        }
        //Time Lord power
        else if (advancementId.equals("minecraft:nether/create_full_beacon")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Time Lord");
            powerAvailable(player, "Time Lord", 11);
        }
        //Parry power
        else if (advancementId.equals("minecraft:story/deflect_arrow")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Parry");
            powerAvailable(player, "Parry", 9);
        } else if (advancementId.equals("minecraft:nether/return_to_sender")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Parry");
            powerAvailable(player, "Parry", 9);
        }
        //Speedster power
        else if (advancementId.equals("minecraft:adventure/adventuring_time")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Speedster");
            powerAvailable(player, "Speedster", 10);
        } else if (advancementId.equals("minecraft:nether/fast_travel")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Speedster");
            powerAvailable(player, "Speedster", 10);
        } else if (advancementId.equals("minecraft:nether/explore_nether")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Speedster");
            powerAvailable(player, "Speedster", 10);
        }
        //Kyle power
        else if (advancementId.equals("minecraft:husbandry/plant_seed")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Kyle");
            powerAvailable(player, "Kyle", 6);
        } else if (advancementId.equals("minecraft:husbandry/obtain_netherite_hoe")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Kyle");
            powerAvailable(player, "Kyle", 6);
        }
        //Viltrumite power
        else if (advancementId.equals("minecraft:story/shiny_gear")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Viltrumite");
            powerAvailable(player, "Viltrumite", 12);
        }
        //Necromancer power
        else if (advancementId.equals("minecraft:adventure/voluntary_exile")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Necromancer");
            powerAvailable(player, "Necromancer", 8);
        }
        //Adaptation power
        else if (advancementId.equals("minecraft:adventure/hot_stuff"))//fire
        {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Adaptation");
            powerAvailable(player, "Adaptation", 1);
        } else if (advancementId.equals("minecraft:nether/brew_potion"))//magic
        {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Adaptation");
            powerAvailable(player, "Adaptation", 1);
        } else if (advancementId.equals("minecraft:adventure/honey_block_slide"))//fall
        {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Adaptation");
            powerAvailable(player, "Adaptation", 1);
        } else if (advancementId.equals("minecraft:nether/get_wither_skull"))//wither
        {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Adaptation");
            powerAvailable(player, "Adaptation", 1);
        } else if (advancementId.equals("minecraft:adventure/sniper_duel"))//arrow
        {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Adaptation");
            powerAvailable(player, "Adaptation", 1);
        }
        //Lifestealer power
        else if (advancementId.equals("minecraft:nether/all_potions")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Lifestealer");
            powerAvailable(player, "Lifestealer", 7);
        }
        //Dragon power
        else if (advancementId.equals("minecraft:end/kill_dragon")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Dragon");
            powerAvailable(player, "Dragon", 3);
        } else if (advancementId.equals("minecraft:end/respawn_dragon")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Dragon");
            powerAvailable(player, "Dragon", 3);
        } else if (advancementId.equals("minecraft:end/dragon_breath")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Dragon");
            powerAvailable(player, "Dragon", 3);
        } else if (advancementId.equals("minecraft:end/dragon_heart")) {
            player.getData(ModDataAttachments.POWER_REQS).addReq("Dragon");
            powerAvailable(player, "Dragon", 3);
        }

    }
    public static void powerAvailable(ServerPlayer player, String power, int powerNumber)
    {
        if(player.getData(ModDataAttachments.POWER_REQS).getReqs(power) >= REQS_NEEDED[powerNumber])
        {
            player.getData(ModDataAttachments.AVAILABLE_POWERS).addPower(power);
            PacketDistributor.sendToPlayer(player, new AvailablePowersSyncPayload(player.getData(ModDataAttachments.AVAILABLE_POWERS)));
        }
    }
}
