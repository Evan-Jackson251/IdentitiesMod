package com.schnozz.identitiesmod.entities;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.entities.custom.DragonEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, IdentitiesMod.MODID);

    public static final Supplier<EntityType<ThrownMobHolder>> THROW_MOB_HOLDER =
            ENTITY_TYPES.register("thrownmobholder", () -> EntityType.Builder.<ThrownMobHolder>of(ThrownMobHolder::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("thrownmobholder"));

    public static final Supplier<EntityType<DragonEntity>> DRAGON =
            ENTITY_TYPES.register("dragon",() -> EntityType.Builder.<DragonEntity>of(DragonEntity::new, MobCategory.MISC)
                    .sized(5f, 3f).build("dragon"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
