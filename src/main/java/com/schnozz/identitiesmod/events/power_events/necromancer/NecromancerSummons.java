package com.schnozz.identitiesmod.events.power_events.necromancer;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.goals.FollowEntityAtDistanceGoal;
import com.schnozz.identitiesmod.leveldata.UUIDSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public class NecromancerSummons {

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

        // Your goal supplies targeting from here.
        mob.targetSelector.addGoal(1, new FollowEntityAtDistanceGoal(mob, player, 1.25, 4));
    }

    public static void despawn(ServerPlayer player, NecromancerTeam.Member member) {
        findSummon(player.server, member).ifPresent(mob -> {
            member.setHealth(mob.getHealth());
            NecromancerTeam.reclaimGear(member, mob);
            mob.discard();
        });
        member.setEntityId(null);
        member.setActive(false);
        // remove from UUIDSavedData too
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
