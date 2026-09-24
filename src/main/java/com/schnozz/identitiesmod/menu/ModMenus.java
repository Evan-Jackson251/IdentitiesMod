package com.schnozz.identitiesmod.menu;

import com.schnozz.identitiesmod.IdentitiesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, IdentitiesMod.MODID);

    public static final Supplier<MenuType<NomiconMenu>> NOMICON = MENU_TYPES.register("nomicon",
                   () -> IMenuTypeExtension.create(NomiconMenu::new));
}
