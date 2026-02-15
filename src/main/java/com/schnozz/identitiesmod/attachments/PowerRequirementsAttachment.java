package com.schnozz.identitiesmod.attachments;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class PowerRequirementsAttachment {
    private Map<String,Integer> POWER_REQS = new HashMap<>();

    public int getReqs(String power){
        return POWER_REQS.get(power);
    }
    public int addReq(String power){
        return POWER_REQS.compute(power, (k, v) -> (v == null ? 1 : v + 1));
    }
    public Map<String,Integer> getAllReqs()
    {
        return POWER_REQS;
    }
    public static final Codec<PowerRequirementsAttachment> CODEC = Codec.unboundedMap(
            Codec.STRING,
            Codec.INT
    ).xmap(map -> {
        PowerRequirementsAttachment att = new PowerRequirementsAttachment();
        att.POWER_REQS.putAll(map);
        return att;
    }, PowerRequirementsAttachment::getAllReqs);
}
