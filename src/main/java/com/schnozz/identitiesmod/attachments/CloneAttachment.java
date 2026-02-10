package com.schnozz.identitiesmod.attachments;

import com.mojang.serialization.Codec;
import java.util.ArrayList;

public class CloneAttachment {
    private ArrayList<Integer> CLONES = new ArrayList<>();

    public ArrayList<Integer> getClones()
    {
        return CLONES;
    }
    public void addClone(Integer cloneId)
    {
        CLONES.add(cloneId);
    }
    public void removeClone(Integer cloneId)
    {
        CLONES.remove(cloneId);
    }
    public void clearClones(){CLONES.clear();}

    private static final Codec<ArrayList<Integer>> CLONE_LIST_CODEC =
            Codec.INT.listOf().xmap(ArrayList::new, list -> list);
    public static final Codec<CloneAttachment> CODEC =
            CLONE_LIST_CODEC.xmap(
                    list -> {
                        CloneAttachment attachment = new CloneAttachment();
                        attachment.CLONES.addAll(list);
                        return attachment;
                    },
                    CloneAttachment::getClones
            );
}
