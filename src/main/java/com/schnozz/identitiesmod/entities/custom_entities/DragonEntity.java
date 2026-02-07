package com.schnozz.identitiesmod.entities.custom_entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DragonEntity extends Animal {
    //public final AnimationState idleAnimationState = new AnimationState();

    @Override
    protected void registerGoals()
    {

    }

    public DragonEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }
    public static AttributeSupplier.Builder createAttributes()
    {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH,35F)
                .add(Attributes.MOVEMENT_SPEED,2F)
                .add(Attributes.ARMOR,10)
                .add(Attributes.ARMOR_TOUGHNESS,4)
                .add(Attributes.STEP_HEIGHT,3)
                .add(Attributes.GRAVITY,0)
                .add(Attributes.ATTACK_KNOCKBACK,4)
                .add(Attributes.BURNING_TIME,0)
                .add(Attributes.KNOCKBACK_RESISTANCE,5);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoAi(true);

//        if(this.level().isClientSide())
//        {
//            this.setupAnimationStates();
//        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger)
    {
        return passenger instanceof Player && this.getPassengers().isEmpty();
    }
    @Override
    public boolean isControlledByLocalInstance() {
        return this.getControllingPassenger() instanceof Player;
    }
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.getFirstPassenger() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
            this.setYRot(player.getYRot());
            this.setXRot(player.getXRot() * 0.5F);
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();

            this.setRot(this.getYRot(), this.getXRot());

            float forward = player.zza; // W/S
            float strafe = player.xxa;  // A/D

            this.setSpeed(0.5F); // Dragon speed

            super.travel(new Vec3(strafe, travelVector.y, forward));
        }
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }
    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    private void setupAnimationStates()
    {
        //this.idleAnimationState.start(this.tickCount);
    }
}
