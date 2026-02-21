package com.schnozz.identitiesmod.events.power_events.dragon;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import com.schnozz.identitiesmod.icons.ChargeIcon;
import com.schnozz.identitiesmod.icons.CooldownIcon;
import com.schnozz.identitiesmod.mob_effects.ModEffects;
import com.schnozz.identitiesmod.networking.payloads.*;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ChargeSyncPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.schnozz.identitiesmod.keymapping.ModMappings.DRAGON_BREATH;
import static com.schnozz.identitiesmod.keymapping.ModMappings.DRAGON_SHIFT;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientDragonEvents {
    //cooldown variables
    private static final int BITE_ATTACK_CD = 40;
    private static final int SHIFT_CD = 500;//18000
    private static final CooldownIcon SHIFT_ICON = new CooldownIcon(128, 272, 19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/dragon_head_icon.png"));
    private static final CooldownIcon BITE_ICON = new CooldownIcon(128, 272, 19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/bite_icon.png"));
    private static final ChargeIcon BREATH_ICON = new ChargeIcon(332, 259, 32, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/soul_flame_icon.png"), 0);

    //logic variables
    private static boolean flying = false;
    private static boolean wasShifted = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer dragonPlayer = Minecraft.getInstance().player;
        if (dragonPlayer == null) return;
        Minecraft mc = Minecraft.getInstance();

        if (dragonPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Dragon") && !dragonPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "shift_cd"),0)) {
            //spawn dragon
            if (!dragonPlayer.isPassenger() && DRAGON_SHIFT.get().consumeClick()) {
                PacketDistributor.sendToServer(new DragonSpawnPayload(dragonPlayer.getId()));

                dragonPlayer.removeAllEffects();
                PacketDistributor.sendToServer(new PotionNoVisPayload(ModEffects.SUPER_INVISIBILITY, 1, MobEffectInstance.INFINITE_DURATION));
                PacketDistributor.sendToServer(new PotionNoVisPayload(MobEffects.DAMAGE_RESISTANCE, 4, MobEffectInstance.INFINITE_DURATION));
                PacketDistributor.sendToServer(new PotionNoVisPayload(MobEffects.WEAKNESS, 255, MobEffectInstance.INFINITE_DURATION));

                dragonPlayer.setData(ModDataAttachments.CHARGE, 0.0);
                PacketDistributor.sendToServer(new ChargeSyncPayload(0.0));
            }

            //riding dragon
            if (dragonPlayer.getVehicle() instanceof DragonEntity dragon) {
                wasShifted = true;
                //third person
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);

                //bite attack
                if (mc.options.keyAttack.isDown() && !dragonPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "bite_cd"),0)) {
                    dragon.biteAttack(dragonPlayer);

                    long currentTime = Minecraft.getInstance().level.getGameTime();
                    CooldownAttachment atachment = new CooldownAttachment();
                    atachment.getAllCooldowns().putAll(dragonPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                    atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "bite_cd"), currentTime, BITE_ATTACK_CD);
                    dragonPlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                    PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, BITE_ATTACK_CD), ResourceLocation.fromNamespaceAndPath("identitiesmod", "bite_cd"), false));
                    BITE_ICON.setCooldown(new Cooldown(currentTime, BITE_ATTACK_CD));
                }

                //breath attack
                if (dragonPlayer.getData(ModDataAttachments.CHARGE) > 0 && DRAGON_BREATH.get().isDown()) {
                    dragon.dragonBreath(dragonPlayer);

                    double currentCharge = dragonPlayer.getData(ModDataAttachments.CHARGE);
                    dragonPlayer.setData(ModDataAttachments.CHARGE, currentCharge - 1);
                    PacketDistributor.sendToServer(new ChargeSyncPayload(currentCharge - 1));
                }

                //flying stuff
                if (mc.options.keySprint.isDown() && flying)//downward fly
                {
                    dragon.push(0, -0.1, 0);
                    PacketDistributor.sendToServer(new GravityPayload(dragon.getId(), 0, -0.1, 0));
                }
                if (mc.options.keyJump.consumeClick())//upward fly (sets flying to true)
                {
                    dragon.push(0, 0.5, 0);
                    PacketDistributor.sendToServer(new GravityPayload(dragon.getId(), 0, 0.5, 0));

                    flying = true;
                }
                if (flying) //prevents gravity while flying
                {
                    dragon.setOnGround(true);
                    PacketDistributor.sendToServer(new GroundedPayload(dragon.getId(), true));

                    dragon.push(0, 0.077, 0);
                    PacketDistributor.sendToServer(new GravityPayload(dragon.getId(), 0, 0.077, 0));

                    Vec3 velocity = dragon.getDeltaMovement();
                    if (velocity.y > 1) {
                        dragon.setDeltaMovement(velocity.x, 1, velocity.z);
                        PacketDistributor.sendToServer(new VelocityPayload(dragon.getId(), velocity.x, 1, velocity.z));
                    }
                    if (velocity.y < -1) {
                        dragon.setDeltaMovement(velocity.x, -1, velocity.z);
                        PacketDistributor.sendToServer(new VelocityPayload(dragon.getId(), velocity.x, -1, velocity.z));
                    }
                }
                if (DRAGON_SHIFT.get().consumeClick() && flying) //manually stops flying
                {
                    flying = false;
                }
            }
            else if(wasShifted)
            {
                wasShifted = false;
                flying = false;//important to prevent rocketing upwards from spawn collision

                //set shift cd
                long currentTime = Minecraft.getInstance().level.getGameTime();
                CooldownAttachment atachment = new CooldownAttachment();
                atachment.getAllCooldowns().putAll(dragonPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "shift_cd"), currentTime, SHIFT_CD);
                dragonPlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, SHIFT_CD), ResourceLocation.fromNamespaceAndPath("identitiesmod", "shift_cd"), false));
                SHIFT_ICON.setCooldown(new Cooldown(currentTime, SHIFT_CD));
            }
            //undo debuffs
            if(dragonPlayer.getAttribute(Attributes.MAX_HEALTH).getBaseValue() == 8F)
            {
                System.out.println("RESET HEALTH AND UNDO DEBUFF");
                PacketDistributor.sendToServer(new MaxHealthPayload(20));
                PacketDistributor.sendToServer(new PotionTogglePayload(MobEffects.WEAKNESS,0));
                PacketDistributor.sendToServer(new PotionTogglePayload(MobEffects.MOVEMENT_SLOWDOWN,0));
            }
            BREATH_ICON.setCharge(dragonPlayer.getData(ModDataAttachments.CHARGE));
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Player p = Minecraft.getInstance().player;
        if (p == null || !p.getData(ModDataAttachments.POWER_TYPE).equals("Dragon")) {
            return;
        }

        long gameTime = Minecraft.getInstance().level.getGameTime();
        GuiGraphics graphics = event.getGuiGraphics();

        if(p.getVehicle() instanceof DragonEntity){
            BITE_ICON.render(graphics, gameTime);
            BREATH_ICON.render(graphics);
        }
        else{
            SHIFT_ICON.render(graphics, gameTime);
        }
    }
}
