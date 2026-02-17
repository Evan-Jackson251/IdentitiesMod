package com.schnozz.identitiesmod.sounds;

import com.schnozz.identitiesmod.IdentitiesMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    // Assuming that your mod id is examplemod
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, IdentitiesMod.MODID);

    public static final Supplier<SoundEvent> PARRY_SOUND = registerSoundEvent("parry_sound");
    public static final Supplier<SoundEvent> ADAPTATION_SOUND = registerSoundEvent("adaptation_sound");
    public static final Supplier<SoundEvent> ZOMBIES_ARE_COMING_SOUND = registerSoundEvent("zombies_are_coming_sound");
    public static final Supplier<SoundEvent> OMNI_MAN_PUNCH_SOUND = registerSoundEvent("omni_man_punch_sound");
    public static final Supplier<SoundEvent> PUNCH_THUNK_SOUND = registerSoundEvent("punch_thunk_sound");
    public static final Supplier<SoundEvent> LIGHT_PUNCH_SOUND = registerSoundEvent("light_punch_sound");
    public static final Supplier<SoundEvent> MAHORAGA_THEME_SOUND = registerSoundEvent("mahoraga_theme_sound");
    public static final Supplier<SoundEvent> GRAB_SOUND = registerSoundEvent("grab_sound");
    public static final Supplier<SoundEvent> WIND_BLOWING_SOUND = registerSoundEvent("wind_blowing_sound");
    public static final Supplier<SoundEvent> SWOOSH_SOUND = registerSoundEvent("swoosh_sound");
    public static final Supplier<SoundEvent> TIME_STOP_SOUND = registerSoundEvent("time_stop_sound");

    public static final Supplier<SoundEvent> DOG_DISC_SOUND = registerSoundEvent("dog_disc_sound");
    public static final ResourceKey<JukeboxSong> DOG_DISC_KEY = createSong("dog_disc_sound");

    public static final Supplier<SoundEvent> DEF_CAT_SOUND = registerSoundEvent("def_cat_sound");
    public static final ResourceKey<JukeboxSong> DEF_CAT_KEY = createSong("def_cat_sound");

    public static ResourceKey<JukeboxSong> createSong(String name)
    {
        return ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, name));
    }

    private static Supplier<SoundEvent> registerSoundEvent(String name)
    {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus)
    {
        SOUND_EVENTS.register(eventBus);
    }
}
