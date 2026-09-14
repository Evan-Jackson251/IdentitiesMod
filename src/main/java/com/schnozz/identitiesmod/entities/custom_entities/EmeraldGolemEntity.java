package com.schnozz.identitiesmod.entities.custom_entities;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.goals.FollowEntityAtDistanceGoal;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EmeraldGolemEntity extends PathfinderMob {


    @Override
    protected void registerGoals()
    {
        //goal selection
        this.goalSelector.addGoal(0,new FloatGoal(this));
        this.goalSelector.addGoal(1,new MeleeAttackGoal(this,1.5F,true));
        this.goalSelector.addGoal(3,new WaterAvoidingRandomStrollGoal(this,1F));
        this.goalSelector.addGoal(4,new OpenDoorGoal(this,true));
        this.goalSelector.addGoal(5,new RandomSwimmingGoal(this,1.5F,120));//mob,speed,how often it changes direction
        //target selection
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this,Player.class,false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class,false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class,false));
    }

    public EmeraldGolemEntity(EntityType<? extends AbstractGolem> entityType, Level level) {

        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH,40F)
                .add(Attributes.MOVEMENT_SPEED,1F)
                .add(Attributes.ATTACK_DAMAGE,5F)
                .add(Attributes.FOLLOW_RANGE)
                .add(Attributes.ARMOR,15F)
                .add(Attributes.KNOCKBACK_RESISTANCE,1F);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return
                !target.getData(ModDataAttachments.POWER_TYPE).equals("Golem")
                        && !(target instanceof EmeraldGolemEntity);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public void followPlayer(Player player) {
        this.goalSelector.addGoal(2, new FollowEntityAtDistanceGoal(this,player,2.3F,5F));
        System.out.println("FOLLOWING PLAYER");
    }
    public void unfollowPlayer() {
        this.getNavigation().stop();

        this.goalSelector.getAvailableGoals().forEach(wrapped -> {
            if (wrapped.getGoal() instanceof FollowEntityAtDistanceGoal) {
                wrapped.stop();
            }
        });
        this.goalSelector.removeAllGoals(goal ->
                goal instanceof FollowEntityAtDistanceGoal
        );
        System.out.println("NOT FOLLOWING PLAYER");
    }
    public void teleportClone(Vec3 pos)
    {
        this.setPos(pos);
    }
    public void killClone()
    {
        this.kill();
    }
    public void peacefulTargeting() {//GIVING NULL EXCEPTION ERROR AFTER REMOVING TARGET SELECTOR
        this.setTarget(null);
        this.getNavigation().stop();

        this.goalSelector.getAvailableGoals().forEach(wrapped -> {
            if (wrapped.getGoal() instanceof MeleeAttackGoal ||  wrapped.getGoal() instanceof WaterAvoidingRandomStrollGoal){
                wrapped.stop();
            }
        });
        this.goalSelector.removeAllGoals(goal ->
                goal instanceof MeleeAttackGoal
        );
        this.goalSelector.removeAllGoals(goal ->
                goal instanceof WaterAvoidingRandomStrollGoal
        );
        System.out.println("SET TARGETING TO PEACEFUL");
    }
    public void aggressiveTargeting(){
        boolean hasAttackGoal = this.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof MeleeAttackGoal);
        if(!hasAttackGoal) {
            this.goalSelector.addGoal(2,new MeleeAttackGoal(this,2.3F,true));
        }

        boolean hasStrollGoal = this.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof WaterAvoidingRandomStrollGoal);
        if(!hasStrollGoal) {
            this.goalSelector.addGoal(3,new WaterAvoidingRandomStrollGoal(this,2F));
        }

        System.out.println("SET TARGETING TO AGGRESIVE");
    }
    
}
