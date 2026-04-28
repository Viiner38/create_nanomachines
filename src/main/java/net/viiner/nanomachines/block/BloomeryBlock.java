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

    public static final EnumProperty<StructureType> STRUCTURE = EnumProperty.create("structure", StructureType.class);
    public static final EnumProperty<BowlPart> PART = EnumProperty.create("part", BowlPart.class);
    public static final BooleanProperty BURNING = BooleanProperty.create("burning");

    private static final VoxelShape[] SHAPES = new VoxelShape[16];
    static {
        for (int i = 0; i < 16; i++) {
            boolean n = (i & 1) != 0, s = (i & 2) != 0, w = (i & 4) != 0, e = (i & 8) != 0;
            int minX = w ? 2 : 0, maxX = e ? 14 : 16;
            int minZ = n ? 2 : 0, maxZ = s ? 14 : 16;
            VoxelShape shape = Block.box(minX, 0, minZ, maxX, 2, maxZ);
            if (n) shape = Shapes.or(shape, Block.box(0, 2, 0, 16, 16, 2));
            if (s) shape = Shapes.or(shape, Block.box(0, 2, 14, 16, 16, 16));
            if (w) shape = Shapes.or(shape, Block.box(0, 2, minZ, 2, 16, maxZ));
            if (e) shape = Shapes.or(shape, Block.box(14, 2, minZ, 16, 16, maxZ));
            SHAPES[i] = shape.optimize();
        }
    }

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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.BLOOMERY.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.BLOOMERY.get()) return null;
        return (lvl, pos, st, be) -> ((BloomeryBlockEntity) be).tick();
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(BURNING) ? 15 : 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof BloomeryBlockEntity be)) return InteractionResult.PASS;

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
            if (!steel.isEmpty()) {
                player.getInventory().add(steel);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            List<BloomeryBlockEntity> group = be.getMultiblockGroup();
            int total = group.stream().mapToInt(BloomeryBlockEntity::getCharcoalAmount).sum();
            int toExtract = player.isShiftKeyDown() ? total : 1;
            ItemStack charcoal = be.tryExtractMultiblock(toExtract, false);
            if (!charcoal.isEmpty()) {
                player.getInventory().add(charcoal);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide) return;
        if (entity instanceof ItemEntity itemEntity && !itemEntity.getItem().isEmpty()) {
            if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be) {
                if (be.isCharcoal(itemEntity.getItem()) || be.isValidIron(itemEntity.getItem())) {
                    be.absorbDroppedItem(itemEntity);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be) {
                int amount = be.getCharcoalAmount();
                while (amount > 0) {
                    int drop = Math.min(amount, 64);
                    popResource(level, pos, new ItemStack(Items.CHARCOAL, drop));
                    amount -= drop;
                }
                if (!be.getIronItem().isEmpty()) popResource(level, pos, be.getIronItem());
                if (!be.getSteelOutput().isEmpty()) popResource(level, pos, be.getSteelOutput());
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
            if (!level.isClientSide) scheduleRefresh(level, pos);
        } else {
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getBlock() != oldState.getBlock()) scheduleRefresh(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) scheduleRefresh(level, pos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refreshAround(level, pos);
    }

    private void scheduleRefresh(Level level, BlockPos center) {
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            BlockPos p = center.offset(x, 0, z);
            if (level.getBlockState(p).getBlock() instanceof BloomeryBlock) level.scheduleTick(p, this, 1);
        }
    }

    private void refreshAround(Level level, BlockPos center) {
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            BlockPos p = center.offset(x, 0, z);
            if (level.getBlockState(p).getBlock() instanceof BloomeryBlock) updateForm(level, p);
        }
    }

    private void updateForm(Level level, BlockPos pos) {
        // Prioriteet 1: Suurim võimalik ahi (3x3)
        for (int dx = 0; dx >= -2; dx--) for (int dz = 0; dz >= -2; dz--) {
            BlockPos o = pos.offset(dx, 0, dz);
            if (canForm3x3(level, o)) { apply3x3(level, o); return; }
        }
        // Prioriteet 2: Suuruselt teine ahi (2x2)
        for (int dx = 0; dx >= -1; dx--) for (int dz = 0; dz >= -1; dz--) {
            BlockPos o = pos.offset(dx, 0, dz);
            if (canForm2x2(level, o)) { apply2x2(level, o); return; }
        }
        // Prioriteet 3: Kolmas suurus (3x1 X-teljel)
        for (int dx = 0; dx >= -2; dx--) {
            BlockPos o = pos.offset(dx, 0, 0);
            if (canForm3x1X(level, o)) { apply3x1X(level, o); return; }
        }
        // Prioriteet 4: Kolmas suurus (3x1 Z-teljel)
        for (int dz = 0; dz >= -2; dz--) {
            BlockPos o = pos.offset(0, 0, dz);
            if (canForm3x1Z(level, o)) { apply3x1Z(level, o); return; }
        }
        // Kui ükski ei sobi, muudame tagasi üksikuks plokiks
        clearSingle(level, pos);
    }

    private int getStructureSize(StructureType st) {
        return switch (st) {
            case BOWL_3X3 -> 9;
            case BOWL_2X2 -> 4;
            case LINE_3X1_X, LINE_3X1_Z -> 3;
            case SINGLE -> 1;
        };
    }

    private boolean canJoin(Level level, BlockPos pos, StructureType st, BowlPart pt) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BloomeryBlock)) return false;
        if (state.getValue(BURNING)) return false; // Põlevat ahju ei tohi lõhkuda

        StructureType currentSt = state.getValue(STRUCTURE);
        if (currentSt == StructureType.SINGLE) return true;
        if (currentSt == st && state.getValue(PART) == pt) return true;

        // VÕTI UPGRADE'IMISEKS: Kui uus struktuur on SUUREM kui vana, neelab ta vana alla!
        return getStructureSize(st) > getStructureSize(currentSt);
    }

    private boolean canForm3x3(Level level, BlockPos o) {
        return canJoin(level, o, StructureType.BOWL_3X3, BowlPart.NW) && canJoin(level, o.east(), StructureType.BOWL_3X3, BowlPart.N) && canJoin(level, o.east(2), StructureType.BOWL_3X3, BowlPart.NE) &&
                canJoin(level, o.south(), StructureType.BOWL_3X3, BowlPart.W) && canJoin(level, o.south().east(), StructureType.BOWL_3X3, BowlPart.C) && canJoin(level, o.south().east(2), StructureType.BOWL_3X3, BowlPart.E) &&
                canJoin(level, o.south(2), StructureType.BOWL_3X3, BowlPart.SW) && canJoin(level, o.south(2).east(), StructureType.BOWL_3X3, BowlPart.S) && canJoin(level, o.south(2).east(2), StructureType.BOWL_3X3, BowlPart.SE);
    }
    private boolean canForm3x1X(Level level, BlockPos o) {
        return canJoin(level, o, StructureType.LINE_3X1_X, BowlPart.W) && canJoin(level, o.east(), StructureType.LINE_3X1_X, BowlPart.C) && canJoin(level, o.east(2), StructureType.LINE_3X1_X, BowlPart.E);
    }
    private boolean canForm3x1Z(Level level, BlockPos o) {
        return canJoin(level, o, StructureType.LINE_3X1_Z, BowlPart.N) && canJoin(level, o.south(), StructureType.LINE_3X1_Z, BowlPart.C) && canJoin(level, o.south(2), StructureType.LINE_3X1_Z, BowlPart.S);
    }
    private boolean canForm2x2(Level level, BlockPos o) {
        return canJoin(level, o, StructureType.BOWL_2X2, BowlPart.NW) && canJoin(level, o.east(), StructureType.BOWL_2X2, BowlPart.NE) &&
                canJoin(level, o.south(), StructureType.BOWL_2X2, BowlPart.SW) && canJoin(level, o.south().east(), StructureType.BOWL_2X2, BowlPart.SE);
    }

    private void apply3x3(Level level, BlockPos o) {
        setPart(level, o, StructureType.BOWL_3X3, BowlPart.NW); setPart(level, o.east(), StructureType.BOWL_3X3, BowlPart.N); setPart(level, o.east(2), StructureType.BOWL_3X3, BowlPart.NE);
        setPart(level, o.south(), StructureType.BOWL_3X3, BowlPart.W); setPart(level, o.south().east(), StructureType.BOWL_3X3, BowlPart.C); setPart(level, o.south().east(2), StructureType.BOWL_3X3, BowlPart.E);
        setPart(level, o.south(2), StructureType.BOWL_3X3, BowlPart.SW); setPart(level, o.south(2).east(), StructureType.BOWL_3X3, BowlPart.S); setPart(level, o.south(2).east(2), StructureType.BOWL_3X3, BowlPart.SE);
    }
    private void apply3x1X(Level level, BlockPos o) {
        setPart(level, o, StructureType.LINE_3X1_X, BowlPart.W); setPart(level, o.east(), StructureType.LINE_3X1_X, BowlPart.C); setPart(level, o.east(2), StructureType.LINE_3X1_X, BowlPart.E);
    }
    private void apply3x1Z(Level level, BlockPos o) {
        setPart(level, o, StructureType.LINE_3X1_Z, BowlPart.N); setPart(level, o.south(), StructureType.LINE_3X1_Z, BowlPart.C); setPart(level, o.south(2), StructureType.LINE_3X1_Z, BowlPart.S);
    }
    private void apply2x2(Level level, BlockPos o) {
        setPart(level, o, StructureType.BOWL_2X2, BowlPart.NW); setPart(level, o.east(), StructureType.BOWL_2X2, BowlPart.NE);
        setPart(level, o.south(), StructureType.BOWL_2X2, BowlPart.SW); setPart(level, o.south().east(), StructureType.BOWL_2X2, BowlPart.SE);
    }

    private void setPart(Level level, BlockPos pos, StructureType st, BowlPart part) {
        BlockState stState = level.getBlockState(pos);
        if (stState.getBlock() instanceof BloomeryBlock) {
            BlockState ns = stState.setValue(STRUCTURE, st).setValue(PART, part);
            if (stState != ns) level.setBlock(pos, ns, 3);
        }
    }

    private void clearSingle(Level level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        if (st.getBlock() instanceof BloomeryBlock) {
            BlockState ns = st.setValue(STRUCTURE, StructureType.SINGLE).setValue(PART, BowlPart.NONE);
            if (st != ns) level.setBlock(pos, ns, 3);
        }
    }

    public static void setBurningState(Level level, BlockPos pos, boolean burning) {
        BlockState st = level.getBlockState(pos);
        if (st.getBlock() instanceof BloomeryBlock) {
            level.setBlock(pos, st.setValue(BURNING, burning), 3);
        }
    }

    public static boolean[] getWalls(StructureType st, BowlPart pt) {
        if (st == StructureType.SINGLE) return new boolean[]{true, true, true, true};
        if (st == StructureType.BOWL_2X2) return new boolean[]{ pt==BowlPart.NW||pt==BowlPart.NE, pt==BowlPart.SW||pt==BowlPart.SE, pt==BowlPart.NW||pt==BowlPart.SW, pt==BowlPart.NE||pt==BowlPart.SE };
        if (st == StructureType.LINE_3X1_X) return new boolean[]{ true, true, pt==BowlPart.W, pt==BowlPart.E };
        if (st == StructureType.LINE_3X1_Z) return new boolean[]{ pt==BowlPart.N, pt==BowlPart.S, true, true };
        if (st == StructureType.BOWL_3X3) return new boolean[]{
                pt==BowlPart.NW||pt==BowlPart.N||pt==BowlPart.NE, pt==BowlPart.SW||pt==BowlPart.S||pt==BowlPart.SE,
                pt==BowlPart.NW||pt==BowlPart.W||pt==BowlPart.SW, pt==BowlPart.NE||pt==BowlPart.E||pt==BowlPart.SE
        };
        return new boolean[]{true, true, true, true};
    }

    private VoxelShape getCurrentShape(BlockState state) {
        boolean[] walls = getWalls(state.getValue(STRUCTURE), state.getValue(PART));
        int index = (walls[0]?1:0) | (walls[1]?2:0) | (walls[2]?4:0) | (walls[3]?8:0);
        return SHAPES[index];
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return getCurrentShape(s); }
    @Override
    public VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return getCurrentShape(s); }

    public enum StructureType implements StringRepresentable {
        SINGLE("single"), BOWL_2X2("bowl_2x2"), LINE_3X1_X("line_3x1_x"), LINE_3X1_Z("line_3x1_z"), BOWL_3X3("bowl_3x3");
        private final String name; StructureType(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public enum BowlPart implements StringRepresentable {
        NONE("none"), NW("nw"), N("n"), NE("ne"), W("w"), C("c"), E("e"), SW("sw"), S("s"), SE("se");
        private final String name; BowlPart(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
}