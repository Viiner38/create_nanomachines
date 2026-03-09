package net.create.nanomachines.util;

import net.create.nanomachines.Nanomachines;
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
}