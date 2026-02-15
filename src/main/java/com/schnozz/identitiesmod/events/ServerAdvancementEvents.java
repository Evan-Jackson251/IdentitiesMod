package com.schnozz.identitiesmod.events;

import com.schnozz.identitiesmod.IdentitiesMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerAdvancementEvents {
    @SubscribeEvent
    public static void onAchievement(AdvancementEvent.AdvancementEarnEvent event)
    {
        String advancementId = event.getAdvancement().id().toString();
        System.out.println("ADVANCEMENT: " + advancementId);
        //Immortal power
        if(advancementId.equals("minecraft:adventure/totem_of_undying"))
        {

        }
        else if(advancementId.equals("minecraft:adventure/sleep_in_bed"))
        {

        }
        else if(advancementId.equals("minecraft:nether/charge_respawn_anchor"))
        {

        }
        //Gravity power
        else if(advancementId.equals("minecraft:adventure/fall_from_world_height"))
        {

        }
        //Time Lord power
        else if(advancementId.equals("minecraft:nether/create_full_beacon"))
        {

        }
        //Parry power
        else if(advancementId.equals("minecraft:story/deflect_arrow"))
        {

        }
        else if(advancementId.equals("minecraft:nether/return_to_sender"))
        {

        }
        else if(advancementId.equals("minecraft:adventure/avoid_vibration"))
        {

        }
        //Speedster power
        else if(advancementId.equals("minecraft:adventure/adventuring_time"))
        {

        }
        else if(advancementId.equals("minecraft:nether/fast_travel"))
        {

        }
        else if(advancementId.equals("minecraft:nether/explore_nether"))
        {

        }
        //Kyle power
        else if(advancementId.equals("minecraft:husbandry/plant_seed"))
        {

        }
        else if(advancementId.equals("minecraft:husbandry/obtain_netherite_hoe"))
        {

        }
        //Viltrumite power
        else if(advancementId.equals("minecraft:story/shiny_gear"))
        {

        }
        //Necromancer power
        else if(advancementId.equals("minecraft:adventure/voluntary_exile"))
        {

        }
        //Adaptation power
        else if(advancementId.equals("minecraft:adventure/hot_stuff"))//fire
        {

        }
        else if(advancementId.equals("minecraft:nether/brew_potion"))//magic
        {

        }
        else if(advancementId.equals("minecraft:adventure/honey_block_slide"))//fall
        {

        }
        else if(advancementId.equals("minecraft:nether/get_wither_skull"))//wither
        {

        }
        else if(advancementId.equals("minecraft:adventure/sniper_duel"))//arrow
        {

        }
        //Lifestealer power
        else if(advancementId.equals("minecraft:nether/all_potions"))
        {

        }
        //Dragon power
        else if(advancementId.equals("minecraft:end/kill_dragon"))
        {

        }
        else if(advancementId.equals("minecraft:end/respawn_dragon"))
        {

        }
        else if(advancementId.equals("minecraft:end/dragon_breath"))
        {

        }
        else if(advancementId.equals("minecraft:end/dragon_heart"))
        {

        }
    }
}
