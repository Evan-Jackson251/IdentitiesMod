package com.schnozz.identitiesmod.attachments;

import com.mojang.serialization.Codec;

import java.util.ArrayList;

public class AvailablePowersAttachment {
    private ArrayList<String> AVAILABLE_POWERS = new ArrayList<>();

    public ArrayList<String> getAvailablePowers()
    {
        return AVAILABLE_POWERS;
    }
    public void addPower(String power)
    {
        AVAILABLE_POWERS.add(power);
    }
    public void removePower(String power)
    {
        AVAILABLE_POWERS.remove(power);
    }
    public void clearPowers(){AVAILABLE_POWERS.clear();}

    private static final Codec<ArrayList<String>> POWER_LIST_CODEC =
            Codec.STRING.listOf().xmap(ArrayList::new, list -> list);
    public static final Codec<AvailablePowersAttachment> CODEC =
            POWER_LIST_CODEC.xmap(
                    list -> {
                        AvailablePowersAttachment attachment = new AvailablePowersAttachment();
                        attachment.AVAILABLE_POWERS.addAll(list);
                        return attachment;
                    },
                    AvailablePowersAttachment::getAvailablePowers
            );
}
