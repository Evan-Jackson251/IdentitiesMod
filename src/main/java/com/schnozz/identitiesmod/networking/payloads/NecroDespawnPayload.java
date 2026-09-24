package com.schnozz.identitiesmod.networking.payloads;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerSummons;
import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerTeam;
import com.schnozz.identitiesmod.leveldata.UUIDSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public record NecroDespawnPayload(int userID) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NecroDespawnPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("identitiesmod", "necro_despawn_payload"));

    public static final StreamCodec<ByteBuf, NecroDespawnPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            NecroDespawnPayload::userID,
            NecroDespawnPayload::new
    );


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static final double RANGE = 8.0D;

    @Nullable
    public static LivingEntity lookingAt(Player player, Predicate<Entity> filter) {
        Level level = player.level();
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 view = player.getViewVector(1.0F);
        Vec3 end = eye.add(view.scale(RANGE));

        // How far the line of sight actually reaches.
        BlockHitResult blockHit = level.clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxSqr = (blockHit.getType() == HitResult.Type.MISS)
                ? RANGE * RANGE
                : eye.distanceToSqr(blockHit.getLocation());

        AABB search = player.getBoundingBox().expandTowards(view.scale(RANGE)).inflate(1.0D);

        LivingEntity best = null;
        double bestSqr = maxSqr;

        for (Entity entity : level.getEntities(player, search, filter)) {
            Optional<Vec3> hit = entity.getBoundingBox()
                    .inflate(entity.getPickRadius())
                    .clip(eye, end);
            if (hit.isEmpty()) continue;

            double distSqr = eye.distanceToSqr(hit.get());
            if (distSqr < bestSqr) {
                bestSqr = distSqr;
                best = (LivingEntity) entity;
            }
        }

        return best;
    }

    public static Optional<NecromancerTeam.Member> lookedAtMember(ServerPlayer player) {
        NecromancerTeam team = player.getData(ModDataAttachments.NECROMANCER_TEAM);

        LivingEntity target = lookingAt(player, e ->
                e instanceof Mob && e.isAlive() && e.isPickable()
                        && isMemberOf(team, e.getUUID()));
        if (target == null) {
            return Optional.empty();
        }

        return team.getMembers().stream()
                .filter(m -> m.getEntityId().filter(id -> id.equals(target.getUUID())).isPresent())
                .findFirst();
    }

    private static boolean isMemberOf(NecromancerTeam team, UUID entityId) {
        return team.getMembers().stream()
                .anyMatch(m -> m.getEntityId().filter(id -> id.equals(entityId)).isPresent());
    }

    public static void handle(NecroDespawnPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!"Necromancer".equals(player.getData(ModDataAttachments.POWER_TYPE))) return;

            NecromancerTeam team = player.getData(ModDataAttachments.NECROMANCER_TEAM);
            if (team.getMembers().isEmpty()) return;

            lookedAtMember(player).ifPresent(m -> NecromancerSummons.despawn(player, m));
        });
    }
}
