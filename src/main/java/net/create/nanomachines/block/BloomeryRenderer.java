package net.create.nanomachines.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BloomeryRenderer extends SafeBlockEntityRenderer<BloomeryBlockEntity> {

    // Söe laadi tekstuuri asukoht — sinu juba valmis tekstuur
    private static final ResourceLocation CHARCOAL_LAYER =
            new ResourceLocation("create_nanomachines", "block/bloomery_charcoal_layer");

    // Y-koordinaadid (bloki-lokaalne 0–1 ruum)
    private static final float FLOOR_Y = 2f / 16f;   // põranda kõrgus (2px)
    private static final float MAX_Y   = 15f / 16f;  // max täitumisel

    // Horisontaalsed piirid igale nurgale
    // (vastavad VoxelShape seintele BloomeryBlock-is)
    private static final float WALL = 2f / 16f;      // seina paksus = 2px

    public BloomeryRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(BloomeryBlockEntity be,
                              float partialTick,
                              PoseStack ms,
                              MultiBufferSource buffer,
                              int light,
                              int overlay) {

        float fill = be.getFillRatio();
        if (fill < 0.001f) return;

        // Söe pinna Y-koordinaat
        float surfaceY = FLOOR_Y + (MAX_Y - FLOOR_Y) * fill;

        // Leia millise PART-iga blokiga tegu on
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof BloomeryBlock)) return;

        BloomeryBlock.BowlPart part = state.getValue(BloomeryBlock.PART);
        BloomeryBlock.StructureType structure = state.getValue(BloomeryBlock.STRUCTURE);

        // Arvuta söe pinna horisontaalsed piirid selle nurgabloki jaoks.
        // Iga nurgabloki sisemus on vastassuunas seina suhtes avatud:
        //   NW: seinad N ja W pool → sissemus SE suunas
        //   NE: seinad N ja E pool → sissemus SW suunas
        //   SW: seinad S ja W pool → sissemus NE suunas
        //   SE: seinad S ja E pool → sissemus NW suunas
        //   SINGLE: suletud igast küljest → sissemus keskel
        float x1, x2, z1, z2;

        if (structure == BloomeryBlock.StructureType.BOWL_2X2) {
            switch (part) {
                case NW -> { x1 = WALL; x2 = 1f; z1 = WALL; z2 = 1f; }
                case NE -> { x1 = 0f;   x2 = 1f - WALL; z1 = WALL; z2 = 1f; }
                case SW -> { x1 = WALL; x2 = 1f; z1 = 0f; z2 = 1f - WALL; }
                case SE -> { x1 = 0f;   x2 = 1f - WALL; z1 = 0f; z2 = 1f - WALL; }
                default  -> { x1 = WALL; x2 = 1f - WALL; z1 = WALL; z2 = 1f - WALL; }
            }
        } else {
            // SINGLE blokk — kõik seinad, sissemus keskel
            x1 = WALL; x2 = 1f - WALL;
            z1 = WALL; z2 = 1f - WALL;
        }

        // Hangi tekstuurisprite
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(CHARCOAL_LAYER);

        VertexConsumer vc = buffer.getBuffer(RenderType.cutoutMipped());
        ms.pushPose();

        // Ülemine pind (mängija vaatab alla)
        renderQuad(ms, vc, sprite, light, overlay,
                x1, surfaceY, z1,
                x1, surfaceY, z2,
                x2, surfaceY, z2,
                x2, surfaceY, z1,
                0f, 1f, 0f);  // normaali suund: +Y

        ms.popPose();
    }

    // ---------------------------------------------------------------
    // Ühe quad'i rendererimine
    // ---------------------------------------------------------------

    private void renderQuad(PoseStack ms, VertexConsumer vc, TextureAtlasSprite sprite,
                            int light, int overlay,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float x3, float y3, float z3,
                            float x4, float y4, float z4,
                            float nx, float ny, float nz) {

        PoseStack.Pose pose = ms.last();
        Matrix4f model  = pose.pose();
        Matrix3f normal = pose.normal();

        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();

        vertex(vc, model, normal, x1, y1, z1, u0, v0, nx, ny, nz, light, overlay);
        vertex(vc, model, normal, x2, y2, z2, u0, v1, nx, ny, nz, light, overlay);
        vertex(vc, model, normal, x3, y3, z3, u1, v1, nx, ny, nz, light, overlay);
        vertex(vc, model, normal, x4, y4, z4, u1, v0, nx, ny, nz, light, overlay);
    }

    private void vertex(VertexConsumer vc, Matrix4f model, Matrix3f normal,
                        float x, float y, float z, float u, float v,
                        float nx, float ny, float nz, int light, int overlay) {
        vc.vertex(model, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}