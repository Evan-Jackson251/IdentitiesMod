package com.schnozz.identitiesmod.menu;

import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerTeam;
import com.schnozz.identitiesmod.networking.snapshots.NomiconSnapshot;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Slot layout: gear (6), stash (18), player inventory (27), hotbar (9).
 *
 * Register the menu type with a network-aware factory so the roster snapshot
 * can ride along on open:
 *
 *   NOMICON = MENUS.register("nomicon",
 *       () -> IMenuTypeExtension.create(NomiconMenu::new));
 */
public class NomiconMenu extends AbstractContainerMenu {

    // --- layout, shared with the screen -------------------------------------
    public static final int WIDTH = 320;
    public static final int HEIGHT = 270;
    public static final int ROW_H = 12;
    public static final int ROSTER_X = 8;
    public static final int ROSTER_Y = 18;
    public static final int ROSTER_W = 96;
    public static final int AVAIL_X = 112;
    public static final int AVAIL_Y = 18;
    public static final int AVAIL_W = 96;
    public static final int AVAIL_ROWS = 10;
    public static final int GEAR_X = 216;
    public static final int GEAR_Y = 18;
    public static final int STASH_X = 8;
    public static final int STASH_Y = 146;
    public static final int INV_X = 8;
    public static final int INV_Y = 186;
    public static final int HOTBAR_Y = 246;

    // --- slot index ranges ---------------------------------------------------
    public static final int GEAR_START = 0;
    public static final int GEAR_COUNT = NecromancerTeam.GEAR_SIZE;
    public static final int STASH_START = GEAR_START + GEAR_COUNT;
    public static final int STASH_COUNT = NecromancerTeam.STASH_SIZE;
    public static final int PLAYER_START = STASH_START + STASH_COUNT;
    public static final int PLAYER_END = PLAYER_START + 36;

    /** Scratch list used when nothing is selected; never persisted. */
    private static final NonNullList<ItemStack> EMPTY_GEAR =
            NonNullList.withSize(NecromancerTeam.GEAR_SIZE, ItemStack.EMPTY);

    private final Player player;
    @Nullable
    private final NecromancerTeam team;          // server side only
    private final Container stash;
    private final Container gear;
    private final DataSlot selected = DataSlot.standalone();

    /** Client-side mirror of roster + tally, refreshed by NomiconSnapshot. */
    private NomiconSnapshot snapshot;

    // ------------------------------------------------------------ constructors

    /** Client constructor, called by the menu type with the openMenu buffer. */
    public NomiconMenu(int windowId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(windowId, inv, null, NomiconSnapshot.read(buf));
    }

    /** Server constructor. */
    public NomiconMenu(int windowId, Inventory inv, NecromancerTeam team) {
        this(windowId, inv, team, NomiconSnapshot.empty());
    }

    /** The only constructor that assigns fields. */
    private NomiconMenu(int windowId, Inventory inv, @Nullable NecromancerTeam team, NomiconSnapshot snapshot) {
        super(ModMenus.NOMICON.get(), windowId);

        this.player = inv.player;
        this.team = team;
        this.snapshot = snapshot;

        // team must already be assigned: GearView reads it through selectedMember().
        this.stash = (team != null) ? new BackedContainer(team.getStash()) : new SimpleContainer(STASH_COUNT);
        this.gear = (team != null) ? new GearView() : new SimpleContainer(GEAR_COUNT);

        this.selected.set(-1);

        // Gear: 2 columns, 3 rows.
        for (int i = 0; i < GEAR_COUNT; i++) {
            int col = i % 2;
            int row = i / 2;
            this.addSlot(new GearSlot(this.gear, i, GEAR_X + col * 18, GEAR_Y + row * 18));
        }

        // Stash: 9 x 2.
        for (int row = 0; row < STASH_COUNT / 9; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(this.stash, row * 9 + col, STASH_X + col * 18, STASH_Y + row * 18));
            }
        }

        // Player inventory and hotbar.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, 9 + row * 9 + col, INV_X + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, INV_X + col * 18, HOTBAR_Y));
        }

        this.addDataSlot(this.selected);
    }

    // ----------------------------------------------------------- selection

    public int getSelected() {
        return selected.get();
    }

    /** Server side. Changing this repoints the gear slots at a different member. */
    public void setSelected(int index) {
        if (team == null) {
            return;
        }
        this.selected.set(team.getMember(index) != null ? index : -1);
        this.broadcastChanges();
    }

    @Nullable
    private NecromancerTeam.Member selectedMember() {
        return team == null ? null : team.getMember(selected.get());
    }

    // ----------------------------------------------------------- client state

    public NomiconSnapshot getSnapshot() {
        return snapshot;
    }

    public void applySnapshot(NomiconSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    // ------------------------------------------------------------ shift-click

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < PLAYER_START) {
            // Gear or stash -> player inventory.
            if (!this.moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Player inventory -> stash.
            if (!this.moveItemStackTo(stack, STASH_START, STASH_START + STASH_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == this.player && player.isAlive();
    }

    // ------------------------------------------------------- container helpers

    /** Split a stack out of a backing list, clearing the slot if it empties. */
    private static ItemStack splitFrom(List<ItemStack> items, int slot, int amount) {
        if (slot < 0 || slot >= items.size() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack current = items.get(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack split = current.split(amount);
        if (current.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
        return split;
    }

    /** Take a whole stack out of a backing list. */
    private static ItemStack takeFrom(List<ItemStack> items, int slot) {
        if (slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack current = items.get(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        items.set(slot, ItemStack.EMPTY);
        return current;
    }

    private static void clearAll(List<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }

    // ----------------------------------------------------------- slot classes

    /** Gear slot: refuses everything when no member is selected, and enforces armor slots. */
    private class GearSlot extends Slot {
        GearSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (team != null && selectedMember() == null) {
                return false;
            }
            EquipmentSlot target = NecromancerTeam.GEAR_SLOTS[this.getSlotIndex()];
            if (target.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                return true; // hands take anything
            }
            Equipable equipable = Equipable.get(stack);
            return equipable != null && equipable.getEquipmentSlot() == target;
        }

        @Override
        public boolean mayPickup(Player player) {
            return team == null || selectedMember() != null;
        }

        @Override
        public void setChanged() {
            // Gear never wears out, so normalise on the way in.
            ItemStack stack = this.getItem();
            if (!stack.isEmpty()) {
                NecromancerTeam.makeUnbreakable(stack);
            }
            super.setChanged();
        }
    }

    /** Container view over a fixed NonNullList that lives in the attachment. */
    private static class BackedContainer implements Container {
        private final NonNullList<ItemStack> items;

        BackedContainer(NonNullList<ItemStack> items) {
            this.items = items;
        }

        @Override public int getContainerSize() { return items.size(); }
        @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
        @Override public ItemStack getItem(int slot) { return items.get(slot); }
        @Override public ItemStack removeItem(int slot, int amount) { return splitFrom(items, slot, amount); }
        @Override public ItemStack removeItemNoUpdate(int slot) { return takeFrom(items, slot); }
        @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); }
        @Override public void setChanged() { }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public void clearContent() { clearAll(items); }
    }

    /**
     * Container view that follows the current selection. Swapping selection
     * changes what these slots read, and broadcastChanges picks up the diff.
     */
    private class GearView implements Container {
        private NonNullList<ItemStack> target() {
            NecromancerTeam.Member member = selectedMember();
            return member != null ? member.getGear() : EMPTY_GEAR;
        }

        @Override public int getContainerSize() { return GEAR_COUNT; }
        @Override public boolean isEmpty() { return target().stream().allMatch(ItemStack::isEmpty); }
        @Override public ItemStack getItem(int slot) { return target().get(slot); }
        @Override public ItemStack removeItem(int slot, int amount) { return splitFrom(target(), slot, amount); }
        @Override public ItemStack removeItemNoUpdate(int slot) { return takeFrom(target(), slot); }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (selectedMember() != null) {
                target().set(slot, stack);
            }
        }

        @Override public void setChanged() { }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public void clearContent() { clearAll(target()); }
    }
}
