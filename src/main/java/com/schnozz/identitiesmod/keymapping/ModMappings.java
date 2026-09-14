package com.schnozz.identitiesmod.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import com.schnozz.identitiesmod.IdentitiesMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;


@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModMappings {
    //POWER CHOOSING SCREEN
    public static final Lazy<KeyMapping> POWER_SCREEN = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.universal.power_screen",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "key.categories.misc"
    ));
    //universal ability mappings
    public static final Lazy<KeyMapping> PRIMARY_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.universal.primary",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> SECONDARY_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.universal.secondary",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> UTILITY_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.universal.utility",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> SPECIAL_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.universal.special",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "key.categories.misc"
    ));
    //Clone
    public static final Lazy<KeyMapping> CLONE_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.clone.clone",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> TELEPORT_CLONE_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.clone.teleport_clone",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> KILL_CLONE_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.clone.kill_clone",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> AGGRESIVE_MODE_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.clone.aggresive_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> PEACEFUL_MODE_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.clone.peaceful_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> FOLLOW_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.clone.follow",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> UNFOLLOW_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.clone.unfollow",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_T,
            "key.categories.misc"
    ));

    // Event is on the mod event bus only on the physical client
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        //POWER CHOOSING SCREEN
        event.register(POWER_SCREEN.get());

        //Universal Mappings
        event.register(PRIMARY_MAPPING.get());
        event.register(SECONDARY_MAPPING.get());
        event.register(UTILITY_MAPPING.get());
        event.register(SPECIAL_MAPPING.get());

        //Clone
        event.register(CLONE_MAPPING.get());
        event.register(TELEPORT_CLONE_MAPPING.get());
        event.register(KILL_CLONE_MAPPING.get());
        event.register(AGGRESIVE_MODE_MAPPING.get());
        event.register(PEACEFUL_MODE_MAPPING.get());
        event.register(FOLLOW_MAPPING.get());
        event.register(UNFOLLOW_MAPPING.get());

    }
}

