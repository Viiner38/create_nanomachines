package net.viiner.nanomachines.block;

import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.viiner.nanomachines.item.ModItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BloomeryBlockEntity extends SmartBlockEntity {

    // ── Constants ────────────────────────────────────────────────────────────────

    public static final int MAX_PER_BLOCK = 16;
    public static final int BURN_DURATION = 6000;  // 5 min


    private static final TagKey<Item> CRUSHED_IRON =
            ItemTags.create(new ResourceLocation("create", "crushed_raw_iron"));
    private static final TagKey<Item> IRON_SHEET =
            ItemTags.create(new ResourceLocation("create", "iron_sheet"));

    // ── Per-block state ──────────────────────────────────────────────────────────

    private int       charcoalAmount = 0;
    private ItemStack ironItem       = ItemStack.EMPTY;
    private ItemStack steelOutput    = ItemStack.EMPTY;
    private boolean   burning        = false;
    private int       burnTick       = 0;

    // Client smooth counter — synced from server via NBT packet
    float clientBurnProgress = 0f;
    private int syncTimer = 0;

    private final BloomeryItemHandler itemHandler = new BloomeryItemHandler();
    private final LazyOptional<IItemHandler> handlerOpt = LazyOptional.of(() -> itemHandler);

    // ── Constructor ──────────────────────────────────────────────────────────────

    public BloomeryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ── Tick ─────────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;

        // Fix 6: fire damage to players inside burning bloomery
        if (!level.isClientSide && burning) {
            level.getEntitiesOfClass(Player.class, new AABB(worldPosition)).forEach(player -> {
                if (!player.fireImmune() && burnTick % 20 == 0) {
                    player.hurt(level.damageSources().inFire(), 1.0f);
                    player.setRemainingFireTicks(40);
                }
            });
        }

        if (level.isClientSide) {
            if (burning) clientBurnProgress = Math.min(clientBurnProgress + 1f, BURN_DURATION);
            return;
        }

        if (!burning) return;

        burnTick++;
        if (++syncTimer >= 4) { syncTimer = 0; sendData(); }
        if (burnTick >= BURN_DURATION) finishBurning();
    }

    // ── Burning ──────────────────────────────────────────────────────────────────

    public boolean canStartBurning() {
        if (burning) return false;
        List<BloomeryBlockEntity> group = getMultiblockGroup();
        int ironCount     = 0;
        int charcoalTotal = 0;
        for (BloomeryBlockEntity be : group) {
            if (!be.ironItem.isEmpty()) ironCount++;
            charcoalTotal += be.charcoalAmount;
        }
        return ironCount > 0 && charcoalTotal >= ironCount * 16;
    }

    /**
     * Fix 2 & 3: startBurning now updates the BURNING blockstate on every block,
     * so the lighting engine propagates light to surrounding blocks immediately.
     */
    public void startBurning(Level level, BlockPos clickedPos) {
        for (BloomeryBlockEntity be : getMultiblockGroup()) {
            be.burning  = true;
            be.burnTick = 0;
            be.setChanged();
            be.sendData();
            // Update blockstate → lighting engine picks up the change
            BloomeryBlock.setBurningState(level, be.worldPosition, true);
        }
    }

    private void finishBurning() {
        burning        = false;
        burnTick       = 0;
        charcoalAmount = 0;

        if (!ironItem.isEmpty()) {
            // TODO: Replace with your steel ingot:
            //   steelOutput = new ItemStack(ModItems.STEEL_INGOT.get());
            steelOutput = new ItemStack(ModItems.NANOMACHINES_STEEL.get()); // PLACEHOLDER
            ironItem    = ItemStack.EMPTY;
        }

        setChanged();
        sendData();

        // Fix 2: clear BURNING blockstate so light goes off immediately
        if (level != null) {
            BloomeryBlock.setBurningState(level, worldPosition, false);
        }
    }

    // ── Charcoal ─────────────────────────────────────────────────────────────────

    public ItemStack tryInsert(ItemStack stack, boolean simulate) {
        if (burning || !isCharcoal(stack)) return stack;
        int space = MAX_PER_BLOCK - charcoalAmount;
        if (space <= 0) return stack;
        int toInsert = Math.min(stack.getCount(), space);
        if (!simulate) { charcoalAmount += toInsert; setChanged(); sendData(); }
        if (toInsert >= stack.getCount()) return ItemStack.EMPTY;
        return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - toInsert);
    }

    public ItemStack tryExtractLocal(int amount, boolean simulate) {
        if (burning || charcoalAmount == 0) return ItemStack.EMPTY;
        int extracted = Math.min(amount, charcoalAmount);
        if (!simulate) { charcoalAmount -= extracted; setChanged(); sendData(); }
        return new ItemStack(Items.CHARCOAL, extracted);
    }

    public ItemStack tryExtractMultiblock(int amount, boolean simulate) {
        if (burning) return ItemStack.EMPTY;
        List<BloomeryBlockEntity> group = getMultiblockGroup();
        int remaining = amount;
        for (BloomeryBlockEntity be : group) {
            if (remaining <= 0) break;
            remaining -= be.tryExtractLocal(remaining, true).getCount();
        }
        int total = amount - remaining;
        if (total == 0) return ItemStack.EMPTY;
        if (!simulate) {
            int left = total;
            for (BloomeryBlockEntity be : group) {
                if (left <= 0) break;
                left -= be.tryExtractLocal(left, false).getCount();
            }
        }
        return new ItemStack(Items.CHARCOAL, total);
    }

    // ── Iron ─────────────────────────────────────────────────────────────────────

    /** Insert iron into THIS block only. Requires charcoal >= 16 in this block. */
    public ItemStack tryInsertIron(ItemStack stack, boolean simulate) {
        if (burning || !isValidIron(stack) || !ironItem.isEmpty()) return stack;
        if (charcoalAmount < 16) return stack;
        if (!simulate) {
            ironItem = ItemHandlerHelper.copyStackWithSize(stack, 1);
            setChanged(); sendData();
        }
        if (stack.getCount() > 1)
            return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
        return ItemStack.EMPTY;
    }

    public ItemStack tryExtractIron(boolean simulate) {
        if (burning || ironItem.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = ironItem.copy();
        if (!simulate) { ironItem = ItemStack.EMPTY; setChanged(); sendData(); }
        return result;
    }

    // ── Steel — unified across multiblock ────────────────────────────────────────

    public ItemStack tryExtractSteelMultiblock(int amount, boolean simulate) {
        List<BloomeryBlockEntity> group = getMultiblockGroup();
        // Count how many we can extract
        int canExtract = 0;
        Item steelItem = Items.IRON_INGOT;
        for (BloomeryBlockEntity be : group) {
            if (canExtract >= amount) break;
            if (!be.steelOutput.isEmpty()) {
                steelItem = be.steelOutput.getItem();
                canExtract++;
            }
        }
        if (canExtract == 0) return ItemStack.EMPTY;
        if (!simulate) {
            int left = canExtract;
            for (BloomeryBlockEntity be : group) {
                if (left <= 0) break;
                if (!be.steelOutput.isEmpty()) { be.steelOutput = ItemStack.EMPTY; be.setChanged(); be.sendData(); left--; }
            }
        }
        return new ItemStack(steelItem, canExtract);
    }

    public boolean    hasSteelOutput()  { return getMultiblockGroup().stream().anyMatch(be -> !be.steelOutput.isEmpty()); }
    public ItemStack  getSteelOutput()  { return steelOutput; }
    public ItemStack  getIronItem()     { return ironItem; }

    // ── Item predicates ──────────────────────────────────────────────────────────

    public boolean isCharcoal(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.CHARCOAL);
    }

    public boolean isValidIron(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.IRON_INGOT)
                || stack.is(Items.RAW_IRON)
                || stack.is(CRUSHED_IRON)
                || stack.is(IRON_SHEET);
    }

    // ── Item drop absorption ─────────────────────────────────────────────────────

    public void absorbDroppedItem(ItemEntity itemEntity) {
        if (level == null || level.isClientSide || burning) return;
        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.isEmpty()) return;

        if (isCharcoal(itemStack)) {
            for (BloomeryBlockEntity be : getMultiblockGroup()) {
                if (itemStack.isEmpty()) break;
                int space = MAX_PER_BLOCK - be.charcoalAmount;
                if (space <= 0) continue;
                int toInsert = Math.min(itemStack.getCount(), space);
                be.charcoalAmount += toInsert;
                be.setChanged(); be.sendData();
                itemStack.shrink(toInsert);
            }
            if (itemStack.isEmpty()) itemEntity.discard();

        } else if (isValidIron(itemStack)) {
            // Fix 4: distribute iron across ALL group blocks, not just this one
            for (BloomeryBlockEntity be : getMultiblockGroup()) {
                if (itemStack.isEmpty()) break;
                if (!be.ironItem.isEmpty()) continue;        // slot taken
                if (be.charcoalAmount < 16) continue;       // not enough charcoal
                be.ironItem = ItemHandlerHelper.copyStackWithSize(itemStack, 1);
                be.setChanged(); be.sendData();
                itemStack.shrink(1);
            }
            if (itemStack.isEmpty()) itemEntity.discard();
        }
    }

    // ── Multiblock group ─────────────────────────────────────────────────────────

    public List<BloomeryBlockEntity> getMultiblockGroup() {
        if (level == null) return List.of(this);
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof BloomeryBlock)) return List.of(this);
        if (state.getValue(BloomeryBlock.STRUCTURE) != BloomeryBlock.StructureType.BOWL_2X2)
            return List.of(this);

        BlockPos nw = switch (state.getValue(BloomeryBlock.PART)) {
            case NW -> worldPosition;
            case NE -> worldPosition.west();
            case SW -> worldPosition.north();
            case SE -> worldPosition.north().west();
            default -> worldPosition;
        };

        List<BloomeryBlockEntity> group = new ArrayList<>();
        for (BlockPos p : new BlockPos[]{ nw, nw.east(), nw.south(), nw.south().east() })
            if (level.getBlockEntity(p) instanceof BloomeryBlockEntity be) group.add(be);
        return group.isEmpty() ? List.of(this) : group;
    }

    // ── Renderer helpers ─────────────────────────────────────────────────────────

    public float getFillRatio() {
        List<BloomeryBlockEntity> group = getMultiblockGroup();
        int total = group.stream().mapToInt(be -> be.charcoalAmount).sum();
        int max   = group.size() * MAX_PER_BLOCK;
        return max == 0 ? 0f : (float) total / max;
    }

    public float getSteelRatio() {
        List<BloomeryBlockEntity> group = getMultiblockGroup();
        long withSteel = group.stream().filter(be -> !be.steelOutput.isEmpty()).count();
        return group.isEmpty() ? 0f : (float) withSteel / group.size();
    }

    public float getClientBurnProgress(float partialTick) {
        return Math.min(clientBurnProgress + (burning ? partialTick : 0f), BURN_DURATION);
    }

    public boolean isBurning()         { return burning;        }
    public int     getCharcoalAmount() { return charcoalAmount; }
    public int     getMaxCharcoal()    { return MAX_PER_BLOCK;  }

    // ── Create behaviours ────────────────────────────────────────────────────────

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this) {
            @Override
            public boolean canInsertFromSide(Direction side) { return true; }

            @Override
            public ItemStack handleInsertion(ItemStack stack, Direction side, boolean simulate) {
                if (burning) return stack;
                if (isCharcoal(stack)) {
                    ItemStack rem = stack.copy();
                    for (BloomeryBlockEntity be : getMultiblockGroup()) {
                        if (rem.isEmpty()) break;
                        rem = be.tryInsert(rem, simulate);
                    }
                    return rem;
                }
                // Fix 4: arm/belt distributes iron across group too
                if (isValidIron(stack)) {
                    ItemStack rem = stack.copy();
                    for (BloomeryBlockEntity be : getMultiblockGroup()) {
                        if (rem.isEmpty()) break;
                        rem = be.tryInsertIron(rem, simulate);
                    }
                    return rem;
                }
                return stack;
            }
        });
    }

    // ── NBT ──────────────────────────────────────────────────────────────────────

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        charcoalAmount = tag.getInt("Charcoal");
        burning        = tag.getBoolean("Burning");
        burnTick       = tag.getInt("BurnTick");
        ironItem       = tag.contains("Iron")  ? ItemStack.of(tag.getCompound("Iron"))  : ItemStack.EMPTY;
        steelOutput    = tag.contains("Steel") ? ItemStack.of(tag.getCompound("Steel")) : ItemStack.EMPTY;
        if (clientPacket) clientBurnProgress = burnTick;
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("Charcoal",    charcoalAmount);
        tag.putBoolean("Burning", burning);
        tag.putInt("BurnTick",    burnTick);
        if (!ironItem.isEmpty())    tag.put("Iron",  ironItem.save(new CompoundTag()));
        if (!steelOutput.isEmpty()) tag.put("Steel", steelOutput.save(new CompoundTag()));
    }

    // ── Capability ───────────────────────────────────────────────────────────────

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return handlerOpt.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); handlerOpt.invalidate(); }

    // ── IItemHandler — slot 0=charcoal, 1=iron, 2=steel ─────────────────────────

    private class BloomeryItemHandler implements IItemHandler {
        @Override public int getSlots() { return 3; }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return switch (slot) {
                case 0 -> {
                    List<BloomeryBlockEntity> g = getMultiblockGroup();
                    int total = g.stream().mapToInt(be -> be.charcoalAmount).sum();
                    yield total > 0 ? new ItemStack(Items.CHARCOAL, total) : ItemStack.EMPTY;
                }
                case 1 -> ironItem.isEmpty() ? ItemStack.EMPTY : ironItem.copy();
                case 2 -> {
                    List<BloomeryBlockEntity> g = getMultiblockGroup();
                    long count = g.stream().filter(be -> !be.steelOutput.isEmpty()).count();
                    if (count == 0) yield ItemStack.EMPTY;
                    Item steel = g.stream().filter(be -> !be.steelOutput.isEmpty())
                            .findFirst().map(be -> be.steelOutput.getItem()).orElse(Items.IRON_INGOT);
                    yield new ItemStack(steel, (int) count);
                }
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (burning) return stack;
            return switch (slot) {
                case 0 -> {
                    if (!isCharcoal(stack)) yield stack;
                    ItemStack rem = stack.copy();
                    for (BloomeryBlockEntity be : getMultiblockGroup()) {
                        if (rem.isEmpty()) break;
                        rem = be.tryInsert(rem, simulate);
                    }
                    yield rem;
                }
                case 1 -> {
                    // Fix 4: distribute iron across group
                    ItemStack rem = stack.copy();
                    for (BloomeryBlockEntity be : getMultiblockGroup()) {
                        if (rem.isEmpty()) break;
                        rem = be.tryInsertIron(rem, simulate);
                    }
                    yield rem;
                }
                default -> stack;
            };
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (burning) return ItemStack.EMPTY;
            return switch (slot) {
                case 0 -> tryExtractMultiblock(amount, simulate);
                case 1 -> tryExtractIron(simulate);
                case 2 -> tryExtractSteelMultiblock(amount, simulate);
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public int getSlotLimit(int slot) {
            return switch (slot) {
                case 0 -> getMultiblockGroup().size() * MAX_PER_BLOCK;
                case 1 -> 1;
                case 2 -> getMultiblockGroup().size();
                default -> 0;
            };
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (burning) return false;
            return switch (slot) {
                case 0 -> isCharcoal(stack);
                case 1 -> isValidIron(stack) && charcoalAmount >= 16;
                default -> false;
            };
        }
    }
}