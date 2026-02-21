package com.schnozz.identitiesmod.entities.custom_entities;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.items.BoundingBoxVisualizer;
import com.schnozz.identitiesmod.mob_effects.ModEffects;
import com.schnozz.identitiesmod.networking.payloads.EntityDamagePayload;
import com.schnozz.identitiesmod.networking.payloads.MaxHealthPayload;
import com.schnozz.identitiesmod.networking.payloads.PotionLevelPayload;
import com.schnozz.identitiesmod.networking.payloads.PotionTogglePayload;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
    private final float BITE_DAMAGE = 15F;
    private final float BREATH_DAMAGE = 5.0F;
    private final int FIRE_TICKS = 150;

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
                .add(Attributes.MAX_HEALTH,50F)
                .add(Attributes.MOVEMENT_SPEED,1F)
                .add(Attributes.ATTACK_DAMAGE,8F)
                .add(Attributes.FOLLOW_RANGE)
                .add(Attributes.ARMOR,20F)
                .add(Attributes.ARMOR_TOUGHNESS,10F)
                .add(Attributes.STEP_HEIGHT,3F)
                .add(Attributes.BURNING_TIME,0F)
                .add(Attributes.KNOCKBACK_RESISTANCE,1F);
    }

    public void biteAttack(Player player)
    {
        Level level = player.level();

        Vec3 look = player.getLookAngle();
        Vec3 start = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 frontCenter = start.add(look.scale(9.0));

        AABB aabb = new AABB(frontCenter, frontCenter).inflate(3.0);
        BoundingBoxVisualizer.showAABB(player.level(), aabb);

        Holder<DamageType> damageTypeHolder =
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.PLAYER_ATTACK);

        List<Entity> entities = player.level().getEntities(player, aabb, e -> !(e == player) && !(e == this));
        for(Entity target: entities)
        {
            PacketDistributor.sendToServer(new EntityDamagePayload(target.getId(),this.getId(),BITE_DAMAGE,damageTypeHolder));
        }
    }
    public void dragonBreath(Player dragonPlayer) {
        Level level = dragonPlayer.level();

        // Configuration
        float range = 30.0F;
        float coneWidth = 0.90F;
        int particleCount = 15;

        Vec3 lookVec = dragonPlayer.getLookAngle().normalize();
        Vec3 origin = this.getEyePosition().add(0,-2,0).add(this.getLookAngle().scale(4));
        // ---------------------------

        // Visuals: Spawn Particles
        if (level.isClientSide) {
            RandomSource rand = level.getRandom();
            double maxConeAngle = Math.acos(coneWidth);

            // Calculate basis vectors for the cone's circular cross-section
            Vec3 upRef = new Vec3(0, 1, 0);
            Vec3 right = upRef.cross(lookVec).normalize();
            if (right.lengthSqr() < 1.0E-4) {
                right = new Vec3(1, 0, 0).cross(lookVec).normalize();
            }
            Vec3 trueUp = lookVec.cross(right).normalize();

            for (int i = 0; i < particleCount; i++) {
                double distance = rand.nextDouble() * range;
                double coneRadiusAtDist = distance * Math.tan(maxConeAngle);

                // Evenly distribute particles across the circular disk of the cone
                double theta = rand.nextDouble() * 2 * Math.PI;
                double r = Math.sqrt(rand.nextDouble()) * coneRadiusAtDist;

                Vec3 offset = right.scale(r * Math.cos(theta))
                        .add(trueUp.scale(r * Math.sin(theta)));

                // Particles start at 'origin' and move outward
                Vec3 particlePos = origin.add(lookVec.scale(distance)).add(offset);

                // Velocity flows away from the origin
                Vec3 velocity = lookVec.scale(0.6).add(offset.normalize().scale(0.02));

                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        particlePos.x, particlePos.y, particlePos.z,
                        velocity.x, velocity.y, velocity.z);
            }
        }

        //Hit Detection
        Vec3 targetCenter = origin.add(lookVec.scale(range / 2));
        AABB areaOfEffect = new AABB(origin, targetCenter).inflate(range);

        Holder<DamageType> damageTypeHolder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.IN_FIRE);

        List<Entity> possibleTargets = level.getEntities(dragonPlayer, areaOfEffect,
                e -> e != dragonPlayer && e instanceof LivingEntity);

        for (Entity target : possibleTargets) {
            // Calculate direction from the custom origin to the target
            Vec3 targetPos = target.position().add(0, target.getEyeHeight() / 2, 0);
            Vec3 dirToTarget = targetPos.subtract(origin).normalize();

            double dotProduct = lookVec.dot(dirToTarget);
            double distSqr = origin.distanceToSqr(targetPos);

            if (dotProduct > coneWidth && distSqr < (range * range)) {
                PacketDistributor.sendToServer(new EntityDamagePayload(
                        target.getId(),
                        this.getId(),
                        BREATH_DAMAGE,
                        damageTypeHolder
                ));
                target.setRemainingFireTicks(FIRE_TICKS);
            }
        }
    }

    //kill dragon on dismount and debuff player
    @Override
    protected void removePassenger(Entity passenger) {
        if (passenger instanceof Player dragonPlayer && dragonPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Dragon")) {
            if(!dragonPlayer.level().isClientSide)
            {
                dragonPlayer.removeEffect(MobEffects.WEAKNESS);
                dragonPlayer.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                dragonPlayer.removeEffect(ModEffects.SUPER_INVISIBILITY);

                dragonPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                        MobEffectInstance.INFINITE_DURATION, 0, false, false));
                dragonPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                        MobEffectInstance.INFINITE_DURATION, 0, false, false));

                dragonPlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0D);
            }
        }
        super.removePassenger(passenger);
        this.kill();
    }
    //debuff player on death
//    @Override
//    public void die(DamageSource source)
//    {
//        if(!this.getPassengers().isEmpty())
//        {
//            for(Entity entity: this.getPassengers()){
//                if(entity instanceof Player dragonPlayer && dragonPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Dragon"))
//                {
//                    PacketDistributor.sendToServer(new PotionLevelPayload(MobEffects.MOVEMENT_SLOWDOWN,0,18000));
//                    PacketDistributor.sendToServer(new PotionLevelPayload(MobEffects.WEAKNESS,0,18000));
//                }
//            }
//        }
//        super.die(source);
//    }

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
            this.yRotO = this.getYRot();
            this.yHeadRot = this.getYRot();
            this.yHeadRotO = this.getYRot();
            this.yBodyRot = this.getYRot();

            this.setXRot(player.getXRot());
            this.xRotO = this.getXRot();

            this.setRot(this.getYRot(), this.getXRot());

            float forward = player.zza;
            float strafe = player.xxa;

            this.setSpeed(0.5F);

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
