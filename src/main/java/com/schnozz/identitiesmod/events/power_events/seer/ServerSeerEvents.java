package com.schnozz.identitiesmod.events.power_events.seer;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.mixin.SeerEnchantmentMixin;
import com.schnozz.identitiesmod.networking.payloads.HiddenEnchantsPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.PossessionEntitySyncPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.PossessionTimerSyncPayload;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;


@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerSeerEvents {
    private static final int POSSESSION_TIME = 100;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {return;}
        //Seer Player
        if (player.getData(ModDataAttachments.POWER_TYPE).equals("Seer")) {
            //In enchantment table
            if ((player.containerMenu instanceof EnchantmentMenu menu)) {
                ItemStack stack = menu.getSlot(0).getItem();
                List<List<EnchantmentInstance>> options = new ArrayList<>();

                for (int option = 0; option < 3; option++) {
                    List<EnchantmentInstance> enchantments =
                            stack.isEmpty() || menu.costs[option] <= 0
                                    ? List.of()
                                    : ((SeerEnchantmentMixin.EnchantmentMenuInvoker) menu)
                                    .identities$getEnchantmentList(
                                            player.registryAccess(),
                                            stack,
                                            option,
                                            menu.costs[option]
                                    );

                    options.add(enchantments);
                }
                PacketDistributor.sendToPlayer(player, new HiddenEnchantsPayload(menu.containerId, options));
            }
        }
        //!Seer Player
        else{
            if(player.getData(ModDataAttachments.POSSESSION_TIMER) > -1){
                //Get possessor
                if(player.level().getPlayerByUUID(player.getData(ModDataAttachments.POSSESSER_ENTITY)) == null)
                {return;}
                Player possessor = player.level().getPlayerByUUID(player.getData(ModDataAttachments.POSSESSER_ENTITY));

                //Possession control logic
                float yaw = possessor.getYRot();
                float pitch = possessor.getXRot();

                player.setYRot(yaw);
                player.setXRot(pitch);

                player.setYHeadRot(yaw);
                player.setYBodyRot(yaw);

                player.yRotO = yaw;
                player.xRotO = pitch;
                player.yHeadRotO = yaw;
                player.yBodyRotO = yaw;

                player.setSprinting(possessor.isSprinting());
                player.setShiftKeyDown(possessor.isShiftKeyDown());

                float forward = possessor.zza;
                float strafe = possessor.xxa;

                player.setSpeed(0.35F);
                player.travel(new Vec3(strafe, 0.0D, forward));

                //Increments timer
                int newTime = player.getData(ModDataAttachments.POSSESSION_TIMER)+1;
                player.setData(ModDataAttachments.POSSESSION_TIMER,newTime);
                PacketDistributor.sendToPlayer(player,new PossessionTimerSyncPayload(newTime));
                //End possession after final time variable
                if(player.getData(ModDataAttachments.POSSESSION_TIMER) > POSSESSION_TIME){
                    player.setData(ModDataAttachments.POSSESSION_TIMER,-1);
                    PacketDistributor.sendToPlayer(player,new PossessionTimerSyncPayload(-1));
                    player.setData(ModDataAttachments.POSSESSER_ENTITY, Util.NIL_UUID);
                    PacketDistributor.sendToPlayer(player, new PossessionEntitySyncPayload(Util.NIL_UUID));
                }
            }
        }
    }

}
