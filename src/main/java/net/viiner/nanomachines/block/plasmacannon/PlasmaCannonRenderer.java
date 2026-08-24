package net.viiner.nanomachines.block.plasmacannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.viiner.nanomachines.Nanomachines;
import net.viiner.nanomachines.block.ModPartialModels;
import net.viiner.nanomachines.client.ClientBeamTracker;

public class PlasmaCannonRenderer extends KineticBlockEntityRenderer<PlasmaCannonBlockEntity> {

    public static final ResourceLocation BEAM_TEXTURE =
            new ResourceLocation(Nanomachines.MOD_ID, "textures/block/laser_beam.png");

    private static final float BEAM_RADIUS = 0.4375f;
    private static final float GLOW_RADIUS = 0.5f;

    public PlasmaCannonRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(PlasmaCannonBlockEntity be, BlockState state) {
        SuperByteBuffer buf = CachedBuffers.partial(ModPartialModels.PLASMA_CANNON_COG, state);
        switch (state.getValue(PlasmaCannonBlock.FACING).getAxis()) {
            case Y -> buf.rotateCentered(Axis.XP.rotation((float) (-Math.PI / 2)));
            case X -> buf.rotateCentered(Axis.YP.rotation((float) (Math.PI / 2)));
            default -> {}
        }
        return buf;
    }

    @Override
    public boolean shouldRenderOffScreen(PlasmaCannonBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    protected void renderSafe(PlasmaCannonBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }

    public static void renderBeam(ClientBeamTracker.BeamState state, float partialTicks,
                                  PoseStack ms, MultiBufferSource buffer) {
        int len = state.length;
        if (len <= 0) return;

        Direction facing = state.facing;
        int col = state.color;
        float r = ((col >> 16) & 0xFF) / 255f * 0.75f + 0.25f;
        float g = ((col >> 8) & 0xFF) / 255f * 0.75f + 0.25f;
        float b = (col & 0xFF) / 255f * 0.75f + 0.25f;

        ms.pushPose();
        alignToFacing(ms, facing);
        switch (facing) {
            case UP -> ms.translate(0, 1, 0);
            case DOWN -> ms.translate(0, 0, -1);
            case SOUTH -> ms.translate(0, 1, -1);
            case EAST -> ms.translate(-1, 1, 0);
            default -> {}
        }

        BeaconRenderer.renderBeaconBeam(
                ms, buffer, BEAM_TEXTURE, partialTicks, 1.0f, 0L, 0, len,
                new float[]{r, g, b}, BEAM_RADIUS, GLOW_RADIUS
        );

        ms.pushPose();
        ms.translate(0.5, 0, 0.5);
        PoseStack.Pose pose = ms.last();
        VertexConsumer solid = buffer.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, false));
        renderCap(pose, solid, r, g, b, 1.0f, 0, BEAM_RADIUS);
        renderCap(pose, solid, r, g, b, 1.0f, len, BEAM_RADIUS);
        VertexConsumer glow = buffer.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, true));
        renderCap(pose, glow, r, g, b, 0.3f, 0, GLOW_RADIUS);
        renderCap(pose, glow, r, g, b, 0.3f, len, GLOW_RADIUS);
        ms.popPose();

        ms.popPose();
    }

    private static void renderCap(PoseStack.Pose pose, VertexConsumer vc,
                                  float r, float g, float b, float a,
                                  float y, float rad) {
        capQuad(pose, vc, r, g, b, a, y, -rad, -rad, -rad, rad, rad, rad, rad, -rad);
        capQuad(pose, vc, r, g, b, a, y, rad, -rad, rad, rad, -rad, rad, -rad, -rad);
    }

    private static void capQuad(PoseStack.Pose pose, VertexConsumer vc,
                                float r, float g, float b, float a, float y,
                                float x1, float z1, float x2, float z2,
                                float x3, float z3, float x4, float z4) {
        capVertex(pose, vc, r, g, b, a, y, x1, z1, 0f, 0f);
        capVertex(pose, vc, r, g, b, a, y, x2, z2, 0f, 1f);
        capVertex(pose, vc, r, g, b, a, y, x3, z3, 1f, 1f);
        capVertex(pose, vc, r, g, b, a, y, x4, z4, 1f, 0f);
    }

    private static void capVertex(PoseStack.Pose pose, VertexConsumer vc,
                                  float r, float g, float b, float a,
                                  float y, float x, float z, float u, float v) {
        vc.vertex(pose.pose(), x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(pose.normal(), 0f, 1f, 0f)
                .endVertex();
    }

    public static AABB beamAabb(BlockPos origin, Direction d, int len) {
        return new AABB(origin)
                .expandTowards(d.getStepX() * len, d.getStepY() * len, d.getStepZ() * len)
                .inflate(1.5);
    }

    private static void alignToFacing(PoseStack ms, Direction facing) {
        switch (facing) {
            case DOWN -> ms.mulPose(Axis.XP.rotationDegrees(180));
            case NORTH -> ms.mulPose(Axis.XP.rotationDegrees(-90));
            case SOUTH -> ms.mulPose(Axis.XP.rotationDegrees(90));
            case EAST -> ms.mulPose(Axis.ZP.rotationDegrees(-90));
            case WEST -> ms.mulPose(Axis.ZP.rotationDegrees(90));
            default -> {}
        }
    }
}