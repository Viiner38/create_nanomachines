package net.create.nanomachines.item;

import net.create.nanomachines.util.ModTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

public class ModToolTiers {
    public static final Tier STEEL = new ForgeTier(3, 800, 6f, 4, 12,
            ModTags.Blocks.NEEDS_STEEL_TOOL, () -> Ingredient.of(ModItems.NANOMACHINES_STEEL.get()));



}
