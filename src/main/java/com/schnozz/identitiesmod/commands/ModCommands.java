package com.schnozz.identitiesmod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.leveldata.FarmValueSavedData;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.leveldata.UUIDSavedData;
import com.schnozz.identitiesmod.networking.payloads.PotionLevelPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.PowerSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

@EventBusSubscriber(modid = "identitiesmod", bus = EventBusSubscriber.Bus.GAME)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("setpower")
                .requires(source -> source.hasPermission(2)) // OP level 2
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("power", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                    String power = StringArgumentType.getString(context, "power");

                                    target.setData(ModDataAttachments.POWER_TYPE, power);
                                    PacketDistributor.sendToPlayer(target, new PowerSyncPayload(power));
                                    System.out.println("Command was successfully executed");

                                    if(power.equals("Viltrumite"))
                                    {
                                        //perm strength 1
                                        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, MobEffectInstance.INFINITE_DURATION, 0, false, true,true));
                                        PacketDistributor.sendToPlayer(target,new PotionLevelPayload(MobEffects.DAMAGE_BOOST,0,MobEffectInstance.INFINITE_DURATION));
                                        //perm speed 1
                                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, MobEffectInstance.INFINITE_DURATION, 0, false, true,true));
                                        PacketDistributor.sendToPlayer(target,new PotionLevelPayload(MobEffects.MOVEMENT_SPEED,0,MobEffectInstance.INFINITE_DURATION));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(Commands.literal("afk")
                .requires(source -> source.hasPermission(0)) // allow all players, or raise for ops
                .executes(context -> {
                    ServerPlayer target = context.getSource().getPlayerOrException();

                    if(target.getTags().contains("AFK"))
                    {
                        target.removeTag("AFK");
                        target.sendSystemMessage(Component.literal("You are no longer AFK"));
                    }
                    else {
                        target.addTag("AFK");
                        target.sendSystemMessage(Component.literal("You are now AFK"));
                    }

                    return Command.SINGLE_SUCCESS;
                })
        );

        dispatcher.register(Commands.literal("deleteNecromancer")
                .requires(source -> source.hasPermission(2)) // OP level 2
                .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "target");

                                    UUIDSavedData savedData = UUIDSavedData.get(target.server);
                                    savedData.clearUUID();

                                    return Command.SINGLE_SUCCESS;
                                })));

        dispatcher.register(Commands.literal("sethome")
                .requires(source -> source.hasPermission(0)) // allow all players, or raise for ops
                .executes(context -> {
                    ServerPlayer target = context.getSource().getPlayerOrException();
                    if(target.level().dimension() == Level.OVERWORLD)
                    {
                        target.setData(ModDataAttachments.HOME_POS, target.getOnPos());
                    }
                    else
                    {
                        target.sendSystemMessage(Component.literal("Please set home in the overworld"));
                    }

                    return Command.SINGLE_SUCCESS;
                })
        );

        dispatcher.register(Commands.literal("home")
                .requires(source -> source.hasPermission(0)) // allow all players, or raise for ops
                .executes(context -> {
                    ServerPlayer target = context.getSource().getPlayerOrException();
                    if(target.getData(ModDataAttachments.HOME_POS) == BlockPos.ZERO) return Command.SINGLE_SUCCESS;

                    BlockPos home = target.getData(ModDataAttachments.HOME_POS);

                    if(!target.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "teleport_combat_tracked_cd"), target.level().getGameTime()))
                    {
                        target.teleportTo(target.getServer().getLevel(Level.OVERWORLD), home.getX(), home.getY() + 1,  home.getZ(), target.getXRot(), target.getYRot());
                    }
                    else {
                        target.sendSystemMessage(Component.literal("COMBAT LOGGED").withStyle(ChatFormatting.RED));
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );

        dispatcher.register(Commands.literal("spawn")
                .requires(source -> source.hasPermission(0)) // allow all players, or raise for ops
                .executes(context -> {
                    ServerPlayer target = context.getSource().getPlayerOrException();
                    BlockPos spawn = target.level().getSharedSpawnPos();
                    if(!target.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "teleport_combat_tracked_cd"), target.level().getGameTime()))
                    {
                        target.teleportTo(target.getServer().getLevel(Level.OVERWORLD), spawn.getX(), spawn.getY() + 1,  spawn.getZ(), target.getXRot(), target.getYRot());
                    }
                    else {
                        target.sendSystemMessage(Component.literal("COMBAT LOGGED").withStyle(ChatFormatting.RED));
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );


        dispatcher.register(Commands.literal("getpower")
                .requires(source -> source.hasPermission(2)) // OP level 2
                .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "target");

                                    target.sendSystemMessage(Component.literal(target.getData(ModDataAttachments.POWER_TYPE)));
                                    PacketDistributor.sendToPlayer(target, new PowerSyncPayload(target.getData(ModDataAttachments.POWER_TYPE)));

                                    return Command.SINGLE_SUCCESS;
                                })));

        dispatcher.register(Commands.literal("getKyleWorth")
                .requires(source -> source.hasPermission(0)) // OP level 2
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");

                            if(!target.level().isClientSide && target.getData(ModDataAttachments.POWER_TYPE).equals("Kyle"))
                            {
                                FarmValueSavedData data = FarmValueSavedData.get(target.getServer());
                                target.sendSystemMessage(Component.literal("Balance: " + data.getValue()));
                            }

                            return Command.SINGLE_SUCCESS;
                        })));
        dispatcher.register(Commands.literal("getAdaptationMap")
                .requires(source -> source.hasPermission(2)) // OP level 2
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");

                            if(!target.level().isClientSide && target.getData(ModDataAttachments.POWER_TYPE).equals("Adaptation"))
                            {
                                target.sendSystemMessage(Component.literal("Map: " + target.getData(ModDataAttachments.ADAPTION).getAllAdaptationValues()));
                            }

                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
