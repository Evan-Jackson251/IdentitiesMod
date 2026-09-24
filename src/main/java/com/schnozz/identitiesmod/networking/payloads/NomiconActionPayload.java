package com.schnozz.identitiesmod.networking.payloads;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerTeam;
import com.schnozz.identitiesmod.leveldata.KillTallySavedData;
import com.schnozz.identitiesmod.menu.NomiconMenu;
import com.schnozz.identitiesmod.networking.snapshots.NomiconSnapshot;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

/**
 * Roster actions sent from the Nomicon screen. All validation happens here on
 * the server; the client is only asking.
 *
 * Register in RegisterPayloadHandlersEvent:
 *   registrar.playToServer(NomiconActionPayload.TYPE, NomiconActionPayload.STREAM_CODEC,
 *                          NomiconActionPayload::handle);
 */
public record NomiconActionPayload(Action action, int index, String typeId) implements CustomPacketPayload {

    public enum Action { SELECT, ADD, REMOVE, TOGGLE_ACTIVE }

    /** Kills of a type required to recruit one of that type. */
    public static final int COST_PER_MEMBER = 1;

    public static final Type<NomiconActionPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "nomicon_action"));

    public static final StreamCodec<ByteBuf, NomiconActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(NomiconActionPayload::actionById, Action::ordinal), NomiconActionPayload::action,
            ByteBufCodecs.VAR_INT, NomiconActionPayload::index,
            ByteBufCodecs.STRING_UTF8, NomiconActionPayload::typeId,
            NomiconActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ----------------------------------------------------------- constructors

    public static NomiconActionPayload select(int index) {
        return new NomiconActionPayload(Action.SELECT, index, "");
    }

    public static NomiconActionPayload remove(int index) {
        return new NomiconActionPayload(Action.REMOVE, index, "");
    }

    public static NomiconActionPayload toggle(int index) {
        return new NomiconActionPayload(Action.TOGGLE_ACTIVE, index, "");
    }

    public static NomiconActionPayload add(ResourceLocation typeId) {
        return new NomiconActionPayload(Action.ADD, -1, typeId.toString());
    }

    private static Action actionById(int id) {
        Action[] values = Action.values();
        return (id >= 0 && id < values.length) ? values[id] : Action.SELECT;
    }

    // ---------------------------------------------------------------- handling

    public static void handle(NomiconActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            // Never trust that the screen is actually open.
            if (!(player.containerMenu instanceof NomiconMenu menu)) {
                return;
            }

            NecromancerTeam team = player.getData(ModDataAttachments.NECROMANCER_TEAM);

            switch (payload.action()) {
                case SELECT -> menu.setSelected(payload.index());
                case ADD -> handleAdd(player, team, menu, payload.typeId());
                case REMOVE -> handleRemove(player, team, menu, payload.index());
                case TOGGLE_ACTIVE -> handleToggle(player, team, menu, payload.index());
            }
        });
    }

    private static void handleAdd(ServerPlayer player, NecromancerTeam team, NomiconMenu menu, String rawId) {
        if (team.isFull()) {
            player.displayClientMessage(Component.translatable("message.identitiesmod.team_full"), true);
            return;
        }

        ResourceLocation typeId = ResourceLocation.tryParse(rawId);
        if (typeId == null) {
            return;
        }

        Optional<EntityType<?>> maybeType = KillTallySavedData.typeOf(typeId);
        if (maybeType.isEmpty()) {
            return;
        }
        EntityType<?> type = maybeType.get();

        // Only hostile mobs can be raised. Drop this check if you want it looser.
        if (type.getCategory() != MobCategory.MONSTER) {
            return;
        }

        KillTallySavedData tally = KillTallySavedData.get(player.server);
        int have = tally.getKills(player.getUUID(), type);
        if (have < COST_PER_MEMBER) {
            player.displayClientMessage(
                    Component.translatable("message.identitiesmod.not_enough_kills",
                            COST_PER_MEMBER - have, type.getDescription()), true);
            return;
        }

        if (team.addMember(type)) {
            tally.setKills(player.getUUID(), type, have - COST_PER_MEMBER);
            sync(player, team, menu);
        }
    }

    private static void handleRemove(ServerPlayer player, NecromancerTeam team, NomiconMenu menu, int index) {
        NecromancerTeam.Member member = team.getMember(index);
        if (member == null) {
            return;
        }
        if (member.isActive()) {
            // Recall it first so its gear and entity are dealt with in one place.
            player.displayClientMessage(Component.translatable("message.identitiesmod.store_first"), true);
            return;
        }

        team.removeMember(index);

        // Gear goes back to the stash, or to the player if the stash is full.
        for (ItemStack stack : member.getGear()) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack leftover = team.depositToStash(stack);
            if (!leftover.isEmpty() && !player.getInventory().add(leftover)) {
                player.drop(leftover, false);
            }
        }

        menu.setSelected(-1);
        sync(player, team, menu);
    }

    private static void handleToggle(ServerPlayer player, NecromancerTeam team, NomiconMenu menu, int index) {
        NecromancerTeam.Member member = team.getMember(index);
        if (member == null) {
            return;
        }

        // Hand off to your power code, which owns spawning and despawning.
        // NecromancerSummons.setActive(player, member, !member.isActive());
        member.setActive(!member.isActive());

        sync(player, team, menu);
    }

    private static void sync(ServerPlayer player, NecromancerTeam team, NomiconMenu menu) {
        PacketDistributor.sendToPlayer(player, NomiconSnapshot.of(player, team));
        menu.broadcastChanges();
    }
}
