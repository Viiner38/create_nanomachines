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

    // Fix 2 & 3: BURNING as blockstate so the lighting engine tracks it properly
    public static final BooleanProperty BURNING = BooleanProperty.create("burning");

    // ── Shapes ───────────────────────────────────────────────────────────────────

    private static final VoxelShape SINGLE_SHAPE = Shapes.or(
            Block.box(0, 2, 0, 16, 16, 2), Block.box(0, 2, 14, 16, 16, 16),
            Block.box(0, 2, 2, 2, 16, 14), Block.box(14, 2, 2, 16, 16, 14),
            Block.box(2, 0, 2, 14, 2, 14));
    private static final VoxelShape NW_SHAPE = Shapes.or(
            Block.box(0, 2, 0, 16, 16, 2), Block.box(0, 2, 2, 2, 16, 16),
            Block.box(2, 0, 2, 16, 2, 16));
    private static final VoxelShape NE_SHAPE = Shapes.or(
            Block.box(0, 2, 0, 16, 16, 2), Block.box(14, 2, 2, 16, 16, 16),
            Block.box(0, 0, 2, 14, 2, 16));
    private static final VoxelShape SW_SHAPE = Shapes.or(
            Block.box(0, 2, 14, 16, 16, 16), Block.box(0, 2, 0, 2, 16, 14),
            Block.box(2, 0, 0, 16, 2, 14));
    private static final VoxelShape SE_SHAPE = Shapes.or(
            Block.box(0, 2, 14, 16, 16, 16), Block.box(14, 2, 0, 16, 16, 14),
            Block.box(0, 0, 0, 14, 2, 14));

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

    // ── Fix 2 & 3: light from blockstate — engine propagates this automatically ──

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

        // Flint and steel → start burning
        if (held.is(Items.FLINT_AND_STEEL)) {
            if (be.canStartBurning()) {
                be.startBurning(level, pos);
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        if (be.isBurning()) return InteractionResult.PASS;

        // Charcoal → fill multiblock
        if (be.isCharcoal(held)) {
            ItemStack rem = held.copy();
            for (BloomeryBlockEntity member : be.getMultiblockGroup()) {
                if (rem.isEmpty()) break;
                rem = member.tryInsert(rem, false);
            }
            int inserted = held.getCount() - rem.getCount();
            if (inserted > 0) {
                player.getItemInHand(hand).shrink(inserted);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        // Iron → into this block's iron slot
        if (be.isValidIron(held)) {
            ItemStack rem = be.tryInsertIron(held, false);
            if (rem.getCount() < held.getCount()) {
                player.setItemInHand(hand, rem.isEmpty() ? ItemStack.EMPTY : rem);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        // Empty hand → extract steel then charcoal
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

    // ── Item drop absorption ─────────────────────────────────────────────────────

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide) return;
        if (entity instanceof ItemEntity itemEntity && !itemEntity.getItem().isEmpty()) {
            if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be) {
                ItemStack item = itemEntity.getItem();
                if (be.isCharcoal(item) || be.isValidIron(item))
                    be.absorbDroppedItem(itemEntity);
            }
        }
    }

    // ── Break drops ──────────────────────────────────────────────────────────────

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be) {
                int amount = be.getCharcoalAmount();
                while (amount > 0) {
                    int drop = Math.min(amount, 64);
                    popResource(level, pos, new ItemStack(Items.CHARCOAL, drop));
                    amount -= drop;
                }
                if (!be.getIronItem().isEmpty())    popResource(level, pos, be.getIronItem());
                if (!be.getSteelOutput().isEmpty()) popResource(level, pos, be.getSteelOutput());
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
            if (!level.isClientSide) scheduleRefresh(level, pos);
        } else {
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    // ── Multiblock logic ─────────────────────────────────────────────────────────

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
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            BlockPos p = center.offset(x, 0, z);
            if (level.getBlockState(p).getBlock() instanceof BloomeryBlock)
                level.scheduleTick(p, this, 1);
        }
    }

    private void refreshAround(Level level, BlockPos center) {
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            BlockPos p = center.offset(x, 0, z);
            if (level.getBlockState(p).getBlock() instanceof BloomeryBlock)
                updateForm(level, p);
        }
    }

    private void updateForm(Level level, BlockPos pos) {
        BlockPos[] origins = { pos, pos.west(), pos.north(), pos.north().west() };
        for (BlockPos o : origins) { if (canForm2x2(level, o)) { apply2x2(level, o); return; } }
        clearSingle(level, pos);
    }

    private boolean canForm2x2(Level level, BlockPos origin) {
        return canJoin2x2(level, origin, BowlPart.NW)
                && canJoin2x2(level, origin.east(), BowlPart.NE)
                && canJoin2x2(level, origin.south(), BowlPart.SW)
                && canJoin2x2(level, origin.south().east(), BowlPart.SE);
    }

    private boolean canJoin2x2(Level level, BlockPos pos, BowlPart expectedPart) {
        BlockState st = level.getBlockState(pos);
        if (!(st.getBlock() instanceof BloomeryBlock)) return false;
        StructureType s = st.getValue(STRUCTURE);
        BowlPart p = st.getValue(PART);
        if (s == StructureType.SINGLE) return true;
        return s == StructureType.BOWL_2X2 && p == expectedPart;
    }

    private void apply2x2(Level level, BlockPos origin) {
        setPart(level, origin, BowlPart.NW);
        setPart(level, origin.east(), BowlPart.NE);
        setPart(level, origin.south(), BowlPart.SW);
        setPart(level, origin.south().east(), BowlPart.SE);
    }

    private void setPart(Level level, BlockPos pos, BowlPart part) {
        BlockState st = level.getBlockState(pos);
        if (!(st.getBlock() instanceof BloomeryBlock)) return;
        // Preserve BURNING state when updating structure/part
        BlockState ns = st.setValue(STRUCTURE, StructureType.BOWL_2X2).setValue(PART, part);
        if (st != ns) level.setBlock(pos, ns, 3);
    }

    private void clearSingle(Level level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        if (!(st.getBlock() instanceof BloomeryBlock)) return;
        // Preserve BURNING state when clearing
        BlockState ns = st.setValue(STRUCTURE, StructureType.SINGLE).setValue(PART, BowlPart.NONE);
        if (st != ns) level.setBlock(pos, ns, 3);
    }

    /**
     * Called by BloomeryBlockEntity to update the BURNING blockstate flag.
     * This is what makes the lighting engine propagate light correctly.
     */
    public static void setBurningState(Level level, BlockPos pos, boolean burning) {
        BlockState st = level.getBlockState(pos);
        if (st.getBlock() instanceof BloomeryBlock) {
            level.setBlock(pos, st.setValue(BURNING, burning), 3);
        }
    }

    // ── Shapes ───────────────────────────────────────────────────────────────────

    private VoxelShape getCurrentShape(BlockState state) {
        if (state.getValue(STRUCTURE) != StructureType.BOWL_2X2) return SINGLE_SHAPE;
        return switch (state.getValue(PART)) {
            case NW -> NW_SHAPE; case NE -> NE_SHAPE;
            case SW -> SW_SHAPE; case SE -> SE_SHAPE;
            default -> SINGLE_SHAPE;
        };
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return getCurrentShape(s); }
    @Override
    public VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return getCurrentShape(s); }

    // ── Enums ─────────────────────────────────────────────────────────────────────

    public enum StructureType implements StringRepresentable {
        SINGLE("single"), BOWL_2X2("bowl_2x2"), LINE_3X1("line_3x1"), BOWL_3X3("bowl_3x3");
        private final String name; StructureType(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public enum BowlPart implements StringRepresentable {
        NONE("none"), NW("nw"), NE("ne"), SW("sw"), SE("se");
        private final String name; BowlPart(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
}