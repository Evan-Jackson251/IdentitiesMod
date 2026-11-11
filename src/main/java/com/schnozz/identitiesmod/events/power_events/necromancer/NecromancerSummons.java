package com.schnozz.identitiesmod.events.power_events.necromancer;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.goals.FollowEntityAtDistanceGoal;
import com.schnozz.identitiesmod.leveldata.UUIDSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NecromancerSummons {

    /** How often stored members heal, in ticks (20 = once per second). */
    public static final int REGEN_INTERVAL_TICKS = 20;
    /** Fraction of max health healed per interval. 0.01 = 1% per second, so empty to full in ~100s. */
    public static final float REGEN_FRACTION = 0.01F;

    /** How far the necromancer can look to pick a team target. */
    public static final double TARGET_RANGE = 50.0;

    public static boolean isSummoned(NecromancerTeam.Member member) {
        return member.getEntityId().isPresent();
    }

    public static Optional<NecromancerTeam.Member> nextStored(NecromancerTeam team) {
        return team.getMembers().stream()
                .filter(member -> member.getEntityId().isEmpty())
                .findFirst();
    }

    public static boolean summon(ServerPlayer player, NecromancerTeam.Member member) {
        if (member.getEntityId().isPresent()) {
            return false;   // already out
        }

        EntityType<?> type = member.getType().orElse(null);
        if (type == null) return false;

        ServerLevel level = player.serverLevel();
        if (!(type.create(level) instanceof Mob mob)) return false;

        mob.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
                MobSpawnType.MOB_SUMMONED, null);
        mob.setPersistenceRequired();
        NecromancerTeam.applyGear(member, mob);

        if (member.getHealth() > 0.0F) {
            mob.setHealth(Math.min(member.getHealth(), mob.getMaxHealth()));
        }

        if (!level.addFreshEntity(mob)) {
            return false;   // don't record state for an entity that didn't spawn
        }

        member.setEntityId(mob.getUUID());
        member.setActive(true);
        UUIDSavedData.get(player.server).addMob(player.getUUID(), mob.getUUID());
        takeControl(mob, player);
        return true;
    }

    public static Optional<Mob> findSummon(MinecraftServer server, NecromancerTeam.Member member) {
        UUID id = member.getEntityId().orElse(null);
        if (id == null) {
            return Optional.empty();
        }

        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof Mob mob && !mob.isRemoved()) {
                return Optional.of(mob);
            }
        }
        return Optional.empty();
    }

    public static void takeControl(Mob mob, Player player) {
        // Current target state.
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.setLastHurtMob(null);

        // Every goal that picks targets lives in targetSelector.
        mob.targetSelector.removeAllGoals(goal -> true);

        // Brain-based mobs ignore targetSelector entirely.
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);

        // Following is movement, so it goes in goalSelector next to the mob's own attack goals.
        // It switches itself off whenever the mob has a live target, letting those attack goals
        // run. Remove any existing copy first so calling takeControl twice can't stack two.
        mob.goalSelector.removeAllGoals(goal -> goal instanceof FollowEntityAtDistanceGoal);
        mob.goalSelector.addGoal(1, new FollowEntityAtDistanceGoal(mob, player, 1.25, 4));

        // Pathfinding is capped by FOLLOW_RANGE (16 for skeletons, 35 for zombies), so
        // without this a summon can't path to a target picked 50 blocks away.
        AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null && followRange.getBaseValue() < TARGET_RANGE + 16) {
            followRange.setBaseValue(TARGET_RANGE + 16);
        }
    }

    /**
     * Finds the living entity the player is looking at, up to {@code range} blocks.
     * Stops at solid blocks (no targeting through walls) and looks past the player's
     * own summons, so a skeleton standing in front of you doesn't eat the click.
     */
    @Nullable
    public static LivingEntity findLookTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(range));

        BlockHitResult blockHit = player.level().clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        UUIDSavedData list = UUIDSavedData.get(player.server);
        AABB searchBox = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, searchBox,
                e -> e instanceof LivingEntity
                        && e.isPickable()
                        && !e.isSpectator()
                        && list.getOwnerOf(e.getUUID()).filter(player.getUUID()::equals).isEmpty(),
                eye.distanceToSqr(end));

        return hit != null ? (LivingEntity) hit.getEntity() : null;
    }

    /** Points every summoned team member at {@code target}. Returns how many were told. */
    public static int setTeamTarget(ServerPlayer player, LivingEntity target) {
        NecromancerTeam team = player.getData(ModDataAttachments.NECROMANCER_TEAM);
        int count = 0;

        for (NecromancerTeam.Member member : team.getMembers()) {
            Mob mob = findSummon(player.server, member).orElse(null);
            if (mob == null || mob == target || mob.level() != target.level()) {
                continue;
            }

            mob.setTarget(target);                                              // goal-based mobs
            mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);   // brain-based mobs (ignored if unused)
            count++;
        }
        return count;
    }

    /**
     * Wipes everything that makes a mob want to fight, so your follow goal takes over again.
     * Covers goal-based mobs, brain-based mobs (piglins, hoglins) and neutral mobs that hold
     * grudges (endermen, zombified piglins).
     */
    public static void clearTarget(Mob mob) {
        mob.setTarget(null);
        mob.setLastHurtByMob(null);

        if (mob instanceof NeutralMob neutral) {
            neutral.stopBeingAngry();
        }

        // Safe on mobs that don't use these memories; erasing an unregistered memory does nothing.
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        mob.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
        mob.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);

        // Drop the path to the old target so they turn around immediately.
        mob.getNavigation().stop();
    }

    /** Clears every summon's target so they go back to following. Returns how many were told. */
    public static int recallTeam(ServerPlayer player) {
        NecromancerTeam team = player.getData(ModDataAttachments.NECROMANCER_TEAM);
        int count = 0;
        for (NecromancerTeam.Member member : team.getMembers()) {
            Optional<Mob> mob = findSummon(player.server, member);
            if (mob.isPresent()) {
                clearTarget(mob.get());
                count++;
            }
        }
        return count;
    }

    /**
     * Pulls every summon to the player and clears their targets. Works across dimensions.
     * Only finds summons in loaded chunks; one left behind in an unloaded chunk can't be reached.
     */
    public static int teleportTeamToPlayer(ServerPlayer player) {
        NecromancerTeam team = player.getData(ModDataAttachments.NECROMANCER_TEAM);
        ServerLevel level = player.serverLevel();
        int count = 0;

        for (NecromancerTeam.Member member : team.getMembers()) {
            Mob mob = findSummon(player.server, member).orElse(null);
            if (mob == null) {
                continue;
            }

            clearTarget(mob);
            spawnTeleportEffects((ServerLevel) mob.level(), mob.position());

            // A rider gets snapped back onto its mount every tick, which would undo the teleport.
            // finalizeSpawn can create jockeys (baby zombie on a chicken, skeleton on a spider).
            mob.stopRiding();

            if (mob.level() == level) {
                Vec3 spot = findSpotNear(player, mob);
                mob.teleportTo(spot.x, spot.y, spot.z);
                mob.resetFallDistance();
            } else {
                // changeDimension replaces the entity with a copy (same UUID, so the roster and
                // UUIDSavedData still match), but AI goals aren't saved, so the copy has to be
                // tamed again or it would spawn hostile.
                Entity moved = mob.changeDimension(new DimensionTransition(
                        level, player.position(), Vec3.ZERO, player.getYRot(), 0.0F,
                        DimensionTransition.DO_NOTHING));
                if (!(moved instanceof Mob movedMob)) {
                    continue;
                }
                takeControl(movedMob, player);
                mob = movedMob;
            }

            spawnTeleportEffects(level, mob.position());
            count++;
        }
        return count;
    }

    /** Random open spot within a few blocks of the player, so the team doesn't land in one stack. */
    private static Vec3 findSpotNear(ServerPlayer player, Mob mob) {
        ServerLevel level = player.serverLevel();
        RandomSource random = mob.getRandom();

        for (int attempt = 0; attempt < 8; attempt++) {
            double x = player.getX() + (random.nextDouble() * 2.0 - 1.0) * 2.5;
            double y = player.getY();
            double z = player.getZ() + (random.nextDouble() * 2.0 - 1.0) * 2.5;

            AABB box = mob.getDimensions(mob.getPose()).makeBoundingBox(x, y, z);
            BlockPos below = BlockPos.containing(x, y - 0.5, z);

            if (level.noCollision(mob, box)
                    && level.getBlockState(below).entityCanStandOn(level, below, mob)) {
                return new Vec3(x, y, z);
            }
        }
        return player.position();   // nothing open nearby; the player's own spot is always valid
    }

    private static void spawnTeleportEffects(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 1.0, pos.z, 24, 0.4, 0.6, 0.4, 0.1);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.6F, 1.0F);
    }

    public static void despawn(ServerPlayer player, NecromancerTeam.Member member) {
        findSummon(player.server, member).ifPresent(mob -> {
            member.setHealth(mob.getHealth());
            member.setMaxHealth(mob.getMaxHealth());
            NecromancerTeam.reclaimGear(member, mob);
            mob.discard();
        });
        // Discarding drops nothing, so it's safe to forget the mob right away.
        member.getEntityId().ifPresent(id ->
                UUIDSavedData.get(player.server).removeMob(player.getUUID(), id));
        member.setEntityId(null);
        member.setActive(false);
    }

    /**
     * Called when a summoned mob dies. Its gear goes to the stash (overflow to the
     * player's inventory, then the ground) and its roster slot is freed.
     *
     * The mob is deliberately NOT removed from UUIDSavedData here: the drop and XP
     * events fire after the death event and still need to see it as a summon so
     * they can cancel its loot. ServerNecroEvents.onEntityLeave cleans it up once
     * the body is actually removed.
     */
    public static void onSummonDied(ServerPlayer owner, Mob mob) {
        NecromancerTeam team = owner.getData(ModDataAttachments.NECROMANCER_TEAM);
        List<NecromancerTeam.Member> members = team.getMembers();

        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getEntityId().filter(mob.getUUID()::equals).isEmpty()) {
                continue;
            }

            team.removeMember(i);
            for (ItemStack leftover : team.returnGearToStash(mob)) {
                if (!owner.getInventory().add(leftover)) {
                    owner.drop(leftover, false);   // add() shrinks the stack; drop what's left
                }
            }
            return;
        }
    }

    /** Heals every stored (not summoned) member a little. */
    public static void regenStored(ServerPlayer player) {
        NecromancerTeam team = player.getData(ModDataAttachments.NECROMANCER_TEAM);
        for (NecromancerTeam.Member member : team.getMembers()) {
            member.regenStored(REGEN_FRACTION);
        }
    }

    public static int despawnAll(ServerPlayer player) {
        NecromancerTeam team = player.getData(ModDataAttachments.NECROMANCER_TEAM);
        int count = 0;
        for (NecromancerTeam.Member member : team.getMembers()) {
            if (member.getEntityId().isPresent()) {
                despawn(player, member);
                count++;
            }
        }
        return count;
    }
}