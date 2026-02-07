package com.schnozz.identitiesmod.events.power_events.necromancer;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.datacomponent.CompoundTagListRecord;
import com.schnozz.identitiesmod.datacomponent.ModDataComponentRegistry;
import com.schnozz.identitiesmod.items.ItemRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.ArrayList;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNecroEvents {
    @SubscribeEvent
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        // Sets the component on melon seeds
        event.modify(ItemRegistry.MOB_HOLDER.get(), builder ->
                builder.set(ModDataComponentRegistry.HELD_LIST.get(), new CompoundTagListRecord(new ArrayList<>()))
        );
    }
}
