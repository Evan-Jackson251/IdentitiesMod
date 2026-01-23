package com.schnozz.identitiesmod.events.time_lord;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.damage_sources.ModDamageTypes;
import com.schnozz.identitiesmod.networking.payloads.EntityDamagePayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.TimeStopSyncPayload;
import com.schnozz.identitiesmod.screen.icon.CooldownIcon;
import com.schnozz.identitiesmod.util.EntitySnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.jar.Attributes;

import static com.schnozz.identitiesmod.events.time_lord.ServerTimeLordEvents.Time_Stop_Damage;
import static com.schnozz.identitiesmod.keymapping.ModMappings.REWIND_MAPPING;
import static com.schnozz.identitiesmod.keymapping.ModMappings.TIME_STOP_MAPPING;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientTimeLordEvents {
    private static final int STOP_DURATION = 120;
    private static int timeCounter = 0;
    private static final CooldownIcon TIME_STOP_COOLDOWN_ICON = new CooldownIcon(10, 10, 16, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/clock_icon.png"));
    private static final int TIME_STOP_CD = 1300;
    private static boolean rewindStored = false;
    private static EntitySnapshot snap;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer timePlayer = Minecraft.getInstance().player;
        if (timePlayer == null) return;
        Level level = timePlayer.level();
        if (!level.isClientSide()) return;

        String power = timePlayer.getData(ModDataAttachments.POWER_TYPE);
        if (power.equals("Time Lord")) {
            if(TIME_STOP_MAPPING.get().consumeClick() && !timePlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "time_stop_cd"),0))
            {
                timePlayer.setData(ModDataAttachments.TIME_STOP_STATE,1);
                PacketDistributor.sendToServer(new TimeStopSyncPayload(1));

                long currentTime = Minecraft.getInstance().level.getGameTime();
                CooldownAttachment atachment = new CooldownAttachment();
                atachment.getAllCooldowns().putAll(timePlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "time_stop_cd"), currentTime, TIME_STOP_CD);
                timePlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, TIME_STOP_CD), ResourceLocation.fromNamespaceAndPath("identitiesmod", "time_stop_cd"), false));
                TIME_STOP_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime, TIME_STOP_CD));
            }
            if(REWIND_MAPPING.get().consumeClick())
            {
                if(timePlayer.getData(ModDataAttachments.TIME_STOP_STATE) == 0)
                {
                    if(!rewindStored)
                    {
                        snap = new EntitySnapshot(timePlayer);
                        rewindStored = true;
                    }
                    else
                    {
                        snap.applySnapshot();
                        snap = null;
                        rewindStored = false;
                    }
                }
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
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        if(!Minecraft.getInstance().player.getData(ModDataAttachments.POWER_TYPE).equals("Time Lord"))
        {
            return;
        }

        long gameTime = Minecraft.getInstance().level.getGameTime();
        GuiGraphics graphics = event.getGuiGraphics();
        TIME_STOP_COOLDOWN_ICON.render(graphics, gameTime);
    }
}
