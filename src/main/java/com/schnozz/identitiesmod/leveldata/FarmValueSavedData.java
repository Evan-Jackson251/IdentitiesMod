package com.schnozz.identitiesmod.leveldata;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class FarmValueSavedData extends SavedData {
    private static final String FARM_VALUE_TAG = "FarmTag";
    private long value;

    public static FarmValueSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        FarmValueSavedData data = new FarmValueSavedData();
        if (tag.contains(FARM_VALUE_TAG, Tag.TAG_LONG)) {
            data.value = tag.getLong(FARM_VALUE_TAG);
        }
        return data;
    }

    // Save to NBT
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong(FARM_VALUE_TAG, value);
        return tag;
    }

    // Getter
    public long getValue() {
        return value;
    }

    public void addToValue(long add)
    {
        this.value += add;
        this.setDirty();
    }

    // Setter
    public void setValue(long newValue) {
        if (this.value != newValue) {
            this.value = newValue;
            this.setDirty(); // Mark as changed
        }
    }

    // Access from Overworld (or replace with get(Level) for dimension-specific)
    public static FarmValueSavedData get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD)
                .getDataStorage()
                .computeIfAbsent(new Factory<>(FarmValueSavedData::new, FarmValueSavedData::load), "farm_value_data");
    }
}
