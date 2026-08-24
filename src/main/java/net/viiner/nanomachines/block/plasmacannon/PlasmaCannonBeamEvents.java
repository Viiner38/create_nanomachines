package net.viiner.nanomachines.block.plasmacannon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.viiner.nanomachines.Nanomachines;
import net.viiner.nanomachines.client.ClientBeamTracker;
import net.viiner.nanomachines.client.PlasmaCannonClient;

@Mod.EventBusSubscriber(
        modid = Nanomachines.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class PlasmaCannonBeamEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;
        PlasmaCannonClient.spawnLightningParticles(mc.level);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || ClientBeamTracker.BEAMS.isEmpty()) return;

        Vec3 cam = event.getCamera().getPosition();
        var ms = event.getPoseStack();
        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();

        for (ClientBeamTracker.BeamState state : ClientBeamTracker.BEAMS.values()) {
            if (!state.dimension.equals(mc.level.dimension())) continue;
            if (event.getFrustum() != null
                    && !event.getFrustum().isVisible(PlasmaCannonRenderer.beamAabb(state.origin, state.facing, state.length))) {
                continue;
            }

            BlockPos pos = state.origin;
            ms.pushPose();
            ms.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
            PlasmaCannonRenderer.renderBeam(state, event.getPartialTick(), ms, buf);
            ms.popPose();
        }

        buf.endBatch(RenderType.beaconBeam(PlasmaCannonRenderer.BEAM_TEXTURE, false));
        buf.endBatch(RenderType.beaconBeam(PlasmaCannonRenderer.BEAM_TEXTURE, true));
    }
}