package com.schnozz.identitiesmod.leveldata;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class KillTallySavedData extends SavedData {
    private static final String PLAYERS_TAG = "Players";
    private static final String DATA_NAME = "necromancer_kill_tally";

    private final Map<UUID, Map<ResourceLocation, Integer>> tallies = new HashMap<>();

    // ---------------------------------------------------------------- load/save

    public static KillTallySavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        KillTallySavedData data = new KillTallySavedData();

        if (!tag.contains(PLAYERS_TAG, Tag.TAG_COMPOUND)) {
            return data;
        }

        CompoundTag players = tag.getCompound(PLAYERS_TAG);
        for (String playerKey : players.getAllKeys()) {
            UUID player;
            try {
                player = UUID.fromString(playerKey);
            } catch (IllegalArgumentException e) {
                continue; // malformed player key
            }

            CompoundTag counts = players.getCompound(playerKey);
            Map<ResourceLocation, Integer> byType = new HashMap<>();
            for (String typeKey : counts.getAllKeys()) {
                ResourceLocation typeId = ResourceLocation.tryParse(typeKey);
                if (typeId == null) {
                    continue; // malformed registry id
                }
                int count = counts.getInt(typeKey);
                if (count > 0) {
                    byType.put(typeId, count);
                }
            }

            if (!byType.isEmpty()) {
                data.tallies.put(player, byType);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();

        for (Map.Entry<UUID, Map<ResourceLocation, Integer>> entry : tallies.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            CompoundTag counts = new CompoundTag();
            for (Map.Entry<ResourceLocation, Integer> typeEntry : entry.getValue().entrySet()) {
                counts.putInt(typeEntry.getKey().toString(), typeEntry.getValue());
            }
            players.put(entry.getKey().toString(), counts);
        }

        tag.put(PLAYERS_TAG, players);
        return tag;
    }

    // ------------------------------------------------------------------ recording

    /** Records one kill. Returns the new total for that type. */
    public int addKill(UUID player, EntityType<?> type) {
        return addKills(player, type, 1);
    }

    /** Convenience overload for use straight from a death event. */
    public int addKill(UUID player, Entity victim) {
        return addKills(player, victim.getType(), 1);
    }

    public int addKills(UUID player, EntityType<?> type, int amount) {
        if (amount <= 0) {
            return getKills(player, type);
        }
        ResourceLocation typeId = idOf(type);
        Map<ResourceLocation, Integer> byType = tallies.computeIfAbsent(player, k -> new HashMap<>());
        int updated = byType.merge(typeId, amount, Integer::sum);
        this.setDirty();
        return updated;
    }

    // ------------------------------------------------------------------- queries

    /** How many of this type the player has killed. Zero if never. */
    public int getKills(UUID player, EntityType<?> type) {
        return getKills(player, idOf(type));
    }

    public int getKills(UUID player, ResourceLocation typeId) {
        Map<ResourceLocation, Integer> byType = tallies.get(player);
        if (byType == null) {
            return 0;
        }
        return byType.getOrDefault(typeId, 0);
    }

    /** Every type this player has killed, as a read-only view. */
    public Map<ResourceLocation, Integer> getKills(UUID player) {
        Map<ResourceLocation, Integer> byType = tallies.get(player);
        return byType == null ? Map.of() : Collections.unmodifiableMap(byType);
    }

    /** Total kills across all types. */
    public int getTotalKills(UUID player) {
        Map<ResourceLocation, Integer> byType = tallies.get(player);
        if (byType == null) {
            return 0;
        }
        int total = 0;
        for (int count : byType.values()) {
            total += count;
        }
        return total;
    }

    /** How many distinct types this player has killed. */
    public int getDistinctTypeCount(UUID player) {
        Map<ResourceLocation, Integer> byType = tallies.get(player);
        return byType == null ? 0 : byType.size();
    }

    /** The type this player has killed most, if any. */
    public Optional<Map.Entry<ResourceLocation, Integer>> getMostKilled(UUID player) {
        Map<ResourceLocation, Integer> byType = tallies.get(player);
        if (byType == null || byType.isEmpty()) {
            return Optional.empty();
        }
        return byType.entrySet().stream().max(Map.Entry.comparingByValue());
    }

    /** Cross-player lookup: who has killed this type, and how often. */
    public Map<UUID, Integer> getKillsByPlayer(EntityType<?> type) {
        ResourceLocation typeId = idOf(type);
        Map<UUID, Integer> result = new HashMap<>();
        for (Map.Entry<UUID, Map<ResourceLocation, Integer>> entry : tallies.entrySet()) {
            Integer count = entry.getValue().get(typeId);
            if (count != null && count > 0) {
                result.put(entry.getKey(), count);
            }
        }
        return result;
    }

    public boolean hasData(UUID player) {
        Map<ResourceLocation, Integer> byType = tallies.get(player);
        return byType != null && !byType.isEmpty();
    }

    // ------------------------------------------------------------------ mutators

    /** Overwrites the count for one type. Zero or less removes the entry. */
    public void setKills(UUID player, EntityType<?> type, int count) {
        ResourceLocation typeId = idOf(type);
        if (count <= 0) {
            Map<ResourceLocation, Integer> byType = tallies.get(player);
            if (byType != null && byType.remove(typeId) != null) {
                if (byType.isEmpty()) {
                    tallies.remove(player);
                }
                this.setDirty();
            }
            return;
        }
        tallies.computeIfAbsent(player, k -> new HashMap<>()).put(typeId, count);
        this.setDirty();
    }

    public void clearPlayer(UUID player) {
        if (tallies.remove(player) != null) {
            this.setDirty();
        }
    }

    public void clearAll() {
        if (!tallies.isEmpty()) {
            tallies.clear();
            this.setDirty();
        }
    }

    // -------------------------------------------------------------------- helpers

    private static ResourceLocation idOf(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type);
    }

    /** Resolve a stored id back to a type. Empty if that entity no longer exists. */
    public static Optional<EntityType<?>> typeOf(ResourceLocation typeId) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(typeId);
    }

    // --------------------------------------------------------------------- access

    public static KillTallySavedData get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD)
                .getDataStorage()
                .computeIfAbsent(new Factory<>(KillTallySavedData::new, KillTallySavedData::load), DATA_NAME);
    }
}
