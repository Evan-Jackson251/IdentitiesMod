package com.schnozz.identitiesmod.items;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.items.item_classes.*;
import com.schnozz.identitiesmod.sounds.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ItemRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IdentitiesMod.MODID);

    //Necromancer
    public static final DeferredItem<Item> NECROROD = ITEMS.registerItem(
            "necrorod",
            Necrorod::new, // The factory that the properties will be passed into.
            new Item.Properties().stacksTo(1) // The properties to use.
    );
    public static final DeferredItem<Item> MOB_HOLDER = ITEMS.registerItem(
            "mobholder",
            MobHolder::new, // The factory that the properties will be passed into.
            new Item.Properties().stacksTo(1) // The properties to use.
    );


    public static final DeferredItem<Item> DOG_MUSIC_DISC = ITEMS.register("dog_music_disc", () ->
            new Item(
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.RARE)
                            .jukeboxPlayable(ModSounds.DOG_DISC_KEY)
            ));

    public static final DeferredItem<Item> DEF_CAT = ITEMS.register("def_cat", () ->
            new Item(
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.RARE)
                            .jukeboxPlayable(ModSounds.DEF_CAT_KEY)
            ));
    public static final DeferredItem<Item> BONE_WHISTLE = ITEMS.registerItem(
            "bone_whistle",
            BoneWhistle::new, // The factory that the properties will be passed into.
            new Item.Properties().stacksTo(1) // The properties to use.
    );
    public static final DeferredItem<Item> BONE_SHEARS = ITEMS.registerItem(
            "bone_shears",
            BoneShears::new, // The factory that the properties will be passed into.
            new Item.Properties().stacksTo(1) // The properties to use.
    );

    //Kyle
    public static final Supplier<SwordItem> SCYTHE = ITEMS.register("scythe", () -> new Scythe(
            // The tier to use.
            ModToolTiers.SCYTHE,
            // The item properties. We don't need to set the durability here because TieredItem handles that for us.
            new Item.Properties().stacksTo(1).attributes(
                    // There are `createAttributes` methods in either the class or subclass of each DiggerItem
                    SwordItem.createAttributes(
                            // The tier to use.
                            ModToolTiers.SCYTHE,
                            // The type-specific attack damage bonus. 3 for swords, 1.5 for shovels, 1 for pickaxes, varying for axes and hoes.
                            1.5f,
                            // The type-specific attack speed modifier. The player has a default attack speed of 4, so to get to the desired
                            // value of 1.6f, we use -2.4f. -2.4f for swords, -3f for shovels, -2.8f for pickaxes, varying for axes and hoes.
                            -2.8f
                            )
            )
    ));

    //power gauntlets
    public static final Supplier<SwordItem> STRONG_S1_GAUNTLET = ITEMS.register("strong_s1_gauntlet", () -> new StrongPowerGauntlet(
            ModToolTiers.STRONG_SEASON_1,
            new Item.Properties().stacksTo(1).attributes(
                    SwordItem.createAttributes(
                            ModToolTiers.STRONG_SEASON_1,
                            0f,
                            -3.2f
                    )
            )
    ));
    public static final Supplier<SwordItem> FAST_S1_GAUNTLET = ITEMS.register("fast_s1_gauntlet", () -> new  FastPowerGauntlet(
            ModToolTiers.FAST_SEASON_1,
            new Item.Properties().stacksTo(1).attributes(
                    SwordItem.createAttributes(
                            ModToolTiers.FAST_SEASON_1,
                            0,
                            -2.4f
                    )
            )
    ));
    public static final Supplier<SwordItem> STRONG_S2_GAUNTLET = ITEMS.register("strong_s2_gauntlet", () -> new StrongPowerGauntlet(
            ModToolTiers.STRONG_SEASON_2,
            new Item.Properties().stacksTo(1).attributes(
                    SwordItem.createAttributes(
                            ModToolTiers.STRONG_SEASON_2,
                            0f,
                            -3.1f
                    )
            )
    ));
    public static final Supplier<SwordItem> FAST_S2_GAUNTLET = ITEMS.register("fast_s2_gauntlet", () -> new FastPowerGauntlet(
            ModToolTiers.FAST_SEASON_2,
            new Item.Properties().stacksTo(1).attributes(
                    SwordItem.createAttributes(
                            ModToolTiers.FAST_SEASON_2,
                            0,
                            -2.4f
                    )
            )
    ));public static final Supplier<SwordItem> STRONG_S3_GAUNTLET = ITEMS.register("strong_s3_gauntlet", () -> new StrongPowerGauntlet(
            ModToolTiers.STRONG_SEASON_3,
            new Item.Properties().stacksTo(1).attributes(
                    SwordItem.createAttributes(
                            ModToolTiers.STRONG_SEASON_3,
                            0f,
                            -3f
                    )
            )
    ));
    public static final Supplier<SwordItem> FAST_S3_GAUNTLET = ITEMS.register("fast_s3_gauntlet", () -> new FastPowerGauntlet(
            ModToolTiers.FAST_SEASON_3,
            new Item.Properties().stacksTo(1).attributes(
                    SwordItem.createAttributes(
                            ModToolTiers.FAST_SEASON_3,
                            0,
                            -2.4f
                    )
            )
    ));public static final Supplier<SwordItem> STRONG_S4_GAUNTLET = ITEMS.register("strong_s4_gauntlet", () -> new StrongPowerGauntlet(
            ModToolTiers.STRONG_SEASON_4,
            new Item.Properties().stacksTo(1).attributes(
                    SwordItem.createAttributes(
                            ModToolTiers.STRONG_SEASON_4,
                            0f,
                            -3f
                    )
            )
    ));
    public static final Supplier<SwordItem> FAST_S4_GAUNTLET = ITEMS.register("fast_s4_gauntlet", () -> new FastPowerGauntlet(
            ModToolTiers.FAST_SEASON_4,
            new Item.Properties().stacksTo(1).attributes(
                    SwordItem.createAttributes(
                            ModToolTiers.FAST_SEASON_4,
                            0,
                            -2.4f
                    )
            )
    ));
    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}
