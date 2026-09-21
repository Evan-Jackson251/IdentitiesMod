package com.schnozz.identitiesmod.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.List;

public class SeerEnchantmentMixin {
    @Mixin(EnchantmentMenu.class)
    public interface EnchantmentMenuInvoker {
        @Invoker("getEnchantmentList")
        List<EnchantmentInstance> identities$getEnchantmentList(
                RegistryAccess registries,
                ItemStack stack,
                int slot,
                int cost
        );
    }
}
