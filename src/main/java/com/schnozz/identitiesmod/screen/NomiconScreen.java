package com.schnozz.identitiesmod.screen;

import com.schnozz.identitiesmod.events.power_events.necromancer.NecromancerTeam;
import com.schnozz.identitiesmod.menu.NomiconMenu;
import com.schnozz.identitiesmod.networking.payloads.NomiconActionPayload;
import com.schnozz.identitiesmod.networking.snapshots.NomiconSnapshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Deliberately texture-free: the background is drawn with filled rectangles so
 * this works before you have any GUI art. Swap renderBg for a blit once you do.
 *
 * Register client side:
 *   @SubscribeEvent
 *   static void onScreens(RegisterMenuScreensEvent event) {
 *       event.register(ModMenus.NOMICON.get(), NomiconScreen::new);
 *   }
 */
public class NomiconScreen extends AbstractContainerScreen<NomiconMenu> {

    private static final int COLOR_BG = 0xFF2B2118;
    private static final int COLOR_PANEL = 0xFF3C2F22;
    private static final int COLOR_SLOT = 0xFF1B1510;
    private static final int COLOR_ROW = 0xFF4A3A2A;
    private static final int COLOR_ROW_SELECTED = 0xFF6E5436;
    private static final int COLOR_TEXT = 0xFFE8DCC8;
    private static final int COLOR_TEXT_DIM = 0xFF9A8B76;

    private int availScroll = 0;

    public NomiconScreen(NomiconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = NomiconMenu.WIDTH;
        this.imageHeight = NomiconMenu.HEIGHT;
        this.inventoryLabelY = NomiconMenu.INV_Y - 11;
    }

    // ------------------------------------------------------------- background

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        gui.fill(x, y, x + imageWidth, y + imageHeight, COLOR_BG);

        panel(gui, x + NomiconMenu.ROSTER_X - 2, y + NomiconMenu.ROSTER_Y - 2,
                NomiconMenu.ROSTER_W + 4, NecromancerTeam.MAX_MEMBERS * NomiconMenu.ROW_H + 4);
        panel(gui, x + NomiconMenu.AVAIL_X - 2, y + NomiconMenu.AVAIL_Y - 2,
                NomiconMenu.AVAIL_W + 4, NomiconMenu.AVAIL_ROWS * NomiconMenu.ROW_H + 4);

        // Roster rows.
        List<NomiconSnapshot.Entry> roster = menu.getSnapshot().roster();
        for (int i = 0; i < NecromancerTeam.MAX_MEMBERS; i++) {
            int rowY = y + NomiconMenu.ROSTER_Y + i * NomiconMenu.ROW_H;
            boolean filled = i < roster.size();
            int color = (i == menu.getSelected()) ? COLOR_ROW_SELECTED : COLOR_ROW;
            gui.fill(x + NomiconMenu.ROSTER_X, rowY,
                    x + NomiconMenu.ROSTER_X + NomiconMenu.ROSTER_W, rowY + NomiconMenu.ROW_H - 1, color);

            if (filled) {
                NomiconSnapshot.Entry entry = roster.get(i);
                Component name = NecromancerTeam.displayName(entry.typeId());
                gui.drawString(font, name, x + NomiconMenu.ROSTER_X + 3, rowY + 2, COLOR_TEXT, false);
                gui.drawString(font, entry.active() ? "\u25CF" : "\u25CB",
                        x + NomiconMenu.ROSTER_X + NomiconMenu.ROSTER_W - 10, rowY + 2, COLOR_TEXT, false);
            } else {
                gui.drawString(font, "-", x + NomiconMenu.ROSTER_X + 3, rowY + 2, COLOR_TEXT_DIM, false);
            }
        }

        // Recruitable types.
        List<Recruit> recruits = recruitable();
        for (int row = 0; row < NomiconMenu.AVAIL_ROWS; row++) {
            int idx = row + availScroll;
            int rowY = y + NomiconMenu.AVAIL_Y + row * NomiconMenu.ROW_H;
            gui.fill(x + NomiconMenu.AVAIL_X, rowY,
                    x + NomiconMenu.AVAIL_X + NomiconMenu.AVAIL_W, rowY + NomiconMenu.ROW_H - 1, COLOR_ROW);
            if (idx >= recruits.size()) {
                continue;
            }
            Recruit recruit = recruits.get(idx);
            boolean affordable = recruit.kills() >= NomiconActionPayload.COST_PER_MEMBER;
            gui.drawString(font, NecromancerTeam.displayName(recruit.typeId()),
                    x + NomiconMenu.AVAIL_X + 3, rowY + 2, affordable ? COLOR_TEXT : COLOR_TEXT_DIM, false);
            String count = String.valueOf(recruit.kills());
            gui.drawString(font, count,
                    x + NomiconMenu.AVAIL_X + NomiconMenu.AVAIL_W - 4 - font.width(count), rowY + 2,
                    affordable ? COLOR_TEXT : COLOR_TEXT_DIM, false);
        }

        // Slot backgrounds.
        for (int i = 0; i < NomiconMenu.GEAR_COUNT; i++) {
            slot(gui, x + NomiconMenu.GEAR_X + (i % 2) * 18, y + NomiconMenu.GEAR_Y + (i / 2) * 18);
        }
        for (int row = 0; row < NomiconMenu.STASH_COUNT / 9; row++) {
            for (int col = 0; col < 9; col++) {
                slot(gui, x + NomiconMenu.STASH_X + col * 18, y + NomiconMenu.STASH_Y + row * 18);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slot(gui, x + NomiconMenu.INV_X + col * 18, y + NomiconMenu.INV_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            slot(gui, x + NomiconMenu.INV_X + col * 18, y + NomiconMenu.HOTBAR_Y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, this.title, 8, 6, COLOR_TEXT, false);
        gui.drawString(font, Component.translatable("gui.identitiesmod.nomicon.recruit"),
                NomiconMenu.AVAIL_X, 6, COLOR_TEXT_DIM, false);
        gui.drawString(font, Component.translatable("gui.identitiesmod.nomicon.gear"),
                NomiconMenu.GEAR_X, 6, COLOR_TEXT_DIM, false);
        gui.drawString(font, this.playerInventoryTitle, NomiconMenu.INV_X, this.inventoryLabelY, COLOR_TEXT_DIM, false);
    }

    private static void panel(GuiGraphics gui, int x, int y, int w, int h) {
        gui.fill(x, y, x + w, y + h, COLOR_PANEL);
    }

    private static void slot(GuiGraphics gui, int x, int y) {
        gui.fill(x - 1, y - 1, x + 17, y + 17, COLOR_SLOT);
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int relX = (int) mouseX - this.leftPos;
        int relY = (int) mouseY - this.topPos;

        // Roster: left click selects, right click removes, middle toggles.
        int rosterRow = rowAt(relX, relY, NomiconMenu.ROSTER_X, NomiconMenu.ROSTER_Y,
                NomiconMenu.ROSTER_W, NecromancerTeam.MAX_MEMBERS);
        if (rosterRow >= 0) {
            if (rosterRow < menu.getSnapshot().roster().size()) {
                if (button == 1) {
                    PacketDistributor.sendToServer(NomiconActionPayload.remove(rosterRow));
                } else if (hasShiftDown()) {
                    PacketDistributor.sendToServer(NomiconActionPayload.toggle(rosterRow));
                } else {
                    PacketDistributor.sendToServer(NomiconActionPayload.select(rosterRow));
                }
            }
            return true;
        }

        // Recruit list: click to spend kills and add.
        int availRow = rowAt(relX, relY, NomiconMenu.AVAIL_X, NomiconMenu.AVAIL_Y,
                NomiconMenu.AVAIL_W, NomiconMenu.AVAIL_ROWS);
        if (availRow >= 0) {
            List<Recruit> recruits = recruitable();
            int idx = availRow + availScroll;
            if (idx < recruits.size()) {
                PacketDistributor.sendToServer(NomiconActionPayload.add(recruits.get(idx).typeId()));
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int relX = (int) mouseX - this.leftPos;
        int relY = (int) mouseY - this.topPos;
        if (rowAt(relX, relY, NomiconMenu.AVAIL_X, NomiconMenu.AVAIL_Y,
                NomiconMenu.AVAIL_W, NomiconMenu.AVAIL_ROWS) >= 0) {
            int max = Math.max(0, recruitable().size() - NomiconMenu.AVAIL_ROWS);
            availScroll = Math.clamp(availScroll - (int) Math.signum(scrollY), 0, max);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private static int rowAt(int relX, int relY, int x, int y, int w, int rows) {
        if (relX < x || relX >= x + w) {
            return -1;
        }
        int row = (relY - y) / NomiconMenu.ROW_H;
        return (relY >= y && row >= 0 && row < rows) ? row : -1;
    }

    // ------------------------------------------------------------------- data

    private record Recruit(ResourceLocation typeId, int kills) {}

    /** Hostile types the player has any kills of, best first. */
    private List<Recruit> recruitable() {
        List<Recruit> out = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Integer> entry : menu.getSnapshot().tally().entrySet()) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entry.getKey()).orElse(null);
            if (type != null && type.getCategory() == MobCategory.MONSTER) {
                out.add(new Recruit(entry.getKey(), entry.getValue()));
            }
        }
        out.sort(Comparator.comparingInt(Recruit::kills).reversed());
        return out;
    }
}
