package com.schnozz.identitiesmod.items.item_classes;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerTeam;
import com.schnozz.identitiesmod.menu.NomiconMenu;
import com.schnozz.identitiesmod.networking.snapshots.NomiconSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Nomicon extends Item {

    public Nomicon(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // openMenu only exists on ServerPlayer; the client just reports success
        // and waits for the server to push the screen.
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!"Necromancer".equals(serverPlayer.getData(ModDataAttachments.POWER_TYPE))) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.identitiesmod.not_a_necromancer"), true);
            return InteractionResultHolder.fail(stack);
        }

        NecromancerTeam team = serverPlayer.getData(ModDataAttachments.NECROMANCER_TEAM);

        serverPlayer.openMenu(
                new SimpleMenuProvider(
                        (windowId, inv, p) -> new NomiconMenu(windowId, inv, team),
                        Component.translatable("container.identitiesmod.nomicon")),
                buf -> NomiconSnapshot.write(buf, NomiconSnapshot.of(serverPlayer, team)));

        return InteractionResultHolder.consume(stack);
    }
}
