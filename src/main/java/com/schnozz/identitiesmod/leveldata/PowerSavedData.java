package com.schnozz.identitiesmod.leveldata;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;

public class PowerSavedData extends SavedData {
    private static final String DATA_NAME = "powers_taken_list";

    private ArrayList<String> powers_taken = new ArrayList<>();

    public static PowerSavedData create() {
        return new PowerSavedData();
    }

    public static PowerSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PowerSavedData data = new PowerSavedData();
        ListTag listTag = tag.getList("powers_taken", Tag.TAG_STRING);
        for (int i = 0; i < listTag.size(); i++) {
            data.powers_taken.add(listTag.getString(i));
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();

        for (String power : powers_taken) {
            listTag.add(StringTag.valueOf(power));
        }

        tag.put("powers_taken", listTag);
        return tag;
    }

    public static PowerSavedData get(ServerLevel level) {
        return level.getServer().overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<PowerSavedData>(
                                PowerSavedData::new,
                                PowerSavedData::load
                        ),
                        DATA_NAME
                );
    }

    public ArrayList<String> getPowersTaken() {
        return powers_taken;
    }
    public void addPowerTaken(String power)
    {
        powers_taken.add(power);
        setDirty();
    }
    public void removePowerTaken(String power) {
        if (powers_taken.remove(power)) {
            setDirty();
        }
    }
    public boolean powerIsTaken(String power) {
        return powers_taken.contains(power);
    }
}
