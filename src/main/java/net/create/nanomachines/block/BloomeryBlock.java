package net.create.nanomachines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.entity.BlockEntityTicker;


// --- AINUS MUUTUS KLASSI PÄISES: lisatud "implements EntityBlock" ---
public class BloomeryBlock extends Block implements EntityBlock {

    public static final EnumProperty<StructureType> STRUCTURE =
            EnumProperty.create("structure", StructureType.class);
    public static final EnumProperty<BowlPart> PART =
            EnumProperty.create("part", BowlPart.class);

    // VoxelShape'id — muutmata, täpselt nagu varem
    private static final VoxelShape SINGLE_SHAPE = Shapes.or(
            Block.box(0, 2, 0, 16, 16, 2),
            Block.box(0, 2, 14, 16, 16, 16),
            Block.box(0, 2, 2, 2, 16, 14),
            Block.box(14, 2, 2, 16, 16, 14),
            Block.box(2, 0, 2, 14, 2, 14)
    );

    private static final VoxelShape NW_SHAPE = Shapes.or(
            Block.box(0, 2, 0, 16, 16, 2),
            Block.box(0, 2, 2, 2, 16, 16),
            Block.box(2, 0, 2, 16, 2, 16)
    );

    private static final VoxelShape NE_SHAPE = Shapes.or(
            Block.box(0, 2, 0, 16, 16, 2),
            Block.box(14, 2, 2, 16, 16, 16),
            Block.box(0, 0, 2, 14, 2, 16)
    );

    private static final VoxelShape SW_SHAPE = Shapes.or(
            Block.box(0, 2, 14, 16, 16, 16),
            Block.box(0, 2, 0, 2, 16, 14),
            Block.box(2, 0, 0, 16, 2, 14)
    );

    private static final VoxelShape SE_SHAPE = Shapes.or(
            Block.box(0, 2, 14, 16, 16, 16),
            Block.box(14, 2, 0, 16, 16, 14),
            Block.box(0, 0, 0, 14, 2, 14)
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

    // ---------------------------------------------------------------
    // EntityBlock implementatsioon — 3 uut meetodit
    // ---------------------------------------------------------------

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.BLOOMERY.get().create(pos, state);
    }

    // UUS — töötab
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.BLOOMERY.get()) return null;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) (BlockEntityTicker<BloomeryBlockEntity>)
                (lvl, pos, st, be) -> be.tick();
        return ticker;
    }

    // ---------------------------------------------------------------
    // Item drop neelamine — mängija kukutab söe ploki sisse
    // ---------------------------------------------------------------

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (level.isClientSide) return;

        if (entity instanceof ItemEntity itemEntity
                && itemEntity.getItem().is(Items.CHARCOAL)
                && !itemEntity.getItem().isEmpty()) {

            if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity bloomery) {
                bloomery.absorbDroppedItem(itemEntity);
            }
        }
    }

    // ---------------------------------------------------------------
    // Kõik allolev on MUUTMATA — täpselt nagu sinu algkoodis
    // ---------------------------------------------------------------

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getBlock() != oldState.getBlock()) {
            scheduleRefresh(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            scheduleRefresh(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        boolean changedBlock = state.getBlock() != newState.getBlock();
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide && changedBlock) {
            scheduleRefresh(level, pos);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refreshAround(level, pos);
    }

    private void scheduleRefresh(Level level, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = center.offset(x, 0, z);
                if (level.getBlockState(checkPos).getBlock() instanceof BloomeryBlock) {
                    level.scheduleTick(checkPos, this, 1);
                }
            }
        }
    }

    private void refreshAround(Level level, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = center.offset(x, 0, z);
                if (level.getBlockState(checkPos).getBlock() instanceof BloomeryBlock) {
                    updateForm(level, checkPos);
                }
            }
        }
    }

    private void updateForm(Level level, BlockPos pos) {
        BlockPos[] possibleOrigins = {
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

        clearSingle(level, pos);
    }

    private boolean canForm2x2(Level level, BlockPos origin) {
        return canJoin2x2(level, origin,                     BowlPart.NW)
                && canJoin2x2(level, origin.east(),          BowlPart.NE)
                && canJoin2x2(level, origin.south(),         BowlPart.SW)
                && canJoin2x2(level, origin.south().east(),  BowlPart.SE);
    }

    private boolean canJoin2x2(Level level, BlockPos pos, BowlPart expectedPart) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BloomeryBlock)) return false;
        StructureType structure = state.getValue(STRUCTURE);
        BowlPart currentPart   = state.getValue(PART);
        if (structure == StructureType.SINGLE) return true;
        return structure == StructureType.BOWL_2X2 && currentPart == expectedPart;
    }

    private void apply2x2(Level level, BlockPos origin) {
        BlockPos nw = origin;
        BlockPos ne = origin.east();
        BlockPos sw = origin.south();
        BlockPos se = origin.south().east();

        setPart(level, nw, BowlPart.NW);
        setPart(level, ne, BowlPart.NE);
        setPart(level, sw, BowlPart.SW);
        setPart(level, se, BowlPart.SE);

        // --- UUED READ: teavita block entity'sid multibloki moodustumisest ---
        notifyBlockEntity(level, nw, true);   // NW = controller
        notifyBlockEntity(level, ne, false);  // ülejäänud = member
        notifyBlockEntity(level, sw, false);
        notifyBlockEntity(level, se, false);
    }

    private void setPart(Level level, BlockPos pos, BowlPart part) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BloomeryBlock)) return;
        BlockState newState = state
                .setValue(STRUCTURE, StructureType.BOWL_2X2)
                .setValue(PART, part);
        if (state != newState) {
            level.setBlock(pos, newState, 3);
        }
    }

    private void clearSingle(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BloomeryBlock)) return;
        BlockState newState = state
                .setValue(STRUCTURE, StructureType.SINGLE)
                .setValue(PART, BowlPart.NONE);
        if (state != newState) {
            level.setBlock(pos, newState, 3);
        }

        // --- UUED READ: teavita block entity't multibloki lagunemisest ---
        if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be) {
            be.setAsStandalone();
        }
    }

    /**
     * Abimeetod: leiab block entity ja kutsub õige setup-meetodi.
     *
     * @param isController true = NW blokk (hoiab kõiki andmeid),
     *                     false = NE/SW/SE blokk (delegeerib NW-le)
     */
    private void notifyBlockEntity(Level level, BlockPos pos, boolean isController) {
        if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be) {
            if (isController) {
                be.setAsController();
            } else {
                be.setAsMember();
            }
        }
    }

    // ---------------------------------------------------------------
    // Shapes — muutmata
    // ---------------------------------------------------------------

    private VoxelShape getCurrentShape(BlockState state) {
        if (state.getValue(STRUCTURE) != StructureType.BOWL_2X2) return SINGLE_SHAPE;
        return switch (state.getValue(PART)) {
            case NW -> NW_SHAPE;
            case NE -> NE_SHAPE;
            case SW -> SW_SHAPE;
            case SE -> SE_SHAPE;
            default -> SINGLE_SHAPE;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        return getCurrentShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
                                        BlockPos pos, CollisionContext context) {
        return getCurrentShape(state);
    }

    // ---------------------------------------------------------------
    // Enum'id — muutmata
    // ---------------------------------------------------------------

    public enum StructureType implements StringRepresentable {
        SINGLE("single"),
        BOWL_2X2("bowl_2x2"),
        LINE_3X1("line_3x1"),
        BOWL_3X3("bowl_3x3");

        private final String name;
        StructureType(String name) { this.name = name; }

        @Override public String getSerializedName() { return name; }
    }

    public enum BowlPart implements StringRepresentable {
        NONE("none"),
        NW("nw"),
        NE("ne"),
        SW("sw"),
        SE("se");

        private final String name;
        BowlPart(String name) { this.name = name; }

        @Override public String getSerializedName() { return name; }
    }
}