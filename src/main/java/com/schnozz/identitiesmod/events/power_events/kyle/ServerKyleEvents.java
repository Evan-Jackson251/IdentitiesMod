package com.schnozz.identitiesmod.events.power_events.kyle;


import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.datacomponent.ChargeRecord;
import com.schnozz.identitiesmod.datacomponent.ModDataComponentRegistry;
import com.schnozz.identitiesmod.items.item_classes.Scythe;
import com.schnozz.identitiesmod.leveldata.FarmValueSavedData;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/*
Kyle Plan:
    1. Add max cap (can't one shot netherite)
 */
@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerKyleEvents {
    @SubscribeEvent
    public static void onKyleDeath(LivingDeathEvent event)
    {
        if(!event.getEntity().level().isClientSide && event.getEntity().getData(ModDataAttachments.POWER_TYPE).equals("Kyle"))
        {
            if(event.getEntity().level() instanceof ServerLevel level)
            {
                FarmValueSavedData data = FarmValueSavedData.get(level.getServer());
                long toSubtract = ((long )(0.2 * data.getValue()));
                data.addToValue(-1 * toSubtract);
            }
        }
    }
    //Updates Scythe bonus damage
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event){
        Player kylePlayer = event.getEntity();
        if(kylePlayer.getData(ModDataAttachments.POWER_TYPE).equals("Kyle")){
            if(kylePlayer.getMainHandItem().getItem() instanceof Scythe && kylePlayer.getServer() != null){
                long farmValue = FarmValueSavedData.get(kylePlayer.getServer()).getValue();
                float bonusDamage = Math.min((float)(farmValue/2000),20F);
                kylePlayer.getMainHandItem().set(ModDataComponentRegistry.CHARGE, new ChargeRecord((int) bonusDamage));
            }
        }
    }
}
