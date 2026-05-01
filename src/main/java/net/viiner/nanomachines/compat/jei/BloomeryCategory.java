package net.viiner.nanomachines.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.viiner.nanomachines.block.ModBlocks;

import javax.annotation.Nonnull;

public class BloomeryCategory implements IRecipeCategory<BloomeryJEIPlugin.BloomeryRecipe> {

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IGuiHelper guiHelper;

    public BloomeryCategory(IGuiHelper helper) {
        this.guiHelper = helper;
        // Create a simple colored background (177x90 pixels)
        this.background = helper.createBlankDrawable(177, 90);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.BLOOMERY.get()));
        this.slot = helper.getSlotDrawable();
    }

    @Override
    @Nonnull
    public mezz.jei.api.recipe.RecipeType<BloomeryJEIPlugin.BloomeryRecipe> getRecipeType() {
        return BloomeryJEIPlugin.BLOOMERY_BURNING;
    }

    @Override
    @Nonnull
    public Component getTitle() {
        return Component.translatable("nanomachines.jei.bloomery_burning");
    }

    @Override
    @Nonnull
    public IDrawable getBackground() {
        return background;
    }

    @Override
    @Nonnull
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder,
                          @Nonnull BloomeryJEIPlugin.BloomeryRecipe recipe,
                          @Nonnull IFocusGroup focuses) {

        // Input slot 1: Iron variant (left side)
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 38)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.ironInput);

        // Input slot 2: Charcoal (right side)
        builder.addSlot(RecipeIngredientRole.INPUT, 51, 38)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.charcoalInput);

        // Output slot: Steel (far right)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 132, 38)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.output);
    }

    @Override
    public void draw(@Nonnull BloomeryJEIPlugin.BloomeryRecipe recipe,
                     @Nonnull IRecipeSlotsView recipeSlotsView,
                     @Nonnull GuiGraphics graphics,
                     double mouseX, double mouseY) {

        PoseStack ms = graphics.pose();

        // Draw Create-style processing arrow
        AllGuiTextures.JEI_ARROW.render(graphics, 85, 40);

        // Draw fire indicator underneath bloomery (custom drawn)
        drawFlame(graphics, 90, 58);

        // Render Bloomery block model in center
        ms.pushPose();
        ms.translate(90, 25, 0);
        ms.mulPose(Axis.XP.rotationDegrees(-15f));
        ms.mulPose(Axis.YP.rotationDegrees(-22.5f));
        ms.scale(20, 20, 20);

        GuiGameElement.of(new ItemStack(ModBlocks.BLOOMERY.get()))
                .render(graphics);

        ms.popPose();

        // Render Flint and Steel above bloomery (like mixer above basin)
        ms.pushPose();
        ms.translate(90, 5, 100);
        ms.mulPose(Axis.XP.rotationDegrees(-15f));
        ms.mulPose(Axis.YP.rotationDegrees(22.5f));
        ms.scale(16, 16, 16);

        GuiGameElement.of(new ItemStack(Items.FLINT_AND_STEEL))
                .render(graphics);

        ms.popPose();

        // Draw burn time text
        Component text = Component.translatable("nanomachines.jei.bloomery.burn_time", "5:00");
        int textWidth = Minecraft.getInstance().font.width(text);
        graphics.drawString(
                Minecraft.getInstance().font,
                text,
                (177 - textWidth) / 2,
                75,
                0x555555,
                false
        );
    }

    private void drawFlame(GuiGraphics graphics, int x, int y) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(x, y, 0);

        // Draw simple fire shapes using fill() with correct API
        // Left flame (red-orange)
        graphics.fill(-3, 0, 0, 8, 0xFFE8593C);
        // Center flame (yellow-orange)
        graphics.fill(-1, 2, 5, 8, 0xFFF2A623);
        // Right flame (red-orange)
        graphics.fill(2, 1, 8, 8, 0xFFE8593C);

        ms.popPose();
    }
}