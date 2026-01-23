package com.schnozz.identitiesmod.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class EntitySnapshot {
    private LivingEntity entity;
    private Vec3 pos;
    private Vec3 velocity;
    private float yRot;
    private float xRot;
    private float health;

    public EntitySnapshot(LivingEntity entity)
    {
        this.entity = entity;
        pos = entity.getPosition(0);
        velocity = entity.getDeltaMovement();
        yRot = entity.getYRot();
        xRot = entity.getXRot();
        health = entity.getHealth();
    }

    public void applySnapshot()
    {
        if(entity != null) {
            entity.setPos(pos);
            entity.setDeltaMovement(velocity);
            entity.setYRot(yRot);
            entity.setXRot(xRot);
            entity.setHealth(health);
        }
    }
}
