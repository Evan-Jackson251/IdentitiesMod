package com.schnozz.identitiesmod.mixin.client;

import com.schnozz.identitiesmod.client_data.ClientSeerData;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin
        extends AbstractContainerScreen<EnchantmentMenu> {

    protected EnchantmentScreenMixin(
            EnchantmentMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
    }

    @ModifyArgs(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/gui/GuiGraphics;" +
                                    "renderComponentTooltip(" +
                                    "Lnet/minecraft/client/gui/Font;" +
                                    "Ljava/util/List;II)V"
            )
    )
    private void identities$showAllEnchantments(Args args) {
        // Arguments: font, tooltip lines, mouseX, mouseY.
        int mouseX = args.get(2);
        int mouseY = args.get(3);

        int hoveredOption = -1;

        for (int option = 0; option < 3; option++) {
            if (this.isHovering(
                    60, 14 + 19 * option,
                    108, 17,
                    mouseX, mouseY
            )) {
                hoveredOption = option;
                break;
            }
        }

        if (hoveredOption == -1
                || this.menu.costs[hoveredOption] <= 0
                || this.menu.getSlot(0).getItem().isEmpty()) {
            return;
        }

        List<EnchantmentInstance> enchantments =
                ClientSeerData.get(this.menu, hoveredOption);

        // No server-provided preview: keep the vanilla tooltip.
        if (enchantments.isEmpty()) {
            return;
        }

        List<Component> original = args.get(1);
        List<Component> replacement = new ArrayList<>();

        for (Component line : original) {
            if (line.getContents() instanceof TranslatableContents translated
                    && translated.getKey().equals("container.enchant.clue")) {

                // Replace the vanilla "Enchantment ... ?" clue with
                // every enchantment for the hovered option.
                for (EnchantmentInstance enchantment : enchantments) {
                    replacement.add(Enchantment.getFullname(
                            enchantment.enchantment,
                            enchantment.level
                    ));
                }
            } else {
                // Preserve lapis costs, XP requirements, and other lines.
                replacement.add(line);
            }
        }

        args.set(1, replacement);
    }
}
