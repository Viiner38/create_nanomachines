package net.viiner.nanomachines.util;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.viiner.nanomachines.Nanomachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static class Blocks {
        public static final TagKey<Block> NEEDS_STEEL_TOOL = createTag("needs_steel_tool");
        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Nanomachines.MOD_ID, name));
        }
    }
    public static class Items {
        public static final TagKey<Item> COLORED_HF = createTag("colored_hf");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Nanomachines.MOD_ID, name));
        }
    }

}