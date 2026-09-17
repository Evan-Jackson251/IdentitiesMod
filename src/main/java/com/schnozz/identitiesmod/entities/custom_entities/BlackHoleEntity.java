package com.schnozz.identitiesmod.entities.custom_entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class BlackHoleEntity extends Entity {
    public final float DAMAGE = 10F;
    public BlackHoleEntity(
            EntityType<? extends BlackHoleEntity> entityType,
            Level level
    ) {
        super(entityType, level);

        setNoGravity(true);
        noPhysics = true;
    }

    @Override
    public void tick()
    {
        super.tick();
        if(level().isClientSide()){
            return;
        }

//        if(tickCount < 40){
//            this.setInvisible(true);
//        }else{
//            this.setInvisible(false);
//        }

        for (Entity target : level().getEntities(
                this,
                getBoundingBox(),
                entity -> entity.isAlive()
                        && !entity.isSpectator()
                        && !(entity instanceof BlackHoleEntity)))
        {
            target.hurt(damageSources().outOfBorder(), DAMAGE);
        }

        if(tickCount >= 180){
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
