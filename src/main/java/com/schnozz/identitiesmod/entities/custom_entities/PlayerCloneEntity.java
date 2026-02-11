package com.schnozz.identitiesmod.entities.custom_entities;

import com.mojang.authlib.GameProfile;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.goals.FollowEntityAtDistanceGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/*
GOALS:
    Clone list still not working on relog
    Parkour hardcore (let it place cobblestone!!!)
*/
public class PlayerCloneEntity extends PathfinderMob {
    private UUID creatorId;
    private double range = 2.8;
    private int attackCooldownTicks = 0;
    private final int SWORD_COOLDOWN = 12;
    private final int NETHERITE_AXE_COOLDOWN = 20;
    private final int IRON_AXE_COOLDOWN = 22;
    private final int STONE_AXE_COOLDOWN = 25;
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
        //goal selection
        this.goalSelector.addGoal(0,new FloatGoal(this));
        this.goalSelector.addGoal(1,new MeleeAttackGoal(this,2.3F,true));
        this.goalSelector.addGoal(3,new WaterAvoidingRandomStrollGoal(this,2F));
        this.goalSelector.addGoal(4,new OpenDoorGoal(this,true));
        this.goalSelector.addGoal(5,new RandomSwimmingGoal(this,2.3F,120));//mob,speed,how often it changes direction
        //target selection
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this,Player.class,false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class,false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class,false));
    }
    @Override
    public boolean canAttack(LivingEntity target) {
        return
                !target.getData(ModDataAttachments.POWER_TYPE).equals("Clone")
                && !(target instanceof PlayerCloneEntity);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackCooldownTicks > 0) {
            this.attackCooldownTicks--;
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity)
    {
        if (this.getMainHandItem().getItem() instanceof SwordItem) {
            if (this.attackCooldownTicks > 0) {
                return false;
            }
            this.attackCooldownTicks = SWORD_COOLDOWN;
        }
        else if (this.getMainHandItem().getItem() instanceof AxeItem axe) {
            if (this.attackCooldownTicks > 0) {
                return false;
            }

            if(axe.getTier().equals(Tiers.NETHERITE) || axe.getTier().equals(Tiers.DIAMOND) || axe.getTier().equals(Tiers.GOLD))
            {
                this.attackCooldownTicks = NETHERITE_AXE_COOLDOWN;
            }
            else if(axe.getTier().equals(Tiers.IRON))
            {
                this.attackCooldownTicks = IRON_AXE_COOLDOWN;
            }
            else if(axe.getTier().equals(Tiers.STONE) || axe.getTier().equals(Tiers.WOOD))
            {
                this.attackCooldownTicks = STONE_AXE_COOLDOWN;
            }
        }
        return super.doHurtTarget(entity);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,20F)
                .add(Attributes.ATTACK_DAMAGE,1F)
                .add(Attributes.GRAVITY)
                .add(Attributes.KNOCKBACK_RESISTANCE,0F)
                .add(Attributes.ATTACK_KNOCKBACK,1F)
                .add(Attributes.MOVEMENT_SPEED, 0.18F);
    }

    @Override
    protected AABB getAttackBoundingBox() { //seems to work, try pvp
        Entity entity = this.getVehicle();
        AABB aabb;
        if (entity != null) {
            AABB aabb1 = entity.getBoundingBox();
            AABB aabb2 = this.getBoundingBox();
            aabb = new AABB(Math.min(aabb2.minX, aabb1.minX), aabb2.minY, Math.min(aabb2.minZ, aabb1.minZ), Math.max(aabb2.maxX, aabb1.maxX), aabb2.maxY, Math.max(aabb2.maxZ, aabb1.maxZ));
        } else {
            aabb = this.getBoundingBox();
        }

        return aabb.inflate(range, (double)0.0F, range);
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
