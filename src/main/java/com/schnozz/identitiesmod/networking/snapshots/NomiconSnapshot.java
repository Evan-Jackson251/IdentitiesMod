package com.schnozz.identitiesmod.networking.snapshots;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerTeam;
import com.schnozz.identitiesmod.leveldata.KillTallySavedData;
import com.schnozz.identitiesmod.menu.NomiconMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything the Nomicon screen needs that isn't an item slot: the roster and
 * the player's kill tally. Sent once in the openMenu buffer, then re-sent
 * whenever the server changes the roster.
 *
 * Register both directions in RegisterPayloadHandlersEvent:
 *   registrar.playToClient(NomiconSnapshot.TYPE, NomiconSnapshot.STREAM_CODEC, NomiconSnapshot::handle);
 */
public record NomiconSnapshot(List<Entry> roster, Map<ResourceLocation, Integer> tally)
        implements CustomPacketPayload {

    public record Entry(ResourceLocation typeId, boolean active) {}

    public static final Type<NomiconSnapshot> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "nomicon_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NomiconSnapshot> STREAM_CODEC =
            StreamCodec.of(NomiconSnapshot::write, NomiconSnapshot::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ------------------------------------------------------------------- build

    /** Builds the snapshot for a player from live server state. */
    public static NomiconSnapshot of(ServerPlayer player, NecromancerTeam team) {
        List<Entry> roster = new ArrayList<>();
        for (NecromancerTeam.Member member : team.getMembers()) {
            roster.add(new Entry(member.getTypeId(), member.isActive()));
        }

        Map<ResourceLocation, Integer> tally =
                new HashMap<>(KillTallySavedData.get(player.server).getKills(player.getUUID()));

        return new NomiconSnapshot(roster, tally);
    }

    public static NomiconSnapshot empty() {
        return new NomiconSnapshot(List.of(), Map.of());
    }

    // ------------------------------------------------------------------ codec

    public static void write(RegistryFriendlyByteBuf buf, NomiconSnapshot snapshot) {
        buf.writeVarInt(snapshot.roster.size());
        for (Entry entry : snapshot.roster) {
            buf.writeUtf(entry.typeId().toString());
            buf.writeBoolean(entry.active());
        }
        buf.writeVarInt(snapshot.tally.size());
        for (Map.Entry<ResourceLocation, Integer> e : snapshot.tally.entrySet()) {
            buf.writeUtf(e.getKey().toString());
            buf.writeVarInt(e.getValue());
        }
    }

    public static NomiconSnapshot read(RegistryFriendlyByteBuf buf) {
        int rosterSize = buf.readVarInt();
        List<Entry> roster = new ArrayList<>(rosterSize);
        for (int i = 0; i < rosterSize; i++) {
            ResourceLocation id = ResourceLocation.parse(buf.readUtf());
            roster.add(new Entry(id, buf.readBoolean()));
        }

        int tallySize = buf.readVarInt();
        Map<ResourceLocation, Integer> tally = new HashMap<>(tallySize);
        for (int i = 0; i < tallySize; i++) {
            ResourceLocation id = ResourceLocation.parse(buf.readUtf());
            tally.put(id, buf.readVarInt());
        }

        return new NomiconSnapshot(roster, tally);
    }

    // ---------------------------------------------------------------- handling

    /** Client side. Uses context.player() rather than Minecraft.getInstance() to stay Dist-safe. */
    public static void handle(NomiconSnapshot snapshot, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof NomiconMenu menu) {
                menu.applySnapshot(snapshot);
            }
        });
    }
}
