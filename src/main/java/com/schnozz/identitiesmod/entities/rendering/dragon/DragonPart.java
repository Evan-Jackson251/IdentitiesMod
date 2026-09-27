package com.schnozz.identitiesmod.entities.rendering.dragon;

import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.entity.PartEntity;

public class DragonPart extends PartEntity<DragonEntity> {
    private final EntityDimensions dimensions;
    private final float damageMultiplier;

    public DragonPart(
            DragonEntity parent,
            float width,
            float height,
            float damageMultiplier
    ) {
        super(parent);

        this.dimensions = EntityDimensions.scalable(width, height);
        this.damageMultiplier = damageMultiplier;

        this.noPhysics = true;
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        // Entity construction may query this before our fields are assigned.
        if (dimensions == null) {
            return super.getDimensions(pose);
        }

        return dimensions.scale(getParent().getScale());
    }

    @Override
    public boolean isPickable() {
        return getParent().isAlive();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }

        return getParent().hurt(source, amount * damageMultiplier);
    }

    @Override
    public InteractionResult interact(
            Player player,
            InteractionHand hand
    ) {
        return getParent().interact(player, hand);
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || getParent() == entity;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
