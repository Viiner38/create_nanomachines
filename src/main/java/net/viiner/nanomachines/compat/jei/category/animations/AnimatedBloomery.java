package net.viiner.nanomachines.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.viiner.nanomachines.block.bloomery.BloomeryBlock;
import net.viiner.nanomachines.block.ModBlocks;

public class AnimatedBloomery {

    private static final BlockState STATE = ModBlocks.BLOOMERY.get().defaultBlockState()
            .setValue(BloomeryBlock.BURNING, true);

    private static final ResourceLocation CHARCOAL_KEY =
            new ResourceLocation("create_nanomachines", "block/burning_bloomery_charcoal_layer");

    // Create JEI shadow — sama sprite sheet mis Create kasutab
    private static final ResourceLocation JEI_WIDGETS =
            new ResourceLocation("create", "textures/gui/jei/widgets.png");
    // JEI_SHADOW: UV (0, 56), suurus 52x11, tekstuur 256x256
    private static final int SHADOW_U = 0, SHADOW_V = 56, SHADOW_W = 52, SHADOW_H = 11;

    // Sama mis BloomeryRenderer.java
    private static final float WALL      = 2f / 16f;
    private static final float FLOOR_Y   = 2f / 16f;
    private static final float SURFACE_Y = FLOOR_Y + (15f / 16f - FLOOR_Y) * 0.85f;

    public void draw(GuiGraphics graphics, int cx, int cy, int blockSize) {
        // 1. Vari bloki all (Create JEI_SHADOW sprite)
        float shadowScale = blockSize / 50f;
        int   sw = (int)(SHADOW_W * shadowScale);
        int   sh = (int)(SHADOW_H * shadowScale);
        int   sx = cx - sw / 2;
        int   sy = cy + (int)(blockSize * 0.30f) - sh / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(JEI_WIDGETS, sx, sy, sw, sh, SHADOW_U, SHADOW_V, SHADOW_W, SHADOW_H, 256, 256);
        RenderSystem.disableBlend();

        // 2. Blokk + charcoal layer
        draw3DBlock(graphics, cx, cy, blockSize);
    }

    private void draw3DBlock(GuiGraphics graphics, int cx, int cy, int blockSize) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(cx, cy, 200);
        ms.mulPose(Axis.XP.rotationDegrees(-15.5f));
        ms.mulPose(Axis.YP.rotationDegrees(22.5f));
        float scale = blockSize / 2f;
        ms.scale(scale, -scale, scale);
        ms.translate(-0.5, -0.5, -0.5);

        // Charcoal layer ENNE bloki seinu
        try {
            TextureAtlasSprite spr = Minecraft.getInstance()
                    .getModelManager()
                    .getAtlas(InventoryMenu.BLOCK_ATLAS)
                    .getSprite(CHARCOAL_KEY);

            MultiBufferSource.BufferSource buf = graphics.bufferSource();
            var vc = buf.getBuffer(RenderType.cutoutMipped());

            float x1 = WALL, x2 = 1f - WALL;
            float z1 = WALL, z2 = 1f - WALL;
            float u0 = spr.getU0(), u1 = spr.getU1();
            float v0 = spr.getV0(), v1 = spr.getV1();
            var pose   = ms.last().pose();
            var normal = ms.last().normal();

            vc.vertex(pose, x1, SURFACE_Y, z1).color(255,255,255,255)
                    .uv(u0,v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.pack(15,15))
                    .normal(normal,0,1,0).endVertex();
            vc.vertex(pose, x1, SURFACE_Y, z2).color(255,255,255,255)
                    .uv(u0,v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.pack(15,15))
                    .normal(normal,0,1,0).endVertex();
            vc.vertex(pose, x2, SURFACE_Y, z2).color(255,255,255,255)
                    .uv(u1,v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.pack(15,15))
                    .normal(normal,0,1,0).endVertex();
            vc.vertex(pose, x2, SURFACE_Y, z1).color(255,255,255,255)
                    .uv(u1,v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.pack(15,15))
                    .normal(normal,0,1,0).endVertex();

            buf.endBatch();
        } catch (Exception ignored) {}

        // Bloki mudel (seinad renderdatakse charcoal peale)
        MultiBufferSource.BufferSource buf = graphics.bufferSource();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                STATE, ms, buf,
                LightTexture.pack(15, 15),
                OverlayTexture.NO_OVERLAY
        );
        buf.endBatch();

        ms.popPose();
    }
}