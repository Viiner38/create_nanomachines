package net.create.nanomachines.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.create.nanomachines.Nanomachines;
import net.create.nanomachines.block.BloomeryBlock;
import net.create.nanomachines.block.entity.BloomeryBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BloomeryBlockEntityRenderer implements BlockEntityRenderer<BloomeryBlockEntity> {

    private static final ResourceLocation CHARCOAL_TEXTURE =
            new ResourceLocation(Nanomachines.MOD_ID, "block/bloomery_charcoal");

    public BloomeryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BloomeryBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        float fill = be.getFillFraction();
        if (fill <= 0.0f) {
            return;
        }

        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof BloomeryBlock)) {
            return;
        }

        Bounds bounds = getBounds(state);

        float minY = 2.0f / 16.0f;
        float maxY = minY + (14.0f / 16.0f) * fill;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(CHARCOAL_TEXTURE);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        renderCuboid(matrix, normal, consumer, sprite,
                bounds.minX, minY, bounds.minZ,
                bounds.maxX, maxY, bounds.maxZ,
                packedLight, OverlayTexture.NO_OVERLAY);
    }

    private Bounds getBounds(BlockState state) {
        if (state.getValue(BloomeryBlock.STRUCTURE) != BloomeryBlock.StructureType.BOWL_2X2) {
            return new Bounds(2f / 16f, 2f / 16f, 14f / 16f, 14f / 16f);
        }

        return switch (state.getValue(BloomeryBlock.PART)) {
            case NW -> new Bounds(2f / 16f, 2f / 16f, 1f, 1f);
            case NE -> new Bounds(0f, 2f / 16f, 14f / 16f, 1f);
            case SW -> new Bounds(2f / 16f, 0f, 1f, 14f / 16f);
            case SE -> new Bounds(0f, 0f, 14f / 16f, 14f / 16f);
            default -> new Bounds(2f / 16f, 2f / 16f, 14f / 16f, 14f / 16f);
        };
    }

    private void renderCuboid(Matrix4f matrix, Matrix3f normalMatrix, VertexConsumer consumer, TextureAtlasSprite sprite,
                              float x1, float y1, float z1, float x2, float y2, float z2,
                              int light, int overlay) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Ülemine
        vertex(consumer, matrix, normalMatrix, x1, y2, z1, u0, v0, 0, 1, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x1, y2, z2, u0, v1, 0, 1, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x2, y2, z2, u1, v1, 0, 1, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x2, y2, z1, u1, v0, 0, 1, 0, light, overlay);

        // Alumine
        vertex(consumer, matrix, normalMatrix, x1, y1, z1, u0, v0, 0, -1, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x2, y1, z1, u1, v0, 0, -1, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x2, y1, z2, u1, v1, 0, -1, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x1, y1, z2, u0, v1, 0, -1, 0, light, overlay);

        // Põhi
        vertex(consumer, matrix, normalMatrix, x2, y1, z1, u0, v1, 0, 0, -1, light, overlay);
        vertex(consumer, matrix, normalMatrix, x2, y2, z1, u0, v0, 0, 0, -1, light, overlay);
        vertex(consumer, matrix, normalMatrix, x1, y2, z1, u1, v0, 0, 0, -1, light, overlay);
        vertex(consumer, matrix, normalMatrix, x1, y1, z1, u1, v1, 0, 0, -1, light, overlay);

        // Lõuna
        vertex(consumer, matrix, normalMatrix, x1, y1, z2, u0, v1, 0, 0, 1, light, overlay);
        vertex(consumer, matrix, normalMatrix, x1, y2, z2, u0, v0, 0, 0, 1, light, overlay);
        vertex(consumer, matrix, normalMatrix, x2, y2, z2, u1, v0, 0, 0, 1, light, overlay);
        vertex(consumer, matrix, normalMatrix, x2, y1, z2, u1, v1, 0, 0, 1, light, overlay);

        // Lääs
        vertex(consumer, matrix, normalMatrix, x1, y1, z1, u0, v1, -1, 0, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x1, y2, z1, u0, v0, -1, 0, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x1, y2, z2, u1, v0, -1, 0, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x1, y1, z2, u1, v1, -1, 0, 0, light, overlay);

        // Ida
        vertex(consumer, matrix, normalMatrix, x2, y1, z2, u0, v1, 1, 0, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x2, y2, z2, u0, v0, 1, 0, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x2, y2, z1, u1, v0, 1, 0, 0, light, overlay);
        vertex(consumer, matrix, normalMatrix, x2, y1, z1, u1, v1, 1, 0, 0, light, overlay);
    }

    private void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix,
                        float x, float y, float z, float u, float v,
                        float nx, float ny, float nz, int light, int overlay) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
    }

    private record Bounds(float minX, float minZ, float maxX, float maxZ) {
    }
}
