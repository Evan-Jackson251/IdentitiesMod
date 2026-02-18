package com.schnozz.identitiesmod.events.power_events.dragon;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import com.schnozz.identitiesmod.networking.payloads.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.DRAGON_SHIFT;

//NEED ICONS FOR EACH ABILITY AND TO CHANGE SOME FOR FLIGHT
@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientDragonEvents {
    private static final int BITE_ATTACK_CD = 50;
    private static int biteAttackCounter = -1;
    private static boolean flying = false;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer dragonPlayer = Minecraft.getInstance().player;
        if (dragonPlayer == null) return;
        Level level = dragonPlayer.level();
        Minecraft mc = Minecraft.getInstance();

        if(dragonPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Dragon")){
            //spawn dragon
            if(!dragonPlayer.isPassenger() && DRAGON_SHIFT.get().consumeClick()){
                PacketDistributor.sendToServer(new DragonSpawnPayload(dragonPlayer.getId()));
            }
            //riding dragon
            if(dragonPlayer.isPassenger() && dragonPlayer.getVehicle() instanceof DragonEntity dragon)
            {
                if(mc.options.keyAttack.isDown())
                {
                    if(biteAttackCounter < 0)
                    {
                        biteAttackCounter = BITE_ATTACK_CD;
                        dragon.biteAttack(dragonPlayer);
                    }
                    else{
                        biteAttackCounter--;
                    }
                }
                if(mc.options.keySprint.isDown() && flying)//fast fly (probably too op)
                {

                }
                if(mc.options.keyShift.isDown() && flying)//downward fly
                {

                }
                //upward fly (sets flying to true)
                if(mc.options.keyJump.consumeClick())
                {
                    dragon.push(0,1,0);
                    PacketDistributor.sendToServer(new GravityPayload(dragon.getId(),0,1,0));

                    flying = true;
                }
                if(flying) //prevents gravity while flying
                {
                    dragon.setOnGround(true);
                    PacketDistributor.sendToServer(new GroundedPayload(dragon.getId(),true));

                    dragon.push(0,0.08,0);
                    PacketDistributor.sendToServer(new GravityPayload(dragon.getId(),0,0.08,0));
                }
                if(DRAGON_SHIFT.get().consumeClick() && flying)
                {
                    flying = false;
                }
            }
            else{//important to prevent rocketing upwards from spawn collision
                flying = false;
            }
        }

    }
}
