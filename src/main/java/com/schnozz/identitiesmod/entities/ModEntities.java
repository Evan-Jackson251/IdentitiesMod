package com.schnozz.identitiesmod.entities;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import com.schnozz.identitiesmod.entities.custom_entities.PlayerCloneEntity;
import com.schnozz.identitiesmod.entities.custom_entities.ThrownMobHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
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
                    .sized(10f, 6f).build("dragon"));

    public static final DeferredHolder<EntityType<?>, EntityType<PlayerCloneEntity>> PLAYER_CLONE =
            ENTITY_TYPES.register(
                    "player_clone",
                    () -> EntityType.Builder
                            .of(PlayerCloneEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .build("player_clone")
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
