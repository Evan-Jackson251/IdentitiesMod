package com.schnozz.identitiesmod.client_data;

import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;

public final class ClientSeerData {
    private static EnchantmentMenu owner;
    private static List<List<EnchantmentInstance>> options = List.of();

    public static void set(
            EnchantmentMenu menu,
            List<List<EnchantmentInstance>> received
    ) {
        owner = menu;
        options = received.stream()
                .map(List::copyOf)
                .toList();
    }

    public static List<EnchantmentInstance> get(
            EnchantmentMenu menu,
            int option
    ) {
        if (owner != menu || option < 0 || option >= options.size()) {
            return List.of();
        }

        return options.get(option);
    }

    public static void clear() {
        owner = null;
        options = List.of();
    }
}
