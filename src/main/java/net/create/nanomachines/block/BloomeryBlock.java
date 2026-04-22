package net.create.nanomachines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BloomeryBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 2, 0, 16, 16, 2),
            Block.box(0, 2, 14, 16, 16, 16),
            Block.box(0, 2, 2, 2, 16, 14),
            Block.box(14, 2, 2, 16, 16, 14),
            Block.box(2, 0, 2, 14, 2, 14)
    );

    public BloomeryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}