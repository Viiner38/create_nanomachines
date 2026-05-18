package net.viiner.nanomachines.block.plasmacannon;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.viiner.nanomachines.block.ModPartialModels;

public class PlasmaCannonRenderer extends KineticBlockEntityRenderer<PlasmaCannonBlockEntity> {

    public PlasmaCannonRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(PlasmaCannonBlockEntity be, BlockState state) {
        return CachedBuffers.partial(ModPartialModels.PLASMA_CANNON_COG, state);
    }

}