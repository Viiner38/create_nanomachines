package net.viiner.nanomachines.block.plasmacannon;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.viiner.nanomachines.block.ModBlockEntities;
import net.viiner.nanomachines.block.ModShapes;


public class PlasmaCannonBlock extends KineticBlock implements IBE<PlasmaCannonBlockEntity>, ICogWheel {

    public PlasmaCannonBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return ModShapes.PLASMACANNON;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    public Class<PlasmaCannonBlockEntity> getBlockEntityClass() {
        return PlasmaCannonBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PlasmaCannonBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PLASMACANNON.get();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

}