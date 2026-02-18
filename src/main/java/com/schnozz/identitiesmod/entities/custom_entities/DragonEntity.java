package com.schnozz.identitiesmod.entities.custom_entities;

import com.schnozz.identitiesmod.items.BoundingBoxVisualizer;
import com.schnozz.identitiesmod.networking.payloads.EntityDamagePayload;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonEntity extends Animal {
    private final float BITE_DAMAGE = 9F;

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
                .add(Attributes.MAX_HEALTH,40F)
                .add(Attributes.MOVEMENT_SPEED,1F)
                .add(Attributes.ATTACK_DAMAGE,8F)
                .add(Attributes.FOLLOW_RANGE)
                .add(Attributes.ARMOR,20F)
                .add(Attributes.ARMOR_TOUGHNESS,10F)
                .add(Attributes.STEP_HEIGHT,3F)
                .add(Attributes.ATTACK_KNOCKBACK,4F)
                .add(Attributes.KNOCKBACK_RESISTANCE,5F);
    }

    public void biteAttack(Player player)
    {
        Level level = player.level();

        Vec3 look = player.getLookAngle();
        Vec3 start = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 frontCenter = start.add(look.scale(7.0));

        AABB aabb = new AABB(frontCenter, frontCenter).inflate(3.0);
        BoundingBoxVisualizer.showAABB(player.level(), aabb);

        Holder<DamageType> damageTypeHolder =
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.PLAYER_ATTACK);

        List<Entity> entities = player.level().getEntities(player, aabb, e -> !(e == player) && !(e == this));
        for(Entity target: entities)
        {
            PacketDistributor.sendToServer(new EntityDamagePayload(target.getId(),player.getId(),BITE_DAMAGE,damageTypeHolder));
        }
    }
    public void dragonBreath(Player dragonPlayer)
    {

    }

    //kill dragon on dismount
    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.kill();
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoAi(true);
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
    protected float getJumpPower() {
        return 3.5F * this.getBlockJumpFactor();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }
}
