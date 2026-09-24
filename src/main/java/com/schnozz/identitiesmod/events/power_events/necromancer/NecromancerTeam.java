package com.schnozz.identitiesmod.events.power_events.necromancer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Unbreakable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A necromancer's roster plus their shared equipment stash.
 *
 * Registered as a player attachment:
 *
 *   NECROMANCER_TEAM = ATTACHMENT_TYPES.register("necromancer_team",
 *       () -> AttachmentType.builder(NecromancerTeam::new)
 *               .serialize(NecromancerTeam.CODEC)
 *               .copyOnDeath()
 *               .build());
 *
 * This object is mutable and mutated in place; NeoForge serializes it on world
 * save, so there is no need to call setData() after changing it.
 */
public class NecromancerTeam {
    public static final int MAX_MEMBERS = 10;
    public static final int STASH_SIZE = 18;
    public static final int GEAR_SIZE = 6;

    /** Gear index -> equipment slot. Index order is fixed and persisted, so don't reorder. */
    public static final EquipmentSlot[] GEAR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND
    };

    public static final Codec<NecromancerTeam> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Member.CODEC.listOf().optionalFieldOf("members", List.of()).forGetter(t -> t.members),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("stash", List.of()).forGetter(t -> t.stash)
    ).apply(inst, NecromancerTeam::new));

    private final List<Member> members;
    private final NonNullList<ItemStack> stash;

    public NecromancerTeam() {
        this.members = new ArrayList<>();
        this.stash = NonNullList.withSize(STASH_SIZE, ItemStack.EMPTY);
    }

    private NecromancerTeam(List<Member> members, List<ItemStack> stash) {
        this.members = new ArrayList<>(members.subList(0, Math.min(members.size(), MAX_MEMBERS)));
        this.stash = pad(stash, STASH_SIZE);
    }

    // ------------------------------------------------------------------- roster

    /** Live list. Mutate through the methods below so the size cap is respected. */
    public List<Member> getMembers() {
        return members;
    }

    public List<Member> getMembersView() {
        return Collections.unmodifiableList(members);
    }

    @Nullable
    public Member getMember(int index) {
        return (index >= 0 && index < members.size()) ? members.get(index) : null;
    }

    public boolean isFull() {
        return members.size() >= MAX_MEMBERS;
    }

    /** Adds a member. Duplicated types are allowed (three zombies is a valid team). */
    public boolean addMember(EntityType<?> type) {
        if (isFull()) {
            return false;
        }
        members.add(new Member(BuiltInRegistries.ENTITY_TYPE.getKey(type)));
        return true;
    }

    /** Removes a member and returns it, so the caller can reclaim its gear. */
    @Nullable
    public Member removeMember(int index) {
        if (index < 0 || index >= members.size()) {
            return null;
        }
        return members.remove(index);
    }

    // -------------------------------------------------------------------- stash

    public NonNullList<ItemStack> getStash() {
        return stash;
    }

    /** Puts a stack into the first free stash slot. Returns what wouldn't fit. */
    public ItemStack depositToStash(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < stash.size(); i++) {
            if (stash.get(i).isEmpty()) {
                stash.set(i, stack.copy());
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    // --------------------------------------------------------------- gear moves

    /**
     * Copies a member's stored gear onto a freshly summoned mob. Gear is made
     * unbreakable and given a zero drop chance so it can't be lost or degraded.
     */
    public static void applyGear(Member member, Mob mob) {
        for (int i = 0; i < GEAR_SLOTS.length; i++) {
            ItemStack stack = member.getGear().get(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack copy = makeUnbreakable(stack.copy());
            mob.setItemSlot(GEAR_SLOTS[i], copy);
            mob.setDropChance(GEAR_SLOTS[i], 0.0F);
        }
    }

    /** Pulls gear back off a mob (on death or on recall) so it can be reused. */
    public static void reclaimGear(Member member, Mob mob) {
        for (int i = 0; i < GEAR_SLOTS.length; i++) {
            ItemStack worn = mob.getItemBySlot(GEAR_SLOTS[i]);
            if (!worn.isEmpty()) {
                member.getGear().set(i, makeUnbreakable(worn.copy()));
                mob.setItemSlot(GEAR_SLOTS[i], ItemStack.EMPTY);
            }
        }
    }

    public static ItemStack makeUnbreakable(ItemStack stack) {
        if (stack.isDamageableItem()) {
            stack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
            stack.remove(DataComponents.DAMAGE);
        }
        return stack;
    }

    // ------------------------------------------------------------------ helpers

    public static Component displayName(ResourceLocation typeId) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(typeId)
                .map(EntityType::getDescription)
                .orElse(Component.literal(typeId.toString()));
    }

    private static NonNullList<ItemStack> pad(List<ItemStack> src, int size) {
        NonNullList<ItemStack> out = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(size, src.size()); i++) {
            out.set(i, src.get(i).copy());
        }
        return out;
    }

    // ------------------------------------------------------------------- member

    /** One roster entry: what it is, whether it's currently out, and its kit. */
    public static final class Member {
        public static final Codec<Member> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("type").forGetter(Member::getTypeId),
                Codec.BOOL.optionalFieldOf("active", false).forGetter(Member::isActive),
                ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("gear", List.of()).forGetter(m -> m.gear),
                UUIDUtil.CODEC.optionalFieldOf("entity").forGetter(Member::getEntityId),
                Codec.FLOAT.optionalFieldOf("health", -1.0F).forGetter(Member::getHealth)
        ).apply(inst, Member::new));

        private final ResourceLocation typeId;
        private boolean active;
        private final NonNullList<ItemStack> gear;
        @Nullable
        private UUID entityId;
        private float health;   // -1 = no stored value, spawn at max

        public Member(ResourceLocation typeId) {
            this(typeId, false, List.of(), Optional.empty(), -1.0F);
        }

        private Member(ResourceLocation typeId, boolean active, List<ItemStack> gear,
                       Optional<UUID> entityId, float health) {
            this.typeId = typeId;
            this.active = active;
            this.gear = pad(gear, GEAR_SIZE);
            this.entityId = entityId.orElse(null);
            this.health = health;
        }

        public ResourceLocation getTypeId() {
            return typeId;
        }

        public Optional<EntityType<?>> getType() {
            return BuiltInRegistries.ENTITY_TYPE.getOptional(typeId);
        }

        /** True when the mob is summoned in the world; false when stored. */
        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public NonNullList<ItemStack> getGear() {
            return gear;
        }

        public float getHealth() { return health; }
        public void setHealth(float health) { this.health = health; }

        /** UUID of the live entity while summoned. */
        public Optional<UUID> getEntityId() {
            return Optional.ofNullable(entityId);
        }

        public void setEntityId(@Nullable UUID entityId) {
            this.entityId = entityId;
        }
    }
}
