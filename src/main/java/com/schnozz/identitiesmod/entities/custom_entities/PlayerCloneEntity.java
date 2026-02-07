package com.schnozz.identitiesmod.entities.custom_entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.UUID;

public class PlayerCloneEntity extends PathfinderMob {

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
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0,new FloatGoal(this));
        this.goalSelector.addGoal(1,new MeleeAttackGoal(this,0,true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this,Player.class,false));
    }
    @Override
    public boolean canAttack(LivingEntity target) {
        return true;
                //!target.getData(ModDataAttachments.POWER_TYPE).equals("clone") && (target instanceof Player || super.canAttack(target));
    }
    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,20F)
                .add(Attributes.ATTACK_DAMAGE,2F)
                .add(Attributes.GRAVITY,0.08F)
                .add(Attributes.ATTACK_SPEED,2.6F)
                .add(Attributes.MOVEMENT_SPEED,0.13F);
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
        tag.putString("ProfileUUID", this.entityData.get(PROFILE_UUID));
        tag.putString("ProfileName", this.entityData.get(PROFILE_NAME));
        tag.putBoolean("Slim", this.entityData.get(SLIM));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(PROFILE_UUID, tag.getString("ProfileUUID"));
        this.entityData.set(PROFILE_NAME, tag.getString("ProfileName"));
        this.entityData.set(SLIM, tag.getBoolean("Slim"));
        this.cachedProfile = null;
    }
}
