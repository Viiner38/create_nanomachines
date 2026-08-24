package net.viiner.nanomachines.block.plasmacannon;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.viiner.nanomachines.block.ModBlockEntities;
import net.viiner.nanomachines.block.ModShapes;

public class PlasmaCannonBlock extends DirectionalKineticBlock
        implements IBE<PlasmaCannonBlockEntity>, ICogWheel {


    public PlasmaCannonBlock(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public boolean isSmallCog() { return true; }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return ModShapes.PLASMACANNON;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof PlasmaCannonBlockEntity be))
            return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof DyeItem dye) {
            be.setBeamColor(dye.getDyeColor());
            if (!player.isCreative()) held.shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (held.isEmpty()) {
            be.resetBeamColor();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public Class<PlasmaCannonBlockEntity> getBlockEntityClass() {
        return PlasmaCannonBlockEntity.class;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING)
                .getAxis();
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