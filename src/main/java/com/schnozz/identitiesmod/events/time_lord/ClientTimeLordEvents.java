package com.schnozz.identitiesmod.events.time_lord;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.damage_sources.ModDamageTypes;
import com.schnozz.identitiesmod.networking.payloads.EntityDamagePayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.TimeStopSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Iterator;
import java.util.Map;

import static com.schnozz.identitiesmod.events.time_lord.ServerTimeLordEvents.Time_Stop_Damage;
import static com.schnozz.identitiesmod.keymapping.ModMappings.TIME_STOP_MAPPING;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientTimeLordEvents {
    private static final int STOP_DURATION = 120;
    private static int timeCounter = 0;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer timePlayer = Minecraft.getInstance().player;
        if (timePlayer == null) return;
        Level level = timePlayer.level();
        if (!level.isClientSide()) return;

        String power = timePlayer.getData(ModDataAttachments.POWER_TYPE);
        if (power.equals("Time Lord")) {
            if(TIME_STOP_MAPPING.get().consumeClick())
            {
                timePlayer.setData(ModDataAttachments.TIME_STOP_STATE,1);
                PacketDistributor.sendToServer(new TimeStopSyncPayload(1));
            }

            if(timePlayer.getData(ModDataAttachments.TIME_STOP_STATE) == 1)
            {
                timeCounter++;
                if(timeCounter >= STOP_DURATION)
                {
                    timeCounter = 0;
                    timePlayer.setData(ModDataAttachments.TIME_STOP_STATE,0);
                    PacketDistributor.sendToServer(new TimeStopSyncPayload(0));

                    //damage entities in map
                    for(Map.Entry<Integer,Float> damage: Time_Stop_Damage.entrySet())
                    {
                        Holder<DamageType> damageTypeHolder =
                                level.registryAccess()
                                        .registryOrThrow(Registries.DAMAGE_TYPE)
                                        .getHolderOrThrow(ModDamageTypes.TIME_DAMAGE);
                        System.out.println("Entity: " + level.getEntity(damage.getKey()));
                        System.out.println("Damage: " + damage.getValue());
                        PacketDistributor.sendToServer(new EntityDamagePayload(damage.getKey(),timePlayer.getId(),damage.getValue(),damageTypeHolder));
                    }
                }
            }
        }
    }
}
