package com.schnozz.identitiesmod.networking;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.entities.ModEntities;
import com.schnozz.identitiesmod.entities.custom_entities.PlayerCloneEntity;
import com.schnozz.identitiesmod.events.power_events.parry.ClientParryEvents;
import com.schnozz.identitiesmod.events.power_events.viltrumite.ClientViltrumiteEvents;
import com.schnozz.identitiesmod.networking.handlers.*;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.*;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.*;
import com.schnozz.identitiesmod.networking.payloads.CDPARRYPayload;
import com.schnozz.identitiesmod.networking.payloads.CDPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PayloadRegister {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.NETWORK); // Replace "1" with your network protocol version

        registrar.playToClient(
                PowerSyncPayload.TYPE,
                PowerSyncPayload.STREAM_CODEC,
                (payload, context) -> {
                    // Schedule work on the main client thread
                    Minecraft.getInstance().execute(() -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player != null) {
                            player.setData(ModDataAttachments.POWER_TYPE.get(), payload.power());
                        }
                    });
                }
        );

        registrar.playBidirectional(
                AdaptationSyncPayload.TYPE,
                AdaptationSyncPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientAdaptationSyncHandler::handle,
                        ServerAdaptationSyncHandler::handle
                )
        );
        registrar.playBidirectional(
                ClonesSyncPayload.TYPE,
                ClonesSyncPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientClonesSyncHandler::handle,
                        ServerClonesSyncHandler::handle
                )
        );

        registrar.playBidirectional(
                LifestealerBuffSyncPayload.TYPE,
                LifestealerBuffSyncPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientLifestealerBuffSyncPayload::handle,
                        ServerLifestealerBuffSyncHandler::handle
                )
        );

        registrar.playBidirectional(
                ChargeSyncPayload.TYPE,
                ChargeSyncPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientChargeSyncHandler::handle,
                        ServerChargeSyncHandler::handle
                )
        );

        registrar.playToClient(
                CDPayload.TYPE,
                CDPayload.STREAM_CODEC,
                (payload, context) -> {
                    // Schedule work on the main client thread
                    Minecraft.getInstance().execute(() -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player != null) {
                            ClientViltrumiteEvents.setIconCooldown(payload.cooldown());
                        }
                    });
                }
        );

        registrar.playToClient(
                CDPARRYPayload.TYPE,
                CDPARRYPayload.STREAM_CODEC,
                (payload, context) -> {
                    // Schedule work on the main client thread
                    Minecraft.getInstance().execute(() -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player != null) {
                            ClientParryEvents.setIconCooldown(payload.cooldown());
                        }
                    });
                }
        );

        registrar.playToServer(
                SoundPayload.TYPE,
                SoundPayload.STREAM_CODEC,
                (payload, context) -> {
                    // Schedule work on the main client thread
                    Player player = context.player();
                    if (player != null) {
                        player.level().playSound(null, player.getOnPos(), payload.sound(), SoundSource.PLAYERS, payload.volume(),1F);
                    }
                }
        );

        registrar.playToServer(
                HealthCostPayload.TYPE,
                HealthCostPayload.STREAM_CODEC,
                (payload, context) -> {
                    Player p = context.player();
                    int currentHealthNeeded = p.getData(ModDataAttachments.HEALTH_NEEDED);
                    p.setData(ModDataAttachments.HEALTH_NEEDED, currentHealthNeeded - payload.cost());
                    p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20 + p.getData(ModDataAttachments.HEALTH_NEEDED));
                }
        );

        registrar.playToServer(
                ClonePayload.TYPE,
                ClonePayload.STREAM_CODEC,
                (payload, context) -> {
                    Level level = context.player().level();
                    ServerPlayer clonePlayer = (ServerPlayer) level.getEntity(payload.userID());
                    if (clonePlayer == null) return;

                    PlayerCloneEntity clone = ModEntities.PLAYER_CLONE.value().create(level);
                    if (clone == null) return;
                    clone.moveTo(
                            clonePlayer.getX(),
                            clonePlayer.getY(),
                            clonePlayer.getZ(),
                            clonePlayer.getYRot(),
                            clonePlayer.getXRot()
                    );
                    clone.copyIdentityFrom(clonePlayer);
                    clone.copyEquipmentFrom(clonePlayer);

                    clone.setCustomName(clonePlayer.getName());
                    clone.setCustomNameVisible(true);
                    clone.setCreatorId(clonePlayer.getId());

                    //clone.followPlayer(clonePlayer);

                    level.addFreshEntity(clone);

                    level.getServer().execute(() -> {
                        clonePlayer.getData(ModDataAttachments.CLONES)
                                .addClone(clone.getId());
                        PacketDistributor.sendToPlayer(clonePlayer,new ClonesSyncPayload(clonePlayer.getData(ModDataAttachments.CLONES)));
                    });
                }
        );

        registrar.playToServer(
                VelocityPayload.TYPE,
                VelocityPayload.STREAM_CODEC,
                ServerVelocityHandler::handle
        );
        registrar.playToServer(
                CloneCommandPayload.TYPE,
                CloneCommandPayload.STREAM_CODEC,
                ServerCloneCommandHandler::handle
        );

        registrar.playToServer(
                KyleWorthPayload.TYPE,
                KyleWorthPayload.STREAM_CODEC,
                ServerKyleWorthHandler::handle
        );

        registrar.playToServer(
                WeaknessEffectPayload.TYPE,
                WeaknessEffectPayload.STREAM_CODEC,
                ServerWeaknessEffectHandler::handle
        );

        registrar.playToServer(
                DripstoneDropPayload.TYPE,
                DripstoneDropPayload.STREAM_CODEC,
                ServerDripstoneDropHandler::handle
        );

        registrar.playToServer(
                GravityArrowPayload.TYPE,
                GravityArrowPayload.STREAM_CODEC,
                ServerGravityArrowHandler::handle
        );

        registrar.playToServer(
                ParryParticlePayload.TYPE,
                ParryParticlePayload.STREAM_CODEC,
                ServerParryParticleHandler::handle
        );

        registrar.playToServer(
                ChaosParticlePayload.TYPE,
                ChaosParticlePayload.STREAM_CODEC,
                ServerChaosParticleHandler::handle
        );

        registrar.playToServer(
                SnapShotSyncPayload.TYPE,
                SnapShotSyncPayload.STREAM_CODEC,
                ServerSnapShotSyncHandler::handle
        );

        registrar.playToServer(
                EntityBoxPayload.TYPE,
                EntityBoxPayload.STREAM_CODEC,
                (payload, context) -> {
                        Player player = context.player();
                        player.setData(ModDataAttachments.ENTITY_HELD.get(), payload.entity());
                }
        );

        registrar.playToServer(
                GravityPayload.TYPE,
                GravityPayload.STREAM_CODEC,
                    ServerGravityHandler::handle
        );

        registrar.playToServer(
                EntityDamagePayload.TYPE,
                EntityDamagePayload.STREAM_CODEC,
                ServerEntityDamageHandler::handle
        );

        registrar.playBidirectional(
                PotionLevelPayload.TYPE,
                PotionLevelPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientPotionLevelHandler::handle,
                        ServerPotionLevelHandler::handle
                )
        );

        registrar.playBidirectional(
                CooldownSyncPayload.TYPE,
                CooldownSyncPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientCooldownHandler::handle,
                        ServerCooldownHandler::handle
                )
        );

        registrar.playBidirectional(
                SpeedsterLightningSync.TYPE,
                SpeedsterLightningSync.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientSpeedsterLightningHandler::handle,
                        ServerSpeedsterLightningHandler::handle
                )
        );

        registrar.playBidirectional(
                TimeStopSyncPayload.TYPE,
                TimeStopSyncPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientTimeStopHandler::handle,
                        ServerTimeStopHandler::handle
                )
        );

        registrar.playToServer(
                PotionTogglePayload.TYPE,
                PotionTogglePayload.STREAM_CODEC,
                ServerPotionToggleHandler::handle
        );

        registrar.playToServer(
                StunPayload.TYPE,
                StunPayload.STREAM_CODEC,
                ServerStunHandler::handle
        );

        registrar.playToServer(
                EntityPositionPayload.TYPE,
                EntityPositionPayload.STREAM_CODEC,
                ServerEntityPositionHandler::handle
        );

        registrar.playToClient(
                GrabSyncPayload.TYPE,
                GrabSyncPayload.STREAM_CODEC,
                ClientGrabSyncPayloadHandler::handle
        );
    }
}
