package net.viiner.nanomachines.block.plasmacannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.viiner.nanomachines.Nanomachines;
import net.viiner.nanomachines.block.ModPartialModels;
import net.viiner.nanomachines.client.ClientBeamTracker;
import org.joml.Matrix4f;

public class PlasmaCannonRenderer extends KineticBlockEntityRenderer<PlasmaCannonBlockEntity> {

    public static final ResourceLocation BEAM_TEXTURE =
            new ResourceLocation(Nanomachines.MOD_ID, "textures/block/laser_beam.png");

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
        if (len < 0) return;

        Direction facing = state.facing;
        int col = state.color;
        float r = ((col >> 16) & 0xFF) / 255f;
        float g = ((col >> 8) & 0xFF) / 255f;
        float b = (col & 0xFF) / 255f;
        r = r * 0.75f + 0.25f;
        g = g * 0.75f + 0.25f;
        b = b * 0.75f + 0.25f;

        if (len > 0) {
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
                    new float[]{r, g, b}, 0.4375f, 0.5f
            );
            ms.popPose();
        }
    }

    public static void renderEndCap(PoseStack ms, MultiBufferSource buffer, float r, float g, float b) {
        ms.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        VertexConsumer vc = buffer.getBuffer(RenderType.lightning());
        Matrix4f m = ms.last().pose();
        float s = 0.55f;
        quad(vc, m, -s, -s, s, -s, s, s, -s, s, r, g, b, 1f);
        float s2 = 0.28f;
        quad(vc, m, -s2, -s2, s2, -s2, s2, s2, -s2, s2, 1f, 1f, 1f, 1f);
    }

    private static void quad(VertexConsumer vc, Matrix4f m,
                             float x1, float y1, float x2, float y2,
                             float x3, float y3, float x4, float y4,
                             float r, float g, float b, float a) {
        vc.vertex(m, x1, y1, 0).color(r, g, b, a).endVertex();
        vc.vertex(m, x2, y2, 0).color(r, g, b, a).endVertex();
        vc.vertex(m, x3, y3, 0).color(r, g, b, a).endVertex();
        vc.vertex(m, x4, y4, 0).color(r, g, b, a).endVertex();
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