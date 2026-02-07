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
    //Clone
    public static final Lazy<KeyMapping> CLONE_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.viltrmuite.clone",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.misc"
    ));
    //Viltrumite
    public static final Lazy<KeyMapping> VILTRUMITE_GRAB_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.viltrmuite.grab", // Will be localized using this translation key
            InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
            GLFW.GLFW_KEY_G,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> VILTRUMITE_DASH_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.viltrumite.choke",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> VILTRUMITE_BLOCK_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.viltrumite.block",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.misc"
    ));

    public static final Lazy<KeyMapping> TRAPPER_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.trapper.invisible",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.misc"
    ));

    //Lifestealer Screen
    public static final Lazy<KeyMapping> LIFESTEALER_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.lifestealer.screen",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.misc"
    ));
    //Necromancer
    public static final Lazy<KeyMapping> NECROMANCER_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.necromancer.remove_target",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.misc"
    ));
    //Gravity
    public static final Lazy<KeyMapping> GRAVITY_METEOR_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.gravity.anvil",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> GRAVITY_DRIPSTONE_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.gravity.dripstone",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> GRAVITY_ARROW_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.gravity.arrow",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> GRAVITY_CYCLONE_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.gravity.cyclone",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.misc"
    ));

    //Parry
    public static final Lazy<KeyMapping> PARRY_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.parry.parry",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.misc"
    ));
    //Adaptation
    public static final Lazy<KeyMapping> ADAPTATION_SWITCH_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.adaptation.switch",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.misc"
    ));
    //Kyle
    public static final Lazy<KeyMapping> KYLE_WORTH_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.kyle.kyleworth",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.misc"
    ));
    //Speedster
    public static final Lazy<KeyMapping> SPEEDSTER_LIGHTNING_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.speedster.lightning",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.misc"
    ));
    //Time Lord
    public static final Lazy<KeyMapping> TIME_STOP_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.time_lord.time_stop",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> REWIND_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.time_lord.rewind",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.misc"
    ));
    public static final Lazy<KeyMapping> SNAPSHOT_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.time_lord.snapshot",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.misc"
    ));
    //Dragon
    public static final Lazy<KeyMapping> DRAGON_SHIFT = Lazy.of(() -> new KeyMapping(
            "key.identitiesmod.time_lord.dragon_shift",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.misc"
    ));

    // Event is on the mod event bus only on the physical client
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        //Viltrumite mappings
        event.register(VILTRUMITE_GRAB_MAPPING.get());
        event.register(VILTRUMITE_DASH_MAPPING.get());
        event.register(VILTRUMITE_BLOCK_MAPPING.get());
        //Gravity mappings
        event.register(GRAVITY_DRIPSTONE_MAPPING.get());
        event.register(GRAVITY_CYCLONE_MAPPING.get());
        event.register(GRAVITY_ARROW_MAPPING.get());
        event.register(GRAVITY_METEOR_MAPPING.get());

        //Adaptation mapping
        event.register(ADAPTATION_SWITCH_MAPPING.get());

        //Parry mapping
        event.register(PARRY_MAPPING.get());

        //Lifestealer mapping
        event.register(LIFESTEALER_MAPPING.get());

        //Kyle
        event.register(KYLE_WORTH_MAPPING.get());

        //Speedster
        event.register(SPEEDSTER_LIGHTNING_MAPPING.get());

        //Time Lord
        event.register(TIME_STOP_MAPPING.get());
        event.register(SNAPSHOT_MAPPING.get());
        event.register(REWIND_MAPPING.get());

        //Dragon
        event.register(DRAGON_SHIFT.get());

        //Clone
        event.register(CLONE_MAPPING.get());
    }
}

