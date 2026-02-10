package com.schnozz.identitiesmod.entities.custom_entities;

import com.mojang.authlib.GameProfile;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.entities.custom_goals.TargetEntityGoal;
import com.schnozz.identitiesmod.goals.FollowEntityAtDistanceGoal;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.ClonesSyncPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

/*
GOALS:
    Fix entityId changing on relog for CLONES
    Parkour hardcore (let it place cobblestone!!!)
    Infire knockback
*/
public class PlayerCloneEntity extends PathfinderMob {
    private UUID creatorId;
    private double range = 3.0;
    private boolean relog = false;
    // Synced data
    private static final EntityDataAccessor<String> PROFILE_UUID =
            SynchedEntityData.defineId(PlayerCloneEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> PROFILE_NAME =
            SynchedEntityData.defineId(PlayerCloneEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> SLIM =
            SynchedEntityData.defineId(PlayerCloneEntity.class, EntityDataSerializers.BOOLEAN);

    // Cached data
    private GameProfile cachedProfile;

    public PlayerCloneEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.0f);
        }
        ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
    }

    @Override
    protected void registerGoals()
    {
        //normal goals
        this.goalSelector.addGoal(0,new FloatGoal(this));
        this.goalSelector.addGoal(1,new MeleeAttackGoal(this,2F,true));
        this.goalSelector.addGoal(3, new OpenDoorGoal(this,true));
        this.goalSelector.addGoal(4,new RandomSwimmingGoal(this,2F,120));//mob,speed,how often it changes direction

        //target selection
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this,Player.class,false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class,false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class,false));


    }
    @Override
    public boolean canAttack(LivingEntity target) {
        return
                !target.getData(ModDataAttachments.POWER_TYPE).equals("Clone")
                && !(target instanceof PlayerCloneEntity);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,20F)
                .add(Attributes.ATTACK_DAMAGE,1F)
                .add(Attributes.GRAVITY,0.08F)
                .add(Attributes.ATTACK_SPEED, 4F)
                //.add(Attributes.ARMOR)
                .add(Attributes.MOVEMENT_SPEED, 0.18F);

    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity entity)
    {
        return this.distanceToSqr(entity) <= (this.range*this.range);
    }
    public void followPlayer(Player player) {
        this.goalSelector.addGoal(2, new FollowEntityAtDistanceGoal(this,player,2.3F,3F));
        //undoAttackTarget();
        System.out.println("FOLLOWING PLAYER");
    }
    public void attackTarget(LivingEntity entity) {
        this.targetSelector.addGoal(0,new TargetEntityGoal(this,entity,2F));
        System.out.println("ATTACK TARGETING");
    }
    public void undoAttackTarget()
    {
        this.targetSelector.getAvailableGoals().forEach(wrappedGoal -> {
            if(wrappedGoal.getGoal() instanceof TargetEntityGoal) {
                this.targetSelector.removeGoal(wrappedGoal.getGoal());
            }
        });
        System.out.println("UNDO ATTACK TARGETING");
    }
    public void peacefulTargeting() {//GIVING NULL EXCEPTION ERROR AFTER REMOVING TARGET SELECTOR
        this.setTarget(null);
        this.getNavigation().stop();

        this.goalSelector.getAvailableGoals().forEach(wrapped -> {
            if (wrapped.getGoal() instanceof MeleeAttackGoal) {
                wrapped.stop();
            }
        });
        this.goalSelector.removeAllGoals(goal ->
                goal instanceof MeleeAttackGoal
        );
        System.out.println("SET TARGETING TO PEACEFUL");
    }
    public void aggressiveTargeting(){
        boolean hasAttackGoal = this.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof MeleeAttackGoal);
        if(!hasAttackGoal) {
            this.goalSelector.addGoal(2,new MeleeAttackGoal(this,2F,true));
        }
        System.out.println("SET TARGETING TO AGGRESIVE");
    }

    public void copyEquipmentFrom(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setItemSlot(slot, player.getItemBySlot(slot).copy());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(PROFILE_UUID, "");
        builder.define(PROFILE_NAME, "");
        builder.define(SLIM, false);
    }

    public boolean isSlim() {
        return this.entityData.get(SLIM);
    }

    public void setSlim(boolean slim) {
        this.entityData.set(SLIM, slim);
    }

    public void setCreatorId(UUID creatorId)
    {
        this.creatorId = creatorId;
        System.out.println("CREATOR UUID: " + creatorId);
    }
    public UUID getCreatorId()
    {
        return creatorId;
    }

    public void copyIdentityFrom(Player player) {
        this.entityData.set(PROFILE_UUID, player.getUUID().toString());
        this.entityData.set(PROFILE_NAME, player.getGameProfile().getName());
        this.cachedProfile = player.getGameProfile();
    }

    public GameProfile getGameProfile() {
        if (cachedProfile == null) {
            String uuidString = this.entityData.get(PROFILE_UUID);
            String name = this.entityData.get(PROFILE_NAME);

            if (!uuidString.isEmpty()) {
                try {
                    cachedProfile = new GameProfile(UUID.fromString(uuidString), name);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return cachedProfile;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (PROFILE_UUID.equals(key) || PROFILE_NAME.equals(key)) {
            this.cachedProfile = null;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (creatorId != null) {
            tag.putUUID("CreatorId", creatorId);
        }
        tag.putString("ProfileUUID", this.entityData.get(PROFILE_UUID));
        tag.putString("ProfileName", this.entityData.get(PROFILE_NAME));
        tag.putBoolean("Slim", this.entityData.get(SLIM));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("CreatorId")) {
            this.creatorId = tag.getUUID("CreatorId");
        }
        this.entityData.set(PROFILE_UUID, tag.getString("ProfileUUID"));
        this.entityData.set(PROFILE_NAME, tag.getString("ProfileName"));
        this.entityData.set(SLIM, tag.getBoolean("Slim"));
        this.cachedProfile = null;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        boolean killedEntity = super.killedEntity(level, entity);

        this.setLastHurtMob(null);
        this.setTarget(null);
        this.targetSelector.tick();

        return killedEntity;
    }
}
