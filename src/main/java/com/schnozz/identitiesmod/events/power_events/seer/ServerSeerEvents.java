package com.schnozz.identitiesmod.events.power_events.seer;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.mixin.SeerEnchantmentMixin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerSeerEvents {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {return;}
        if (!player.getData(ModDataAttachments.POWER_TYPE).equals("Seer")) {return;}
        if (!(player.containerMenu instanceof EnchantmentMenu menu)) {return;}

        // Check your cached state here.
        // Only continue if the menu, input item, seed, or costs changed.

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

        // Send your completed payload only to this player:
        // PacketDistributor.sendToPlayer(player, payload);
    }
}
