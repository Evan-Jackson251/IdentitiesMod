package com.schnozz.identitiesmod.entities.custom_goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class TargetEntityGoal extends Goal {
    private final Mob mob;
    private float speed;
    public TargetEntityGoal(Mob mob, LivingEntity entity,Float speed)
    {
        this.mob = mob;
        this.speed = speed;
        mob.getNavigation().stop();
        mob.setTarget(entity);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }
    @Override
    public void start(){
        LivingEntity target = mob.getTarget();
        if (target != null) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            mob.getNavigation().moveTo(target, speed);
        }
    }
    @Override
    public boolean canUse() {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }
    @Override
    public boolean canContinueToUse() {
        return canUse();
    }
}
