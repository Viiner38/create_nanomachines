package net.viiner.nanomachines.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BloomeryRenderer extends SafeBlockEntityRenderer<BloomeryBlockEntity> {

    private static final ResourceLocation CHARCOAL_LAYER = new ResourceLocation("create_nanomachines", "block/bloomery_charcoal_layer");
    private static final ResourceLocation BURNING_LAYER = new ResourceLocation("create_nanomachines", "block/burning_bloomery_charcoal_layer");
    private static final ResourceLocation STEEL_LAYER = new ResourceLocation("create_nanomachines", "block/bloomery_steel_layer");

    private static final float WALL = 2f / 16f;
    private static final float FLOOR_Y = 2f / 16f;
    private static final float MAX_CH_Y = 15f / 16f;
    private static final float FIRE_MAX_Y = 0.1f;
    private static final int FADE_START = BloomeryBlockEntity.BURN_DURATION - 20;
    private static final float FIRE_FLOOR_Y = 8f / 16f;
    private static final int FIRE_RISE_TICKS = 60;

    public BloomeryRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    protected void renderSafe(BloomeryBlockEntity be, float partialTick, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof BloomeryBlock)) return;

        BloomeryBlock.BowlPart part = state.getValue(BloomeryBlock.PART);
        BloomeryBlock.StructureType structure = state.getValue(BloomeryBlock.STRUCTURE);

        // Dünaamilised seinad kõikide kujude jaoks
        boolean[] walls = BloomeryBlock.getWalls(structure, part);
        float x1 = walls[2] ? WALL : 0f;
        float x2 = walls[3] ? 1f - WALL : 1f;
        float z1 = walls[0] ? WALL : 0f;
        float z2 = walls[1] ? 1f - WALL : 1f;

        ms.pushPose();
        boolean burning = be.isBurning();

        float fill = be.getFillRatio();
        if (fill > 0.001f && !be.hasSteelOutput()) {
            float surfaceY = FLOOR_Y + (MAX_CH_Y - FLOOR_Y) * fill;
            TextureAtlasSprite sprite = sprite(burning ? BURNING_LAYER : CHARCOAL_LAYER);
            VertexConsumer vc = buffer.getBuffer(RenderType.cutoutMipped());
            renderQuad(ms, vc, sprite, light, overlay, x1, surfaceY, z1, x1, surfaceY, z2, x2, surfaceY, z2, x2, surfaceY, z1, 0, 1, 0);
        }

        if (be.hasSteelOutput()) {
            float steelRatio = be.getSteelRatio();
            float steelMaxH = (MAX_CH_Y - FLOOR_Y) * 0.25f;
            float steelY = FLOOR_Y + steelMaxH * steelRatio;
            VertexConsumer vc = buffer.getBuffer(RenderType.cutoutMipped());
            renderQuad(ms, vc, sprite(STEEL_LAYER), light, overlay, x1, steelY, z1, x1, steelY, z2, x2, steelY, z2, x2, steelY, z1, 0, 1, 0);
        }
        ms.popPose();

        if (burning && be.getLevel() != null && be.getLevel().isClientSide()) {
            spawnFireParticles(be, partialTick);
        }
    }

    // Taastatud partiklite loogika: Iga plokk teeb oma partiklid, eeldusel, et tal pole asju peal.
    private void spawnFireParticles(BloomeryBlockEntity be, float partialTick) {
        ClientLevel level = (ClientLevel) be.getLevel();
        if (level == null) return;

        BlockPos pos = be.getBlockPos();
        // Mängija saab partiklid peatada, pannes selle ploki peale midagi
        if (!level.getBlockState(pos.above()).isAir()) return;

        float progress = be.getClientBurnProgress(partialTick);
        float t;
        if (progress < FADE_START) t = Math.min(progress / (float) FIRE_RISE_TICKS, 1f);
        else t = Math.max(1f - (progress - FADE_START) / 50f, 0f);
        if (t < 0.01f) return;

        RandomSource rng = level.getRandom();

        // Loob partikleid ainult lokaalse 1x1 ploki piires
        double innerMinX = pos.getX() + WALL, innerMaxX = pos.getX() + 1.0 - WALL;
        double innerMinZ = pos.getZ() + WALL, innerMaxZ = pos.getZ() + 1.0 - WALL;
        double fireFloor = pos.getY() + FIRE_FLOOR_Y;
        double topY = fireFloor + FIRE_MAX_Y * t, fireH = topY - fireFloor;

        if (fireH <= 0) return;

        int count = (int) (6 * t) + 1;

        for (int i = 0; i < count; i++) {
            double px = innerMinX + rng.nextDouble() * (innerMaxX - innerMinX);
            double pz = innerMinZ + rng.nextDouble() * (innerMaxZ - innerMinZ);
            double py = fireFloor + rng.nextDouble() * fireH;
            level.addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0.02 * t, 0);
            if (rng.nextFloat() < 0.25f) level.addParticle(ParticleTypes.SMOKE, px, topY, pz, 0, 0.01, 0);
        }
    }

    private void renderQuad(PoseStack ms, VertexConsumer vc, TextureAtlasSprite sprite, int light, int overlay,
                            float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4,
                            float nx, float ny, float nz) {
        PoseStack.Pose pose = ms.last(); Matrix4f model = pose.pose(); Matrix3f normal = pose.normal();
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        vtx(vc, model, normal, x1, y1, z1, u0, v0, nx, ny, nz, light, overlay);
        vtx(vc, model, normal, x2, y2, z2, u0, v1, nx, ny, nz, light, overlay);
        vtx(vc, model, normal, x3, y3, z3, u1, v1, nx, ny, nz, light, overlay);
        vtx(vc, model, normal, x4, y4, z4, u1, v0, nx, ny, nz, light, overlay);
    }

    private void vtx(VertexConsumer vc, Matrix4f model, Matrix3f normal, float x, float y, float z, float u, float v, float nx, float ny, float nz, int light, int overlay) {
        vc.vertex(model, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();
    }

    private TextureAtlasSprite sprite(ResourceLocation loc) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(loc);
    }
}