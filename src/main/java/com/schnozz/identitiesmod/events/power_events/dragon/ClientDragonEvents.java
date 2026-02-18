package com.schnozz.identitiesmod.events.power_events.dragon;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import com.schnozz.identitiesmod.mob_effects.ModEffects;
import com.schnozz.identitiesmod.networking.payloads.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.DRAGON_BREATH;
import static com.schnozz.identitiesmod.keymapping.ModMappings.DRAGON_SHIFT;

//NEED ICONS FOR EACH ABILITY AND TO CHANGE SOME FOR FLIGHT
@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientDragonEvents {
    private static final int BITE_ATTACK_CD = 40;
    private static int biteAttackCounter = -1;
    private static boolean flying = false;
    private static boolean shifted = false;
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

                dragonPlayer.removeAllEffects();
                PacketDistributor.sendToServer(new PotionNoVisPayload(ModEffects.SUPER_INVISIBILITY,1, MobEffectInstance.INFINITE_DURATION));
                PacketDistributor.sendToServer(new PotionNoVisPayload(MobEffects.DAMAGE_RESISTANCE,4, MobEffectInstance.INFINITE_DURATION));
                PacketDistributor.sendToServer(new PotionNoVisPayload(MobEffects.WEAKNESS,255, MobEffectInstance.INFINITE_DURATION));

                shifted = true;
            }
            //riding dragon
            if(dragonPlayer.isPassenger() && dragonPlayer.getVehicle() instanceof DragonEntity dragon)
            {
                //third person
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                //bite attack
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
                //breath attack
                if(DRAGON_BREATH.get().consumeClick())
                {
                    dragon.dragonBreath(dragonPlayer);
                }
                //flying stuff
                if(mc.options.keySprint.isDown() && flying)//downward fly
                {
                    dragon.push(0,-0.1,0);
                    PacketDistributor.sendToServer(new GravityPayload(dragon.getId(),0,-0.1,0));
                }
                if(mc.options.keyJump.consumeClick())//upward fly (sets flying to true)
                {
                    dragon.push(0,0.5,0);
                    PacketDistributor.sendToServer(new GravityPayload(dragon.getId(),0,0.5,0));

                    flying = true;
                }
                if(flying) //prevents gravity while flying
                {
                    dragon.setOnGround(true);
                    PacketDistributor.sendToServer(new GroundedPayload(dragon.getId(),true));

                    dragon.push(0,0.08,0);
                    PacketDistributor.sendToServer(new GravityPayload(dragon.getId(),0,0.08,0));

                    Vec3 velocity = dragon.getDeltaMovement();
                    if(velocity.y > 1)
                    {
                        dragon.setDeltaMovement(velocity.x,1,velocity.z);
                        PacketDistributor.sendToServer(new VelocityPayload(dragon.getId(),velocity.x,1,velocity.z));
                    }
                    if(velocity.y < -1)
                    {
                        dragon.setDeltaMovement(velocity.x,-1,velocity.z);
                        PacketDistributor.sendToServer(new VelocityPayload(dragon.getId(),velocity.x,-1,velocity.z));
                    }
                }
                if(DRAGON_SHIFT.get().consumeClick() && flying) //manually stops flying
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
