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

    // ── Textures ─────────────────────────────────────────────────────────────────

    private static final ResourceLocation CHARCOAL_LAYER =
            new ResourceLocation("create_nanomachines", "block/bloomery_charcoal_layer");
    private static final ResourceLocation BURNING_LAYER =
            new ResourceLocation("create_nanomachines", "block/burning_bloomery_charcoal_layer");
    private static final ResourceLocation STEEL_LAYER =
            new ResourceLocation("create_nanomachines", "block/bloomery_steel_layer");

    // ── Geometry constants ────────────────────────────────────────────────────────

    private static final float WALL         = 2f / 16f;
    private static final float FLOOR_Y      = 2f / 16f;
    private static final float MAX_CH_Y     = 15f / 16f;
    private static final float FIRE_FLOOR_Y = 14f / 16f;
    private static final float FIRE_MAX_Y   = 0.05f;
    private static final int   FIRE_RISE_TICKS = 65;
    private static final int   FADE_START   = BloomeryBlockEntity.FADE_START;

    public BloomeryRenderer(BlockEntityRendererProvider.Context ctx) {}

    // ── Main render ──────────────────────────────────────────────────────────────

    @Override
    protected void renderSafe(BloomeryBlockEntity be,
                              float partialTick,
                              PoseStack ms,
                              MultiBufferSource buffer,
                              int light,
                              int overlay) {

        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof BloomeryBlock)) return;

        BloomeryBlock.StructureType structure = state.getValue(BloomeryBlock.STRUCTURE);
        BloomeryBlock.BowlPart      part      = state.getValue(BloomeryBlock.PART);

        float x1, x2, z1, z2;

        switch (structure) {
            case BOWL_2X2 -> {
                switch (part) {
                    case NW -> { x1=WALL; x2=1f;       z1=WALL; z2=1f;       }
                    case NE -> { x1=0f;  x2=1f-WALL;   z1=WALL; z2=1f;       }
                    case SW -> { x1=WALL; x2=1f;       z1=0f;   z2=1f-WALL;  }
                    case SE -> { x1=0f;  x2=1f-WALL;   z1=0f;   z2=1f-WALL;  }
                    default -> { x1=WALL; x2=1f-WALL;  z1=WALL; z2=1f-WALL;  }
                }
            }
            case LINE_3X1 -> {
                switch (part) {
                    case Z_N -> { x1=WALL; x2=1f-WALL; z1=WALL; z2=1f;      }
                    case Z_M -> { x1=WALL; x2=1f-WALL; z1=0f;   z2=1f;      }
                    case Z_S -> { x1=WALL; x2=1f-WALL; z1=0f;   z2=1f-WALL; }
                    case X_W -> { x1=WALL; x2=1f;      z1=WALL; z2=1f-WALL; }
                    case X_M -> { x1=0f;   x2=1f;      z1=WALL; z2=1f-WALL; }
                    case X_E -> { x1=0f;   x2=1f-WALL; z1=WALL; z2=1f-WALL; }
                    default  -> { x1=WALL; x2=1f-WALL; z1=WALL; z2=1f-WALL; }
                }
            }
            case BOWL_3X3 -> {
                switch (part) {
                    case NW -> { x1=WALL; x2=1f;       z1=WALL; z2=1f;       }
                    case N  -> { x1=0f;   x2=1f;       z1=WALL; z2=1f;       }
                    case NE -> { x1=0f;   x2=1f-WALL;  z1=WALL; z2=1f;       }
                    case W  -> { x1=WALL; x2=1f;       z1=0f;   z2=1f;       }
                    case C  -> { x1=0f;   x2=1f;       z1=0f;   z2=1f;       }
                    case E  -> { x1=0f;   x2=1f-WALL;  z1=0f;   z2=1f;       }
                    case SW -> { x1=WALL; x2=1f;       z1=0f;   z2=1f-WALL;  }
                    case S  -> { x1=0f;   x2=1f;       z1=0f;   z2=1f-WALL;  }
                    case SE -> { x1=0f;   x2=1f-WALL;  z1=0f;   z2=1f-WALL;  }
                    default -> { x1=WALL; x2=1f-WALL;  z1=WALL; z2=1f-WALL;  }
                }
            }
            default -> { x1=WALL; x2=1f-WALL; z1=WALL; z2=1f-WALL; }
        }

        ms.pushPose();
        boolean burning = be.isBurning();

        // ── 1. Charcoal / burning surface ────────────────────────────────────────
        float fill = be.getFillRatio();
        boolean charcoalVisible = fill > 0.001f
                && !be.hasSteelOutput()
                && !(burning && be.getClientBurnProgress(partialTick) >= FADE_START);

        if (charcoalVisible) {
            float surfaceY = FLOOR_Y + (MAX_CH_Y - FLOOR_Y) * fill;
            TextureAtlasSprite sprite = sprite(burning ? BURNING_LAYER : CHARCOAL_LAYER);
            VertexConsumer vc = buffer.getBuffer(RenderType.cutoutMipped());
            renderQuad(ms, vc, sprite, light, overlay,
                    x1, surfaceY, z1, x1, surfaceY, z2,
                    x2, surfaceY, z2, x2, surfaceY, z1, 0,1,0);
        }

        // ── 2. Steel layer ────────────────────────────────────────────────────────
        if (be.hasSteelOutput()) {
            float steelRatio = be.getSteelRatio();
            float steelMaxH  = (MAX_CH_Y - FLOOR_Y) * 0.25f;
            float steelY     = FLOOR_Y + steelMaxH * steelRatio;
            VertexConsumer vc = buffer.getBuffer(RenderType.cutoutMipped());
            renderQuad(ms, vc, sprite(STEEL_LAYER), light, overlay,
                    x1, steelY, z1, x1, steelY, z2,
                    x2, steelY, z2, x2, steelY, z1, 0,1,0);
        }

        ms.popPose();

        // ── 3. Fire particles ─────────────────────────────────────────────────────
        if (burning && be.getLevel() != null && be.getLevel().isClientSide()) {
            spawnFireParticles(be, part, structure, partialTick);
        }
    }

    // ── Fire particles ───────────────────────────────────────────────────────────

    /**
     * Computes particle spawn bounds for this block, extending to cover the wall
     * gap on any side that borders another bloomery block in the same multiblock.
     * This eliminates the visible gap between adjacent blocks.
     */
    private void spawnFireParticles(BloomeryBlockEntity be,
                                    BloomeryBlock.BowlPart part,
                                    BloomeryBlock.StructureType structure,
                                    float partialTick) {
        ClientLevel level = (ClientLevel) be.getLevel();
        if (level == null) return;

        BlockPos pos = be.getBlockPos();
        if (!level.getBlockState(pos.above()).isAir()) return;

        float progress = be.getClientBurnProgress(partialTick);
        float t;
        if (progress < FADE_START) {
            t = Math.min(progress / (float) FIRE_RISE_TICKS, 1f);
        } else {
            t = Math.max(1f - (progress - FADE_START) / 20f, 0f);
        }
        if (t < 0.01f) return;

        // Determine which sides have a bloomery neighbour — extend bound to 0/1
        // so the gap between blocks is filled seamlessly.
        boolean openWest  = isBloomeryNeighbour(level, pos, -1, 0);
        boolean openEast  = isBloomeryNeighbour(level, pos,  1, 0);
        boolean openNorth = isBloomeryNeighbour(level, pos,  0, -1);
        boolean openSouth = isBloomeryNeighbour(level, pos,  0,  1);

        // Base bounds respect walls; open sides stretch to block edge (0 or 1).
        float minX = openWest  ? 0f : WALL;
        float maxX = openEast  ? 1f : 1f - WALL;
        float minZ = openNorth ? 0f : WALL;
        float maxZ = openSouth ? 1f : 1f - WALL;

        double baseX = pos.getX();
        double baseZ = pos.getZ();
        double baseY = pos.getY();

        double innerMinX = baseX + minX;
        double innerMaxX = baseX + maxX;
        double innerMinZ = baseZ + minZ;
        double innerMaxZ = baseZ + maxZ;

        double fireFloor = baseY + FIRE_FLOOR_Y;
        double topY      = fireFloor + FIRE_MAX_Y * t;
        double fireH     = topY - fireFloor;
        if (fireH <= 0.01) return;

        RandomSource rng = level.getRandom();
        int count = (int)(3 * t) + 1;

        for (int i = 0; i < count; i++) {
            double px = innerMinX + rng.nextDouble() * (innerMaxX - innerMinX);
            double pz = innerMinZ + rng.nextDouble() * (innerMaxZ - innerMinZ);
            double py = fireFloor + rng.nextDouble() * fireH;

            level.addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0.02 * t, 0);

            if (rng.nextFloat() < 0.25f) {
                level.addParticle(ParticleTypes.SMOKE, px, topY, pz, 0, 0.01, 0);
            }
        }
    }

    /** Returns true if the block at (pos + dx, pos.y, pos + dz) is also a BloomeryBlock. */
    private boolean isBloomeryNeighbour(ClientLevel level, BlockPos pos, int dx, int dz) {
        return level.getBlockState(pos.offset(dx, 0, dz)).getBlock() instanceof BloomeryBlock;
    }

    // ── Quad rendering ───────────────────────────────────────────────────────────

    private void renderQuad(PoseStack ms, VertexConsumer vc, TextureAtlasSprite sprite,
                            int light, int overlay,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float x3, float y3, float z3,
                            float x4, float y4, float z4,
                            float nx, float ny, float nz) {
        PoseStack.Pose pose   = ms.last();
        Matrix4f       model  = pose.pose();
        Matrix3f       normal = pose.normal();
        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();
        vtx(vc, model, normal, x1,y1,z1, u0,v0, nx,ny,nz, light,overlay);
        vtx(vc, model, normal, x2,y2,z2, u0,v1, nx,ny,nz, light,overlay);
        vtx(vc, model, normal, x3,y3,z3, u1,v1, nx,ny,nz, light,overlay);
        vtx(vc, model, normal, x4,y4,z4, u1,v0, nx,ny,nz, light,overlay);
    }

    private void vtx(VertexConsumer vc, Matrix4f model, Matrix3f normal,
                     float x, float y, float z, float u, float v,
                     float nx, float ny, float nz, int light, int overlay) {
        vc.vertex(model,x,y,z).color(255,255,255,255)
                .uv(u,v).overlayCoords(overlay).uv2(light)
                .normal(normal,nx,ny,nz).endVertex();
    }

    private TextureAtlasSprite sprite(ResourceLocation loc) {
        return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(loc);
    }
}