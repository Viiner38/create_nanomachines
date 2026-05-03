package net.viiner.nanomachines.compat.jei;

import com.simibubi.create.AllItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.viiner.nanomachines.block.ModBlocks;
import net.viiner.nanomachines.compat.jei.category.BloomeryCategory;
import net.viiner.nanomachines.item.ModItems; // asenda õige paketi teega

import javax.annotation.Nonnull;
import java.util.List;

@JeiPlugin
public class NanomachinesJEI implements IModPlugin {

    public static final RecipeType<BloomeryRecipe> BLOOMERY_BURNING =
            new RecipeType<>(new ResourceLocation("create_nanomachines", "bloomery_burning"), BloomeryRecipe.class);

    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("create_nanomachines", "jei_plugin");
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new BloomeryCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        // Output — asenda ModItems.NANOMACHINES_STEEL oma tegeliku item'iga
        ItemStack steelOutput = new ItemStack(ModItems.NANOMACHINES_STEEL.get());

        registration.addRecipes(BLOOMERY_BURNING, List.of(
                new BloomeryRecipe(new ItemStack(Items.IRON_INGOT),        new ItemStack(Items.CHARCOAL, 16), steelOutput),
                new BloomeryRecipe(new ItemStack(Items.RAW_IRON),          new ItemStack(Items.CHARCOAL, 16), steelOutput),
                new BloomeryRecipe(new ItemStack(AllItems.IRON_SHEET.get()),      new ItemStack(Items.CHARCOAL, 16), steelOutput),
                new BloomeryRecipe(new ItemStack(AllItems.CRUSHED_IRON.get()), new ItemStack(Items.CHARCOAL, 16), steelOutput)
        ));
    }

    // See meetod ütleb JEI-le: "Bloomery blokk KASUTAB seda retseptitüüpi"
    // Ilma selleta ilmub retsept vale koha all
    @Override
    public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLOOMERY.get()), BLOOMERY_BURNING);
    }

    public static class BloomeryRecipe {
        public final ItemStack ironInput;
        public final ItemStack charcoalInput;
        public final ItemStack output;

        public BloomeryRecipe(ItemStack ironInput, ItemStack charcoalInput, ItemStack output) {
            this.ironInput = ironInput;
            this.charcoalInput = charcoalInput;
            this.output = output;
        }
    }
}