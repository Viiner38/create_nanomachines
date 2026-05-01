package net.viiner.nanomachines.compat.jei;

import com.simibubi.create.compat.jei.CreateJEI;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.viiner.nanomachines.Nanomachines;
import net.viiner.nanomachines.block.ModBlocks;
import net.viiner.nanomachines.item.ModItems;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@JeiPlugin
@OnlyIn(Dist.CLIENT)
public class BloomeryJEIPlugin implements IModPlugin {

    private static final ResourceLocation ID = new ResourceLocation(Nanomachines.MOD_ID, "jei_plugin");

    public static final mezz.jei.api.recipe.RecipeType<BloomeryRecipe> BLOOMERY_BURNING =
            new mezz.jei.api.recipe.RecipeType<>(
                    new ResourceLocation(Nanomachines.MOD_ID, "bloomery_burning"),
                    BloomeryRecipe.class
            );

    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new BloomeryCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Create synthetic recipes for all valid iron variants
        List<BloomeryRecipe> recipes = new ArrayList<>();

        // Recipe 1: Iron Ingot -> Steel
        recipes.add(new BloomeryRecipe(
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.CHARCOAL, 16),
                new ItemStack(ModItems.NANOMACHINES_STEEL.get())
        ));

        // Recipe 2: Raw Iron -> Steel
        recipes.add(new BloomeryRecipe(
                new ItemStack(Items.RAW_IRON),
                new ItemStack(Items.CHARCOAL, 16),
                new ItemStack(ModItems.NANOMACHINES_STEEL.get())
        ));

        // Recipe 3: Crushed Raw Iron -> Steel (if Create is loaded)
        try {
            var crushedIron = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("create", "crushed_raw_iron"));
            if (crushedIron != null) {
                recipes.add(new BloomeryRecipe(
                        new ItemStack(crushedIron),
                        new ItemStack(Items.CHARCOAL, 16),
                        new ItemStack(ModItems.NANOMACHINES_STEEL.get())
                ));
            }
        } catch (Exception ignored) {}

        // Recipe 4: Iron Sheet -> Steel (if Create is loaded)
        try {
            var ironSheet = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("create", "iron_sheet"));
            if (ironSheet != null) {
                recipes.add(new BloomeryRecipe(
                        new ItemStack(ironSheet),
                        new ItemStack(Items.CHARCOAL, 16),
                        new ItemStack(ModItems.NANOMACHINES_STEEL.get())
                ));
            }
        } catch (Exception ignored) {}

        registration.addRecipes(BLOOMERY_BURNING, recipes);
    }

    // Simple recipe holder class
    public static class BloomeryRecipe {
        public final ItemStack ironInput;
        public final ItemStack charcoalInput;
        public final ItemStack output;

        public BloomeryRecipe(ItemStack iron, ItemStack charcoal, ItemStack output) {
            this.ironInput = iron;
            this.charcoalInput = charcoal;
            this.output = output;
        }
    }
}