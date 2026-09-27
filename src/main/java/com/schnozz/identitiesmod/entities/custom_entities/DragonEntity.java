package com.schnozz.identitiesmod.entities.custom_entities;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.entities.rendering.dragon.DragonPart;
import com.schnozz.identitiesmod.items.BoundingBoxVisualizer;
import com.schnozz.identitiesmod.networking.payloads.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonEntity extends Animal {
    //final variables
    private final float BITE_DAMAGE = 15F;
    private final float BREATH_DAMAGE = 2.5F;
    private final int FIRE_TICKS = 150;
    //bools
    private boolean isFlying = false;
    //Hit box variables
    private final DragonPart headPart =
            new DragonPart(this, 1.5F, 1.5F, 1.0F);

    private final DragonPart neckPart =
            new DragonPart(this, 2.0F, 2.0F, 1.0F);

    private final DragonPart bodyPart =
            new DragonPart(this, 3.0F, 2.0F, 1.0F);

    private final DragonPart leftWingPart =
            new DragonPart(this, 4.0F, 1.0F, 1.0F);

    private final DragonPart rightWingPart =
            new DragonPart(this, 4.0F, 1.0F, 1.0F);

    private final DragonPart tailPart1 =
            new DragonPart(this, 2.0F, 1.5F, 1.0F);

    private final DragonPart tailPart2 =
            new DragonPart(this, 2.0F, 1.5F, 1.0F);

    private final DragonPart tailPart3 =
            new DragonPart(this, 2.0F, 1.5F, 1.0F);
    private final DragonPart[] dragonParts = {
            headPart,
            neckPart,
            bodyPart,
            leftWingPart,
            rightWingPart,
            tailPart1,
            tailPart2,
            tailPart3
    };

    @Override
    protected void registerGoals()
    {

    }

    public DragonEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.setId(
                ENTITY_COUNTER.getAndAdd(this.dragonParts.length + 1) + 1
        );
    }
    public static AttributeSupplier.Builder createAttributes()
    {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH,60F)
                .add(Attributes.MOVEMENT_SPEED,1F)
                .add(Attributes.ATTACK_DAMAGE,8F)
                .add(Attributes.FOLLOW_RANGE)
                .add(Attributes.ARMOR,20F)
                .add(Attributes.ARMOR_TOUGHNESS,12F)
                .add(Attributes.STEP_HEIGHT,2F)
                .add(Attributes.BURNING_TIME,0F)
                .add(Attributes.KNOCKBACK_RESISTANCE,1F);
    }

    //Attacks
    public void biteAttack(Player player)
    {
        Level level = player.level();

        Vec3 origin = this.getPosition(1);

        // Horizontal facing, ignoring looking up/down
        double yaw = Math.toRadians(player.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 sideways = new Vec3(forward.z, 0, -forward.x);

        // Center is halfway along the 5-block depth
        Vec3 center = origin.add(forward.scale(6)).add(0, 6, 0);

        // World-aligned extents enclosing the rotated rectangle
        double halfX = Math.abs(forward.x) * 2.5 + Math.abs(sideways.x);
        double halfZ = Math.abs(forward.z) * 2.5 + Math.abs(sideways.z);

        origin.add(0,-2,0);

        AABB aabb = new AABB(
                center.x - halfX, origin.y - 2,     center.z - halfZ,
                center.x + halfX, origin.y + 2, center.z + halfZ
        );
        BoundingBoxVisualizer.showAABB(level,aabb);

        var targets = player.level().getEntities(player, aabb, target -> {
            if(target instanceof DragonEntity) {return false;}
            Vec3 offset = target.getBoundingBox().getCenter().subtract(origin);

            //double depth = offset.dot(forward);
            //double width = offset.dot(sideways);

            return true;
        });

        Holder<DamageType> damageTypeHolder =
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.PLAYER_ATTACK);

        for(Entity target: targets)
        {
            PacketDistributor.sendToServer(new EntityDamagePayload(target.getId(),this.getId(),BITE_DAMAGE,damageTypeHolder));
            this.heal(1F);
            PacketDistributor.sendToServer(new HealPayload(this.getId(),2F));
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
                PacketDistributor.sendToServer(new SetFirePayload(FIRE_TICKS,target.getId()));
            }
        }
    }
    public void blockExplosion(){
        PacketDistributor.sendToServer(new ExplosionPayload(
                this.getId(),this.getX(),this.getY()+3,this.getZ(),7F));
    }
    //kill dragon on dismount and debuff player
    @Override
    protected void removePassenger(Entity passenger) {
        if (passenger instanceof Player dragonPlayer && dragonPlayer.getData(ModDataAttachments.POWER_TYPE).equals("Dragon")) {
//            if(!dragonPlayer.level().isClientSide)
//            {
//                System.out.println("SERVER SIDE DRAGON PASSENGER REMOVE");
//                dragonPlayer.removeEffect(MobEffects.WEAKNESS);
//                dragonPlayer.removeEffect(MobEffects.DAMAGE_RESISTANCE);
//                dragonPlayer.removeEffect(ModEffects.SUPER_INVISIBILITY);
//
//                dragonPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
//                        MobEffectInstance.INFINITE_DURATION, 0, false, false));
//                dragonPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
//                        MobEffectInstance.INFINITE_DURATION, 0, false, false));
//
//                dragonPlayer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0D);
//            }
            if(dragonPlayer.level().isClientSide)
            {
                System.out.println("CLIENT SIDE DRAGON PASSENGER REMOVE");

                Minecraft mc = Minecraft.getInstance();
                mc.options.setCameraType(CameraType.FIRST_PERSON);

                PacketDistributor.sendToServer(new RemoveEffectsPayload(dragonPlayer.getId()));

                PacketDistributor.sendToServer(new PotionLevelPayload(MobEffects.MOVEMENT_SLOWDOWN,0,MobEffectInstance.INFINITE_DURATION));
                PacketDistributor.sendToServer(new PotionLevelPayload(MobEffects.WEAKNESS,0,MobEffectInstance.INFINITE_DURATION));

                PacketDistributor.sendToServer(new MaxHealthPayload(8,dragonPlayer.getId()));
            }
        }
        super.removePassenger(passenger);
        this.kill();
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoAi(true);
        updateDragonParts();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger)
    {
        return passenger instanceof Player && this.getPassengers().isEmpty();
    }
    @Override
    protected Vec3 getPassengerAttachmentPoint(
            Entity passenger,
            EntityDimensions dimensions,
            float scale
    ) {
        /*
         * Matches the head pivot in the ridden model pose below,
         * including the renderer's -2 Y translation.
         *
         * These are specific to the model/renderer we created.
         */
        double headHeight = 2.251D * scale;
        double headForward = 3.875D * scale;

        Vec3 offset = new Vec3(
                0.0D,
                headHeight - passenger.getEyeHeight(),
                headForward
        );

        return offset.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
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
        if (this.getControllingPassenger() instanceof Player player) {
            // Current rotation only. Minecraft maintains the previous-tick values.
            this.setYRot(player.getYRot());
            this.setXRot(player.getXRot());

            this.yHeadRot = this.getYRot();
            this.yBodyRot = this.getYRot();

            this.setSpeed(0.1F);
            if(this.isFlying){this.setSpeed(0.3F);}

            super.travel(new Vec3(
                    player.xxa,
                    travelVector.y,
                    player.zza
            ));
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    protected float getJumpPower() {
        return 3.5F * this.getBlockJumpFactor();
    }

    //HIT BOXES
    @Override
    public boolean isMultipartEntity() {
        return true;
    }
    @Override
    public PartEntity<?>[] getParts() {
        return dragonParts;
    }
    @Override
    public void setId(int id) {
        super.setId(id);

        // Guard against calls before subclass fields have initialized.
        if (dragonParts != null) {
            for (int i = 0; i < dragonParts.length; i++) {
                dragonParts[i].setId(id + i + 1);
            }
        }
    }
    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        updateDragonParts();
    }
    private void updateDragonParts() {
        // Arguments: part, sideways offset, bottom height, forward offset.
        // These approximate the flight pose from the earlier model.
        positionDragonPart(bodyPart,       0.0, 1.5, -1.5);
        positionDragonPart(neckPart,       0.0, 1.5,  1.5);
        positionDragonPart(headPart,       0.0, 1.5,  4.0);

        positionDragonPart(leftWingPart,   4.0, 2.0, -1.5);
        positionDragonPart(rightWingPart, -4.0, 2.0, -1.5);

        positionDragonPart(tailPart1,      0.0, 2.0, -4.5);
        positionDragonPart(tailPart2,      0.0, 2.0, -6.5);
        positionDragonPart(tailPart3,      0.0, 2.0, -8.5);
    }
    private void positionDragonPart(DragonPart part, double sideways, double bottomHeight, double forward)
    {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yaw);
        double cos = Mth.cos(yaw);
        double scale = this.getScale();

        double previousX = part.getX();
        double previousY = part.getY();
        double previousZ = part.getZ();

        // Refresh in case the parent's scale changes.
        part.refreshDimensions();

        part.setPos(
                this.getX() + (sideways * cos - forward * sin) * scale,
                this.getY() + bottomHeight * scale,
                this.getZ() + (sideways * sin + forward * cos) * scale
        );

        part.xo = previousX;
        part.yo = previousY;
        part.zo = previousZ;

        part.xOld = previousX;
        part.yOld = previousY;
        part.zOld = previousZ;
    }
    //Animation
    public boolean shouldAnimateFlight(){
        return isFlying;
    }
    public void setIsFlying(boolean flying){
        this.isFlying = flying;
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
