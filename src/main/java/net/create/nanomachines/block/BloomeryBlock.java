package net.create.nanomachines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BloomeryBlock extends Block {

    public static final EnumProperty<StructureType> STRUCTURE =
            EnumProperty.create("structure", StructureType.class);

    public static final EnumProperty<BowlPart> PART =
            EnumProperty.create("part", BowlPart.class);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 2, 0, 16, 16, 2),
            Block.box(0, 2, 14, 16, 16, 16),
            Block.box(0, 2, 2, 2, 16, 14),
            Block.box(14, 2, 2, 16, 16, 14),
            Block.box(2, 0, 2, 14, 2, 14)
    );

    public BloomeryBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STRUCTURE, StructureType.SINGLE)
                .setValue(PART, BowlPart.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STRUCTURE, PART);
    }
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (level.isClientSide) {
            return;
        }

        updateForm(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (level.isClientSide) {
            return;
        }

        updateForm(level, pos);
    }
    private void updateForm(Level level, BlockPos pos) {
        BlockPos[] possibleOrigins = new BlockPos[] {
                pos,
                pos.west(),
                pos.north(),
                pos.north().west()
        };

        for (BlockPos origin : possibleOrigins) {
            if (canForm2x2(level, origin)) {
                apply2x2(level, origin);
                return;
            }
        }

        clearForm(level, pos);
    }

    private boolean canForm2x2(Level level, BlockPos origin) {
        BlockPos nw = origin;
        BlockPos ne = origin.east();
        BlockPos sw = origin.south();
        BlockPos se = origin.south().east();

        return isBloomery(level, nw)
                && isBloomery(level, ne)
                && isBloomery(level, sw)
                && isBloomery(level, se);
    }

    private boolean isBloomery(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof BloomeryBlock;
    }

    private void apply2x2(Level level, BlockPos origin) {
    }

    private void clearForm(Level level, BlockPos pos) {
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public enum StructureType implements StringRepresentable {
        SINGLE("single"),
        BOWL_2X2("bowl_2x2"),
        LINE_3X1("line_3x1"),
        BOWL_3X3("bowl_3x3");

        private final String name;

        StructureType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum BowlPart implements StringRepresentable {
        NONE("none"),
        NW("nw"),
        NE("ne"),
        SW("sw"),
        SE("se");

        private final String name;

        BowlPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}