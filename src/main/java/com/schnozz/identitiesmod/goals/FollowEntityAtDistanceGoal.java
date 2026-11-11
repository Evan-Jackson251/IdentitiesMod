package com.schnozz.identitiesmod.goals;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.EnumSet;

/**
 * Keeps a mob near a leader (the necromancer) whenever it has nothing to fight.
 *
 * Belongs in goalSelector, not targetSelector. It claims MOVE so it doesn't fight random
 * strolling, and it switches itself off while the mob has a live target so attack goals
 * get full control of movement.
 */
public class FollowEntityAtDistanceGoal extends Goal {
    private final Mob mob;
    private final Entity leader;   // renamed from "target" so it isn't confused with mob.getTarget()
    private final double speed;
    private final float minDistance;
    private int timeToRecalcPath;

    public FollowEntityAtDistanceGoal(Mob mob, Entity leader, double speed, float minDistance) {
        this.mob = mob;
        this.leader = leader;
        this.speed = speed;
        this.minDistance = minDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !hasLiveTarget() && leaderReachable() && tooFarFromLeader();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
    }

    @Override
    public void tick() {
        mob.getLookControl().setLookAt(leader, 10.0F, (float) mob.getMaxHeadXRot());

        // Re-path every half second instead of every update. Pathfinding is expensive,
        // and with a full team, re-pathing constantly adds up.
        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = adjustedTickDelay(10);
            mob.getNavigation().moveTo(leader, speed);
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * True while the mob has something alive to fight. Also clears a dead target, so the
     * mob doesn't stand around holding a reference to a corpse.
     */
    private boolean hasLiveTarget() {
        LivingEntity target = mob.getTarget();
        if (target != null) {
            if (target.isAlive() && target.level() == mob.level()) {
                return true;
            }
            mob.setTarget(null);
        }

        // Brain-based mobs (piglins, hoglins) keep their target in a memory instead.
        // hasMemoryValue is safe on mobs that don't use this memory; getMemory is not.
        Brain<?> brain = mob.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            LivingEntity brainTarget = brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (brainTarget != null && brainTarget.isAlive()) {
                return true;
            }
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
        return false;
    }

    /** Leader still exists and is in the same dimension (distances across dimensions are meaningless). */
    private boolean leaderReachable() {
        return leader != null && leader.isAlive() && leader.level() == mob.level();
    }

    private boolean tooFarFromLeader() {
        return mob.distanceToSqr(leader) > (double) (minDistance * minDistance);
    }
}