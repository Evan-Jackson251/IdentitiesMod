package com.schnozz.identitiesmod.attachments;

import com.mojang.serialization.Codec;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModDataAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, IdentitiesMod.MODID);

    public static final Supplier<AttachmentType<Integer>> HEALTH_NEEDED = ATTACHMENT_TYPES.register(
            "needed_health", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<String>> POWER_TYPE = ATTACHMENT_TYPES.register(
            "power_type", () -> AttachmentType.builder(() -> "").serialize(Codec.STRING).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<CompoundTag>> ENTITY_HELD = ATTACHMENT_TYPES.register(
            "entity_data",
            () -> AttachmentType.builder(CompoundTag::new) // default = empty tag therefore not null
                    .build()
    );

    public static final Supplier<AttachmentType<CooldownAttachment>> COOLDOWN =
            ATTACHMENT_TYPES.register("cooldowns", () ->
                    AttachmentType.builder(CooldownAttachment::new)
                            .serialize(CooldownAttachment.CODEC)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<AdaptationAttachment>> ADAPTION =
            ATTACHMENT_TYPES.register("adaption", () ->
                    AttachmentType.builder(AdaptationAttachment::new)
                            .serialize(AdaptationAttachment.CODEC)
                            .build()
            );

    public static final Supplier<AttachmentType<LifestealerBuffsAttachment>> LIFESTEALER_BUFFS =
            ATTACHMENT_TYPES.register("lifestealer_buffs", () ->
                    AttachmentType.builder(LifestealerBuffsAttachment::new)
                            .serialize(LifestealerBuffsAttachment.CODEC)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<BlockPos>> HOME_POS = ATTACHMENT_TYPES.register(
            "home_pos", () -> AttachmentType.builder(() -> BlockPos.ZERO).serialize(BlockPos.CODEC).copyOnDeath().build()
    );

}
