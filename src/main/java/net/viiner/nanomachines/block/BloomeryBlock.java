package net.viiner.nanomachines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class BloomeryBlock extends Block implements EntityBlock {

    public static final EnumProperty<StructureType> STRUCTURE =
            EnumProperty.create("structure", StructureType.class);
    public static final EnumProperty<BowlPart> PART =
            EnumProperty.create("part", BowlPart.class);
    public static final BooleanProperty BURNING = BooleanProperty.create("burning");

    // ── Shapes ───────────────────────────────────────────────────────────────────

    private static final VoxelShape SINGLE_SHAPE = Shapes.or(
            Block.box(0,2,0,16,16,2), Block.box(0,2,14,16,16,16),
            Block.box(0,2,2,2,16,14), Block.box(14,2,2,16,16,14),
            Block.box(2,0,2,14,2,14));

    private static final VoxelShape NW_SHAPE = Shapes.or(
            Block.box(0,2,0,16,16,2), Block.box(0,2,2,2,16,16), Block.box(2,0,2,16,2,16));
    private static final VoxelShape NE_SHAPE = Shapes.or(
            Block.box(0,2,0,16,16,2), Block.box(14,2,2,16,16,16), Block.box(0,0,2,14,2,16));
    private static final VoxelShape SW_SHAPE = Shapes.or(
            Block.box(0,2,14,16,16,16), Block.box(0,2,0,2,16,14), Block.box(2,0,0,16,2,14));
    private static final VoxelShape SE_SHAPE = Shapes.or(
            Block.box(0,2,14,16,16,16), Block.box(14,2,0,16,16,14), Block.box(0,0,0,14,2,14));

    private static final VoxelShape N_SHAPE = Shapes.or(
            Block.box(0,2,0,16,16,2), Block.box(0,0,2,16,2,16));
    private static final VoxelShape S_SHAPE = Shapes.or(
            Block.box(0,2,14,16,16,16), Block.box(0,0,0,16,2,14));
    private static final VoxelShape W_SHAPE = Shapes.or(
            Block.box(0,2,0,2,16,16), Block.box(2,0,0,16,2,16));
    private static final VoxelShape E_SHAPE = Shapes.or(
            Block.box(14,2,0,16,16,16), Block.box(0,0,0,14,2,16));
    private static final VoxelShape C_SHAPE  = Block.box(0,0,0,16,2,16);

    private static final VoxelShape Z_N_SHAPE = Shapes.or(
            Block.box(0,2,0,16,16,2), Block.box(0,2,2,2,16,16),
            Block.box(14,2,2,16,16,16), Block.box(2,0,2,14,2,16));
    private static final VoxelShape Z_M_SHAPE = Shapes.or(
            Block.box(0,2,0,2,16,16), Block.box(14,2,0,16,16,16), Block.box(2,0,0,14,2,16));
    private static final VoxelShape Z_S_SHAPE = Shapes.or(
            Block.box(0,2,14,16,16,16), Block.box(0,2,0,2,16,14),
            Block.box(14,2,0,16,16,14), Block.box(2,0,0,14,2,14));
    private static final VoxelShape X_W_SHAPE = Shapes.or(
            Block.box(0,2,0,2,16,16), Block.box(2,2,0,16,16,2),
            Block.box(2,2,14,16,16,16), Block.box(2,0,2,16,2,14));
    private static final VoxelShape X_M_SHAPE = Shapes.or(
            Block.box(0,2,0,16,16,2), Block.box(0,2,14,16,16,16), Block.box(0,0,2,16,2,14));
    private static final VoxelShape X_E_SHAPE = Shapes.or(
            Block.box(14,2,0,16,16,16), Block.box(0,2,0,14,16,2),
            Block.box(0,2,14,14,16,16), Block.box(0,0,2,14,2,14));

    // ── Constructor ──────────────────────────────────────────────────────────────

    public BloomeryBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STRUCTURE, StructureType.SINGLE)
                .setValue(PART, BowlPart.NONE)
                .setValue(BURNING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STRUCTURE, PART, BURNING);
    }

    // ── EntityBlock ──────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.BLOOMERY.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.BLOOMERY.get()) return null;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> ticker = (BlockEntityTicker<T>)
                (BlockEntityTicker<BloomeryBlockEntity>) (lvl, pos, st, be) -> be.tick();
        return ticker;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(BURNING) ? 15 : 0;
    }

    // ── Interaction ──────────────────────────────────────────────────────────────

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof BloomeryBlockEntity be))
            return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);

        if (held.is(Items.FLINT_AND_STEEL)) {
            if (be.canStartBurning()) {
                be.startBurning(level, pos);
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        if (be.isBurning()) return InteractionResult.PASS;

        if (be.isCharcoal(held)) {
            ItemStack rem = held.copy();
            for (BloomeryBlockEntity m : be.getMultiblockGroup()) {
                if (rem.isEmpty()) break;
                rem = m.tryInsert(rem, false);
            }
            int inserted = held.getCount() - rem.getCount();
            if (inserted > 0) { player.getItemInHand(hand).shrink(inserted); return InteractionResult.CONSUME; }
            return InteractionResult.PASS;
        }

        if (be.isValidIron(held)) {
            ItemStack rem = be.tryInsertIron(held, false);
            if (rem.getCount() < held.getCount()) {
                player.setItemInHand(hand, rem.isEmpty() ? ItemStack.EMPTY : rem);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        if (held.isEmpty()) {
            ItemStack steel = be.tryExtractSteelMultiblock(1, false);
            if (!steel.isEmpty()) { player.getInventory().add(steel); return InteractionResult.CONSUME; }
            List<BloomeryBlockEntity> group = be.getMultiblockGroup();
            int total = group.stream().mapToInt(BloomeryBlockEntity::getCharcoalAmount).sum();
            int toExtract = player.isShiftKeyDown() ? total : 1;
            ItemStack charcoal = be.tryExtractMultiblock(toExtract, false);
            if (!charcoal.isEmpty()) { player.getInventory().add(charcoal); return InteractionResult.CONSUME; }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide) return;
        if (entity instanceof ItemEntity ie && !ie.getItem().isEmpty())
            if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be)
                if (be.isCharcoal(ie.getItem()) || be.isValidIron(ie.getItem()))
                    be.absorbDroppedItem(ie);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be) {
                int amt = be.getCharcoalAmount();
                while (amt > 0) { int d = Math.min(amt,64); popResource(level,pos,new ItemStack(Items.CHARCOAL,d)); amt-=d; }
                if (!be.getIronItem().isEmpty())    popResource(level, pos, be.getIronItem());
                if (!be.getSteelOutput().isEmpty()) popResource(level, pos, be.getSteelOutput());
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
            if (!level.isClientSide) scheduleRefresh(level, pos);
        } else {
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    public static void setBurningState(Level level, BlockPos pos, boolean burning) {
        BlockState st = level.getBlockState(pos);
        if (st.getBlock() instanceof BloomeryBlock)
            level.setBlock(pos, st.setValue(BURNING, burning), 3);
    }

    // ── Multiblock formation ─────────────────────────────────────────────────────

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getBlock() != oldState.getBlock())
            scheduleRefresh(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide) scheduleRefresh(level, pos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refreshAround(level, pos);
    }

    private void scheduleRefresh(Level level, BlockPos center) {
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            BlockPos p = center.offset(x, 0, z);
            if (level.getBlockState(p).getBlock() instanceof BloomeryBlock)
                level.scheduleTick(p, this, 1);
        }
    }

    private void refreshAround(Level level, BlockPos center) {
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            BlockPos p = center.offset(x, 0, z);
            if (level.getBlockState(p).getBlock() instanceof BloomeryBlock)
                updateForm(level, p);
        }
    }

    private void updateForm(Level level, BlockPos pos) {
        // Priority: 3x3 > 2x2 > 3x1 > single
        for (int dx = -2; dx <= 0; dx++) for (int dz = -2; dz <= 0; dz++) {
            BlockPos o = pos.offset(dx, 0, dz);
            if (canForm3x3(level, o)) { apply3x3(level, o); return; }
        }
        for (BlockPos o : new BlockPos[]{ pos, pos.west(), pos.north(), pos.north().west() }) {
            if (canForm2x2(level, o)) { apply2x2(level, o); return; }
        }
        for (int dz = -2; dz <= 0; dz++) {
            if (canForm3x1Z(level, pos.offset(0,0,dz))) { apply3x1Z(level, pos.offset(0,0,dz)); return; }
        }
        for (int dx = -2; dx <= 0; dx++) {
            if (canForm3x1X(level, pos.offset(dx,0,0))) { apply3x1X(level, pos.offset(dx,0,0)); return; }
        }
        clearSingle(level, pos);
    }

    // ── Priority system: higher-priority structures can override lower ones ──────

    private int getPriority(StructureType type) {
        return switch (type) {
            case BOWL_3X3 -> 3;
            case BOWL_2X2 -> 2;
            case LINE_3X1 -> 1;
            case SINGLE   -> 0;
        };
    }

    /**
     * A block can join a structure formation if:
     * - It is a BloomeryBlock, AND
     * - It is SINGLE (free), OR
     * - It is already correctly placed as expectedType+expectedPart, OR
     * - The requesting structure has HIGHER priority than the block's current structure
     */
    private boolean canJoin(Level level, BlockPos pos,
                            StructureType requestingType, BowlPart expectedPart) {
        BlockState st = level.getBlockState(pos);
        if (!(st.getBlock() instanceof BloomeryBlock)) return false;

        StructureType currentType = st.getValue(STRUCTURE);
        BowlPart currentPart = st.getValue(PART);

        // Free block
        if (currentType == StructureType.SINGLE) return true;

        // Already correctly placed
        if (currentType == requestingType && currentPart == expectedPart) return true;

        // Higher priority can override lower
        return getPriority(requestingType) > getPriority(currentType);
    }

    // ── Formation checks ─────────────────────────────────────────────────────────

    private boolean canForm3x3(Level level, BlockPos o) {
        return canJoin(level, o,                              StructureType.BOWL_3X3, BowlPart.NW)
                && canJoin(level, o.east(),                       StructureType.BOWL_3X3, BowlPart.N)
                && canJoin(level, o.east().east(),                StructureType.BOWL_3X3, BowlPart.NE)
                && canJoin(level, o.south(),                      StructureType.BOWL_3X3, BowlPart.W)
                && canJoin(level, o.south().east(),               StructureType.BOWL_3X3, BowlPart.C)
                && canJoin(level, o.south().east().east(),        StructureType.BOWL_3X3, BowlPart.E)
                && canJoin(level, o.south().south(),              StructureType.BOWL_3X3, BowlPart.SW)
                && canJoin(level, o.south().south().east(),       StructureType.BOWL_3X3, BowlPart.S)
                && canJoin(level, o.south().south().east().east(), StructureType.BOWL_3X3, BowlPart.SE);
    }

    private boolean canForm2x2(Level level, BlockPos o) {
        return canJoin(level, o,              StructureType.BOWL_2X2, BowlPart.NW)
                && canJoin(level, o.east(),       StructureType.BOWL_2X2, BowlPart.NE)
                && canJoin(level, o.south(),      StructureType.BOWL_2X2, BowlPart.SW)
                && canJoin(level, o.south().east(), StructureType.BOWL_2X2, BowlPart.SE);
    }

    private boolean canForm3x1Z(Level level, BlockPos o) {
        return canJoin(level, o,                  StructureType.LINE_3X1, BowlPart.Z_N)
                && canJoin(level, o.south(),          StructureType.LINE_3X1, BowlPart.Z_M)
                && canJoin(level, o.south().south(),  StructureType.LINE_3X1, BowlPart.Z_S);
    }

    private boolean canForm3x1X(Level level, BlockPos o) {
        return canJoin(level, o,                  StructureType.LINE_3X1, BowlPart.X_W)
                && canJoin(level, o.east(),           StructureType.LINE_3X1, BowlPart.X_M)
                && canJoin(level, o.east().east(),    StructureType.LINE_3X1, BowlPart.X_E);
    }

    // ── Formation apply ──────────────────────────────────────────────────────────

    private void apply3x3(Level level, BlockPos o) {
        setPart(level, o,                              StructureType.BOWL_3X3, BowlPart.NW);
        setPart(level, o.east(),                       StructureType.BOWL_3X3, BowlPart.N);
        setPart(level, o.east().east(),                StructureType.BOWL_3X3, BowlPart.NE);
        setPart(level, o.south(),                      StructureType.BOWL_3X3, BowlPart.W);
        setPart(level, o.south().east(),               StructureType.BOWL_3X3, BowlPart.C);
        setPart(level, o.south().east().east(),        StructureType.BOWL_3X3, BowlPart.E);
        setPart(level, o.south().south(),              StructureType.BOWL_3X3, BowlPart.SW);
        setPart(level, o.south().south().east(),       StructureType.BOWL_3X3, BowlPart.S);
        setPart(level, o.south().south().east().east(), StructureType.BOWL_3X3, BowlPart.SE);
    }

    private void apply2x2(Level level, BlockPos o) {
        setPart(level, o,              StructureType.BOWL_2X2, BowlPart.NW);
        setPart(level, o.east(),       StructureType.BOWL_2X2, BowlPart.NE);
        setPart(level, o.south(),      StructureType.BOWL_2X2, BowlPart.SW);
        setPart(level, o.south().east(), StructureType.BOWL_2X2, BowlPart.SE);
    }

    private void apply3x1Z(Level level, BlockPos o) {
        setPart(level, o,                  StructureType.LINE_3X1, BowlPart.Z_N);
        setPart(level, o.south(),          StructureType.LINE_3X1, BowlPart.Z_M);
        setPart(level, o.south().south(),  StructureType.LINE_3X1, BowlPart.Z_S);
    }

    private void apply3x1X(Level level, BlockPos o) {
        setPart(level, o,                  StructureType.LINE_3X1, BowlPart.X_W);
        setPart(level, o.east(),           StructureType.LINE_3X1, BowlPart.X_M);
        setPart(level, o.east().east(),    StructureType.LINE_3X1, BowlPart.X_E);
    }

    private void setPart(Level level, BlockPos pos, StructureType type, BowlPart part) {
        BlockState st = level.getBlockState(pos);
        if (!(st.getBlock() instanceof BloomeryBlock)) return;
        BlockState ns = st.setValue(STRUCTURE, type).setValue(PART, part);
        if (st != ns) level.setBlock(pos, ns, 3);
    }

    private void clearSingle(Level level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        if (!(st.getBlock() instanceof BloomeryBlock)) return;
        BlockState ns = st.setValue(STRUCTURE, StructureType.SINGLE).setValue(PART, BowlPart.NONE);
        if (st != ns) level.setBlock(pos, ns, 3);
    }

    // ── Shapes ───────────────────────────────────────────────────────────────────

    private VoxelShape getCurrentShape(BlockState state) {
        return switch (state.getValue(PART)) {
            case NW  -> NW_SHAPE;  case NE  -> NE_SHAPE;
            case SW  -> SW_SHAPE;  case SE  -> SE_SHAPE;
            case N   -> N_SHAPE;   case S   -> S_SHAPE;
            case W   -> W_SHAPE;   case E   -> E_SHAPE;
            case C   -> C_SHAPE;
            case Z_N -> Z_N_SHAPE; case Z_M -> Z_M_SHAPE; case Z_S -> Z_S_SHAPE;
            case X_W -> X_W_SHAPE; case X_M -> X_M_SHAPE; case X_E -> X_E_SHAPE;
            default  -> SINGLE_SHAPE;
        };
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return getCurrentShape(s); }
    @Override
    public VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return getCurrentShape(s); }

    // ── Enums ─────────────────────────────────────────────────────────────────────

    public enum StructureType implements StringRepresentable {
        SINGLE("single"), BOWL_2X2("bowl_2x2"), LINE_3X1("line_3x1"), BOWL_3X3("bowl_3x3");
        private final String name; StructureType(String n) { name=n; }
        @Override public String getSerializedName() { return name; }
    }

    public enum BowlPart implements StringRepresentable {
        NONE("none"),
        NW("nw"), NE("ne"), SW("sw"), SE("se"),
        N("n"), S("s"), W("w"), E("e"),
        C("c"),
        Z_N("z_n"), Z_M("z_m"), Z_S("z_s"),
        X_W("x_w"), X_M("x_m"), X_E("x_e");

        private final String name; BowlPart(String n) { name=n; }
        @Override public String getSerializedName() { return name; }
    }
}