package com.schnozz.identitiesmod.leveldata;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class UUIDSavedData extends SavedData {
    private static final String OWNERS_TAG = "Owners";
    private static final String DATA_NAME = "necromancer_list";

    private final Map<UUID, List<UUID>> controlled = new HashMap<>();

    // ---------------------------------------------------------------- load/save

    public static UUIDSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        UUIDSavedData data = new UUIDSavedData();

        if (!tag.contains(OWNERS_TAG, Tag.TAG_COMPOUND)) {
            return data;
        }

        CompoundTag owners = tag.getCompound(OWNERS_TAG);
        for (String key : owners.getAllKeys()) {
            UUID owner;
            try {
                owner = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue; // malformed owner key, skip
            }

            ListTag mobTags = owners.getList(key, Tag.TAG_STRING);
            List<UUID> mobs = new ArrayList<>(mobTags.size());
            for (int i = 0; i < mobTags.size(); i++) {
                try {
                    UUID mob = UUID.fromString(mobTags.getString(i));
                    if (!mobs.contains(mob)) {
                        mobs.add(mob);
                    }
                } catch (IllegalArgumentException e) {
                    // malformed mob UUID, skip
                }
            }

            if (!mobs.isEmpty()) {
                data.controlled.put(owner, mobs);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag owners = new CompoundTag();

        for (Map.Entry<UUID, List<UUID>> entry : controlled.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue; // don't persist empty entries
            }
            ListTag mobTags = new ListTag();
            for (UUID mob : entry.getValue()) {
                mobTags.add(StringTag.valueOf(mob.toString()));
            }
            owners.put(entry.getKey().toString(), mobTags);
        }

        tag.put(OWNERS_TAG, owners);
        return tag;
    }

    // ------------------------------------------------------------------ mutators

    /** Associates a mob with an owner. Returns true if something changed. */
    public boolean addMob(UUID owner, UUID mob) {
        List<UUID> mobs = controlled.computeIfAbsent(owner, k -> new ArrayList<>());
        if (mobs.contains(mob)) {
            return false;
        }
        mobs.add(mob);
        this.setDirty();
        return true;
    }

    /** Removes a mob from a specific owner. */
    public boolean removeMob(UUID owner, UUID mob) {
        List<UUID> mobs = controlled.get(owner);
        if (mobs == null || !mobs.remove(mob)) {
            return false;
        }
        if (mobs.isEmpty()) {
            controlled.remove(owner);
        }
        this.setDirty();
        return true;
    }

    /** Removes a mob from whoever owns it (useful on mob death). */
    public boolean removeMob(UUID mob) {
        boolean changed = false;
        Iterator<Map.Entry<UUID, List<UUID>>> it = controlled.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, List<UUID>> entry = it.next();
            if (entry.getValue().remove(mob)) {
                changed = true;
                if (entry.getValue().isEmpty()) {
                    it.remove();
                }
            }
        }
        if (changed) {
            this.setDirty();
        }
        return changed;
    }

    /** Drops every mob belonging to one owner. */
    public void clearMobs(UUID owner) {
        if (controlled.remove(owner) != null) {
            this.setDirty();
        }
    }

    /** Drops everything for every owner. */
    public void clearAll() {
        if (!controlled.isEmpty()) {
            controlled.clear();
            this.setDirty();
        }
    }

    /** Replaces an owner's whole list at once. */
    public void setMobs(UUID owner, List<UUID> mobs) {
        if (mobs == null || mobs.isEmpty()) {
            clearMobs(owner);
            return;
        }
        List<UUID> copy = new ArrayList<>();
        for (UUID mob : mobs) {
            if (!copy.contains(mob)) {
                copy.add(mob);
            }
        }
        controlled.put(owner, copy);
        this.setDirty();
    }

    // ------------------------------------------------------------------ queries

    /** Read-only view; never null. Mutating it won't mark the data dirty, so it's immutable. */
    public List<UUID> getMobs(UUID owner) {
        List<UUID> mobs = controlled.get(owner);
        return mobs == null ? List.of() : Collections.unmodifiableList(mobs);
    }

    public boolean controls(UUID owner, UUID mob) {
        List<UUID> mobs = controlled.get(owner);
        return mobs != null && mobs.contains(mob);
    }

    public int getMobCount(UUID owner) {
        List<UUID> mobs = controlled.get(owner);
        return mobs == null ? 0 : mobs.size();
    }

    /** Reverse lookup: which player owns this mob, if any. */
    public Optional<UUID> getOwnerOf(UUID mob) {
        for (Map.Entry<UUID, List<UUID>> entry : controlled.entrySet()) {
            if (entry.getValue().contains(mob)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public Set<UUID> getOwners() {
        return Collections.unmodifiableSet(controlled.keySet());
    }

    public Map<UUID, List<UUID>> getAll() {
        return Collections.unmodifiableMap(controlled);
    }

    // ------------------------------------------------------------------- access

    public static UUIDSavedData get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD)
                .getDataStorage()
                .computeIfAbsent(new Factory<>(UUIDSavedData::new, UUIDSavedData::load), DATA_NAME);
    }
}
