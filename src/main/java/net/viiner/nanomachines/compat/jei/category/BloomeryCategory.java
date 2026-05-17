package net.viiner.nanomachines.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.viiner.nanomachines.block.ModBlocks;
import net.viiner.nanomachines.compat.jei.NanomachinesJEI;
import net.viiner.nanomachines.compat.jei.category.animations.AnimatedBloomery;

import javax.annotation.Nonnull;

public class BloomeryCategory implements IRecipeCategory<NanomachinesJEI.BloomeryRecipe> {

    private static final ResourceLocation JEI_WIDGETS =
            new ResourceLocation("create", "textures/gui/jei/widgets.png");
    private static final int ARROW_U = 19, ARROW_V = 10, ARROW_W = 42, ARROW_H = 10;

    private final AnimatedBloomery animatedBloomery = new AnimatedBloomery();

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;

    private static final int W = 170;
    private static final int H = 90;

    private static final int BLOCK_CX   = W / 2 - 15;
    private static final int BLOCK_CY   = H / 2 - 4;
    private static final int BLOCK_SIZE = 60;

    public BloomeryCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(W, H);
        this.icon  = helper.createDrawableItemStack(new ItemStack(ModBlocks.BLOOMERY.get()));
        this.slot  = helper.getSlotDrawable();
        this.arrow = helper.drawableBuilder(JEI_WIDGETS, ARROW_U, ARROW_V, ARROW_W, ARROW_H)
                .setTextureSize(256, 256)
                .build();
    }

    @Override @Nonnull
    public mezz.jei.api.recipe.RecipeType<NanomachinesJEI.BloomeryRecipe> getRecipeType() {
        return NanomachinesJEI.BLOOMERY_BURNING;
    }
    @Override @Nonnull public Component getTitle()      { return Component.literal("Bloomery Burning"); }
    @Override @Nonnull public IDrawable getBackground() { return background; }
    @Override @Nonnull public IDrawable getIcon()       { return icon; }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder,
                          @Nonnull NanomachinesJEI.BloomeryRecipe recipe,
                          @Nonnull IFocusGroup focuses) {

        builder.addSlot(RecipeIngredientRole.INPUT, 15, 26)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.ironInput);

        builder.addSlot(RecipeIngredientRole.INPUT, 15, 46)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.charcoalInput);

        builder.addSlot(RecipeIngredientRole.OUTPUT, W - 30, 36)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.output);
    }

    @Override
    public void draw(@Nonnull NanomachinesJEI.BloomeryRecipe recipe,
                     @Nonnull IRecipeSlotsView recipeSlotsView,
                     @Nonnull GuiGraphics graphics,
                     double mouseX, double mouseY) {

        animatedBloomery.draw(graphics, BLOCK_CX, BLOCK_CY, BLOCK_SIZE);

        graphics.renderItem(new ItemStack(Items.FLINT_AND_STEEL),
                BLOCK_CX - 8, 67);

        arrow.draw(graphics, 94, 39);
    }
}