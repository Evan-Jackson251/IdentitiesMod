package com.schnozz.identitiesmod.items;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.common.Tags;

public class ModToolTiers {
    public static final Tier SCYTHE = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,5000, 9, 0, 3, () -> Ingredient.of(Items.JUNGLE_WOOD) );// tag for mineable blocks)

    //power gauntlets
    public static final Tier FAST_SEASON_1 = new SimpleTier(BlockTags.INCORRECT_FOR_STONE_TOOL, Integer.MAX_VALUE, 0F, 4, 3, null);
    public static final Tier FAST_SEASON_2 = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, Integer.MAX_VALUE, 0F, 5, 3, null);
    public static final Tier FAST_SEASON_3 = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, Integer.MAX_VALUE, 0F, 6, 3, null);
    public static final Tier FAST_SEASON_4 = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, Integer.MAX_VALUE, 0F, 7, 3, null);

    public static final Tier STRONG_SEASON_1 = new SimpleTier(BlockTags.INCORRECT_FOR_STONE_TOOL, Integer.MAX_VALUE, 0F, 8F, 3, null);
    public static final Tier STRONG_SEASON_2 = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, Integer.MAX_VALUE, 0F, 8F, 3, null);
    public static final Tier STRONG_SEASON_3 = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, Integer.MAX_VALUE, 0F, 8F, 3, null);
    public static final Tier STRONG_SEASON_4 = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, Integer.MAX_VALUE, 0F, 9F, 3, null);

}
