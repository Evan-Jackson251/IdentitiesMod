package com.schnozz.identitiesmod.events.power_events.time_lord;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.damage_sources.ModDamageTypes;
import com.schnozz.identitiesmod.networking.payloads.EntityDamagePayload;
import com.schnozz.identitiesmod.networking.payloads.SoundPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.SnapShotSyncPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.TimeStopSyncPayload;
import com.schnozz.identitiesmod.icons.CooldownIcon;
import com.schnozz.identitiesmod.sounds.ModSounds;
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

import static com.schnozz.identitiesmod.events.power_events.time_lord.ServerTimeLordEvents.Time_Stop_Damage;
import static com.schnozz.identitiesmod.keymapping.ModMappings.*;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientTimeLordEvents {
    private static final int STOP_DURATION = 100;
    private static int timeCounter = 0;

    private static final CooldownIcon TIME_STOP_COOLDOWN_ICON = new CooldownIcon(128,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/clock_icon.png"));
    private static CooldownIcon SNAPSHOT_COOLDOWN_ICON = new CooldownIcon(88,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/open_chest_icon.png"));
    private static final CooldownIcon REWIND_COOLDOWN_ICON = new CooldownIcon(108,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/rewind_icon.png"));

    private static final int TIME_STOP_CD = 1500;
    private static final int REWIND_CD = 1200;//1200

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

                PacketDistributor.sendToServer(new SoundPayload(ModSounds.TIME_STOP_SOUND.get(),10F));
            }
            if(SNAPSHOT_MAPPING.get().consumeClick()) {
//                if(level.dimension() != Level.OVERWORLD)
//                {
//                    return;
//                }
                snap = snap.fromEntity(timePlayer);

                rewindStored = true;
                SNAPSHOT_COOLDOWN_ICON = new CooldownIcon(88,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/chest_icon.png"));
            }
            if(REWIND_MAPPING.get().consumeClick() && !timePlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "rewind_cd"),0))
            {
                if(timePlayer.getData(ModDataAttachments.TIME_STOP_STATE) == 0)
                {
                    if(rewindStored)
                    {
                        PacketDistributor.sendToServer(new SnapShotSyncPayload(snap));
                        snap = null;
                        rewindStored = false;

                        long currentTime = Minecraft.getInstance().level.getGameTime();
                        CooldownAttachment atachment = new CooldownAttachment();
                        atachment.getAllCooldowns().putAll(timePlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                        atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "rewind_cd"), currentTime, REWIND_CD);
                        timePlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                        PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, REWIND_CD), ResourceLocation.fromNamespaceAndPath("identitiesmod", "rewind_cd"), false));
                        REWIND_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime, REWIND_CD));

                        SNAPSHOT_COOLDOWN_ICON = new CooldownIcon(88,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/open_chest_icon.png"));
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
        SNAPSHOT_COOLDOWN_ICON.render(graphics, gameTime);
        REWIND_COOLDOWN_ICON.render(graphics, gameTime);
    }
}
