package com.schnozz.identitiesmod.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

public class EntitySnapshot {
    private int entityId,foodLevel,dimension; //0 overworld, 1 nether, 2 end
    private Vec3 pos,velocity;
    private Float yRot,xRot,health,exhaustionLevel,saturationLevel;
    private Map<MobEffect, MobEffectInstance> effectMap;

    public EntitySnapshot(int dimension, int foodLevel, Float exhaustionLevel, Float saturationLevel, int entityId, Vec3 pos, Vec3 velocity, Float yRot, Float xRot, Float health, Map<MobEffect, MobEffectInstance> effectMap)
    {
        this.dimension = dimension;
        this.foodLevel = foodLevel;
        this.exhaustionLevel = exhaustionLevel;
        this.saturationLevel = saturationLevel;
        this.entityId = entityId;
        this.pos = pos;
        this.velocity = velocity;
        this.yRot = yRot;
        this.xRot = xRot;
        this.health = health;
        this.effectMap = effectMap;
    }
    public static EntitySnapshot fromEntity(Player entity) {
        int dim = 0;
        if(entity.level().dimension().toString().equals("Nether")){
            dim = 1;
        }
        else if(entity.level().dimension().toString().equals("End")){
            dim = 2;
        }
        return new EntitySnapshot(
                dim,
                entity.getFoodData().getFoodLevel(),
                entity.getFoodData().getExhaustionLevel(),
                entity.getFoodData().getSaturationLevel(),
                entity.getId(),
                entity.position(),
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot(),
                entity.getHealth(),
                entity.getActiveEffectsMap().entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().value(),               // unwrap Holder<MobEffect> → MobEffect
                                e -> new MobEffectInstance(e.getValue())
                        ))

        );
    }
    public static final Codec<EntitySnapshot> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("dimension").forGetter(s -> s.dimension),
                    Codec.INT.fieldOf("food_level").forGetter(s -> s.foodLevel),
                    Codec.FLOAT.fieldOf("exhaustion_level").forGetter(s -> s.exhaustionLevel),
                    Codec.FLOAT.fieldOf("saturation_level").forGetter(s -> s.saturationLevel),
                    Codec.INT.fieldOf("entity_id").forGetter(s -> s.entityId),
                    Vec3.CODEC.fieldOf("pos").forGetter(s -> s.pos),
                    Vec3.CODEC.fieldOf("velocity").forGetter(s -> s.velocity),
                    Codec.FLOAT.fieldOf("yRot").forGetter(s -> s.yRot),
                    Codec.FLOAT.fieldOf("xRot").forGetter(s -> s.xRot),
                    Codec.FLOAT.fieldOf("health").forGetter(s -> s.health),
                    Codec.unboundedMap(
                            BuiltInRegistries.MOB_EFFECT.byNameCodec(),
                            MobEffectInstance.CODEC
                    ).fieldOf("effects").forGetter(s -> s.effectMap)
            ).apply(instance, EntitySnapshot::new));

    public static final StreamCodec<ByteBuf, EntitySnapshot> STREAM_CODEC =
            ByteBufCodecs.fromCodec(CODEC);

    public void applySnapshot(Level level)
    {
        ServerPlayer entity = (ServerPlayer)level.getEntity(entityId);

        ServerLevel dimensionLevel = entity.getServer().getLevel(Level.OVERWORLD);
        if(dimension == 1)
        {
            dimensionLevel = entity.getServer().getLevel(Level.NETHER);
        }
        else if(dimension == 2)
        {
            dimensionLevel = entity.getServer().getLevel(Level.END);
        }

        if(entity != null) {
            entity.teleportTo(dimensionLevel,pos.x,pos.y,pos.z, EnumSet.noneOf(RelativeMovement.class),yRot,xRot);
//            entity.setPos(pos);
//            entity.setDeltaMovement(velocity);
//            entity.setYRot(yRot);
//            entity.setXRot(xRot);
            entity.setHealth(health);
            entity.removeAllEffects();
            for(MobEffectInstance inst : effectMap.values())
            {
                entity.addEffect(new MobEffectInstance(inst));
            }
            entity.getFoodData().setFoodLevel(foodLevel);
            entity.getFoodData().setExhaustion(exhaustionLevel);
            entity.getFoodData().setSaturation(saturationLevel);

        }
    }
}
