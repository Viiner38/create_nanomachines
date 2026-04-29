package net.viiner.nanomachines.block;

import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    public static final int MAX_PER_BLOCK = 16;
    public static final int BURN_DURATION = 600;
    public static final int FADE_START    = BURN_DURATION - 20;

    private static final TagKey<Item> CRUSHED_IRON_TAG =
            ItemTags.create(new ResourceLocation("create", "crushed_raw_ores/iron"));
    private static final TagKey<Item> IRON_SHEET_TAG =
            ItemTags.create(new ResourceLocation("create", "iron_sheets"));

    private int       charcoalAmount = 0;
    private ItemStack ironItem       = ItemStack.EMPTY;
    private ItemStack steelOutput    = ItemStack.EMPTY;
    private boolean   burning        = false;
    private int       burnTick       = 0;

    float clientBurnProgress = 0f;
    private int syncTimer = 0;

    private final BloomeryItemHandler itemHandler = new BloomeryItemHandler();
    private final LazyOptional<IItemHandler> handlerOpt = LazyOptional.of(() -> itemHandler);

    public BloomeryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ── Tick ─────────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;

        if (level.isClientSide) {
            if (burning) clientBurnProgress = Math.min(clientBurnProgress + 1f, BURN_DURATION);
            return;
        }

        if (!burning) return;

        if (burnTick % 20 == 0) {
            level.getEntitiesOfClass(Player.class, new AABB(worldPosition)).forEach(player -> {
                if (!player.fireImmune()) {
                    player.hurt(level.damageSources().inFire(), 1.0f);
                    player.setRemainingFireTicks(40);
                }
            });
        }

        if (burnTick % 80 == 1) {
            level.playSound(null, worldPosition,
                    SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    0.5f + level.random.nextFloat() * 0.5f,
                    0.8f + level.random.nextFloat() * 0.4f);
        }

        burnTick++;
        if (++syncTimer >= 4) { syncTimer = 0; sendData(); }
        if (burnTick >= BURN_DURATION) finishBurning();
    }

    // ── Burning ──────────────────────────────────────────────────────────────────

    public boolean canStartBurning() {
        if (burning) return false;
        List<BloomeryBlockEntity> group = getMultiblockGroup();
        int iron = 0, coal = 0;
        for (BloomeryBlockEntity be : group) {
            if (!be.ironItem.isEmpty()) iron++;
            coal += be.charcoalAmount;
        }
        int maxCoal = group.size() * MAX_PER_BLOCK;
        return iron > 0 && coal >= iron * 16 && coal >= maxCoal;
    }

    public void startBurning(Level level, BlockPos clickedPos) {
        for (BloomeryBlockEntity be : getMultiblockGroup()) {
            be.burning  = true;
            be.burnTick = 0;
            be.setChanged(); be.sendData();
            BloomeryBlock.setBurningState(level, be.worldPosition, true);
        }
    }

    private void finishBurning() {
        burning = false; burnTick = 0; charcoalAmount = 0;
        if (!ironItem.isEmpty()) {
            steelOutput = new ItemStack(ModItems.NANOMACHINES_STEEL.get());
            ironItem    = ItemStack.EMPTY;
        }
        setChanged(); sendData();
        if (level != null) BloomeryBlock.setBurningState(level, worldPosition, false);
    }

    // ── Charcoal ─────────────────────────────────────────────────────────────────

    public ItemStack tryInsert(ItemStack stack, boolean simulate) {
        if (burning || !isCharcoal(stack)) return stack;
        int space = MAX_PER_BLOCK - charcoalAmount;
        if (space <= 0) return stack;
        int n = Math.min(stack.getCount(), space);
        if (!simulate) { charcoalAmount += n; setChanged(); sendData(); }
        if (n >= stack.getCount()) return ItemStack.EMPTY;
        return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - n);
    }

    public ItemStack tryExtractLocal(int amount, boolean simulate) {
        if (burning || charcoalAmount == 0) return ItemStack.EMPTY;
        int n = Math.min(amount, charcoalAmount);
        if (!simulate) { charcoalAmount -= n; setChanged(); sendData(); }
        return new ItemStack(Items.CHARCOAL, n);
    }

    public ItemStack tryExtractMultiblock(int amount, boolean simulate) {
        if (burning) return ItemStack.EMPTY;
        List<BloomeryBlockEntity> group = getMultiblockGroup();
        int rem = amount;
        for (BloomeryBlockEntity be : group) { if (rem<=0) break; rem -= be.tryExtractLocal(rem,true).getCount(); }
        int total = amount - rem;
        if (total == 0) return ItemStack.EMPTY;
        if (!simulate) {
            int left = total;
            for (BloomeryBlockEntity be : group) { if (left<=0) break; left -= be.tryExtractLocal(left,false).getCount(); }
        }
        return new ItemStack(Items.CHARCOAL, total);
    }

    // ── Iron ─────────────────────────────────────────────────────────────────────

    public ItemStack tryInsertIron(ItemStack stack, boolean simulate) {
        if (burning || !isValidIron(stack) || !ironItem.isEmpty() || charcoalAmount < 16) return stack;
        if (!simulate) { ironItem = ItemHandlerHelper.copyStackWithSize(stack, 1); setChanged(); sendData(); }
        if (stack.getCount() > 1) return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()-1);
        return ItemStack.EMPTY;
    }

    public ItemStack tryExtractIron(boolean simulate) {
        if (burning || ironItem.isEmpty()) return ItemStack.EMPTY;
        ItemStack r = ironItem.copy();
        if (!simulate) { ironItem = ItemStack.EMPTY; setChanged(); sendData(); }
        return r;
    }

    // ── Steel ─────────────────────────────────────────────────────────────────────

    public ItemStack tryExtractSteelMultiblock(int amount, boolean simulate) {
        List<BloomeryBlockEntity> group = getMultiblockGroup();
        int can = 0; Item steelItem = Items.IRON_INGOT;
        for (BloomeryBlockEntity be : group) {
            if (can >= amount) break;
            if (!be.steelOutput.isEmpty()) { steelItem = be.steelOutput.getItem(); can++; }
        }
        if (can == 0) return ItemStack.EMPTY;
        if (!simulate) {
            int left = can;
            for (BloomeryBlockEntity be : group) {
                if (left <= 0) break;
                if (!be.steelOutput.isEmpty()) { be.steelOutput=ItemStack.EMPTY; be.setChanged(); be.sendData(); left--; }
            }
        }
        return new ItemStack(steelItem, can);
    }

    public boolean   hasSteelOutput()  { return getMultiblockGroup().stream().anyMatch(be->!be.steelOutput.isEmpty()); }
    public ItemStack getSteelOutput()  { return steelOutput; }
    public ItemStack getIronItem()     { return ironItem; }

    // ── Predicates ───────────────────────────────────────────────────────────────

    public boolean isCharcoal(ItemStack s) { return !s.isEmpty() && s.is(Items.CHARCOAL); }

    public boolean isValidIron(ItemStack s) {
        if (s.isEmpty()) return false;
        return s.is(Items.IRON_INGOT) || s.is(Items.RAW_IRON)
                || s.is(CRUSHED_IRON_TAG) || s.is(IRON_SHEET_TAG);
    }

    // ── Drop absorption ──────────────────────────────────────────────────────────

    public void absorbDroppedItem(ItemEntity itemEntity) {
        if (level==null||level.isClientSide||burning) return;
        ItemStack st = itemEntity.getItem();
        if (st.isEmpty()) return;

        if (isCharcoal(st)) {
            for (BloomeryBlockEntity be : getMultiblockGroup()) {
                if (st.isEmpty()) break;
                int sp = MAX_PER_BLOCK - be.charcoalAmount;
                if (sp<=0) continue;
                int n = Math.min(st.getCount(), sp);
                be.charcoalAmount+=n; be.setChanged(); be.sendData(); st.shrink(n);
            }
            if (st.isEmpty()) itemEntity.discard();
        } else if (isValidIron(st)) {
            for (BloomeryBlockEntity be : getMultiblockGroup()) {
                if (st.isEmpty()) break;
                if (!be.ironItem.isEmpty()||be.charcoalAmount<16) continue;
                be.ironItem=ItemHandlerHelper.copyStackWithSize(st,1);
                be.setChanged(); be.sendData(); st.shrink(1);
            }
            if (st.isEmpty()) itemEntity.discard();
        }
    }

    // ── Multiblock group ─────────────────────────────────────────────────────────

    public List<BloomeryBlockEntity> getMultiblockGroup() {
        if (level==null) return List.of(this);
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof BloomeryBlock)) return List.of(this);

        BloomeryBlock.StructureType structure = state.getValue(BloomeryBlock.STRUCTURE);
        BloomeryBlock.BowlPart part = state.getValue(BloomeryBlock.PART);

        return switch (structure) {
            case BOWL_2X2 -> {
                // NW is the origin
                BlockPos nw = switch (part) {
                    case NW -> worldPosition;
                    case NE -> worldPosition.west();
                    case SW -> worldPosition.north();
                    case SE -> worldPosition.north().west();
                    default -> worldPosition;
                };
                yield collect(nw, nw.east(), nw.south(), nw.south().east());
            }
            case BOWL_3X3 -> {
                // Find C_NW corner (NW part of 3x3 = top-left = smallest X, smallest Z)
                BlockPos cnw = switch (part) {
                    case NW -> worldPosition;
                    case N  -> worldPosition.west();
                    case NE -> worldPosition.west().west();
                    case W  -> worldPosition.north();
                    case C  -> worldPosition.north().west();
                    case E  -> worldPosition.north().west().west();
                    case SW -> worldPosition.north().north();
                    case S  -> worldPosition.north().north().west();
                    case SE -> worldPosition.north().north().west().west();
                    default -> worldPosition;
                };
                List<BlockPos> pos = new ArrayList<>();
                for (int dx=0;dx<3;dx++) for (int dz=0;dz<3;dz++) pos.add(cnw.offset(dx,0,dz));
                yield collect(pos.toArray(new BlockPos[0]));
            }
            case LINE_3X1 -> {
                boolean isZ = part==BloomeryBlock.BowlPart.Z_N
                        || part==BloomeryBlock.BowlPart.Z_M
                        || part==BloomeryBlock.BowlPart.Z_S;
                if (isZ) {
                    BlockPos first = switch (part) {
                        case Z_N -> worldPosition;
                        case Z_M -> worldPosition.north();
                        case Z_S -> worldPosition.north().north();
                        default  -> worldPosition;
                    };
                    yield collect(first, first.south(), first.south().south());
                } else {
                    BlockPos first = switch (part) {
                        case X_W -> worldPosition;
                        case X_M -> worldPosition.west();
                        case X_E -> worldPosition.west().west();
                        default  -> worldPosition;
                    };
                    yield collect(first, first.east(), first.east().east());
                }
            }
            default -> List.of(this);
        };
    }

    private List<BloomeryBlockEntity> collect(BlockPos... positions) {
        List<BloomeryBlockEntity> g = new ArrayList<>();
        for (BlockPos p : positions)
            if (level.getBlockEntity(p) instanceof BloomeryBlockEntity be) g.add(be);
        return g.isEmpty() ? List.of(this) : g;
    }

    // ── Renderer ─────────────────────────────────────────────────────────────────

    public float getFillRatio() {
        List<BloomeryBlockEntity> g = getMultiblockGroup();
        int total = g.stream().mapToInt(be->be.charcoalAmount).sum();
        int max   = g.size()*MAX_PER_BLOCK;
        return max==0?0f:(float)total/max;
    }

    public float getSteelRatio() {
        List<BloomeryBlockEntity> g = getMultiblockGroup();
        long n = g.stream().filter(be->!be.steelOutput.isEmpty()).count();
        return g.isEmpty()?0f:(float)n/g.size();
    }

    public float getClientBurnProgress(float partialTick) {
        return Math.min(clientBurnProgress+(burning?partialTick:0f),BURN_DURATION);
    }

    public boolean isBurning()         { return burning;        }
    public int     getCharcoalAmount() { return charcoalAmount; }
    public int     getMaxCharcoal()    { return MAX_PER_BLOCK;  }

    // ── Create behaviours ────────────────────────────────────────────────────────

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this) {
            @Override public boolean canInsertFromSide(Direction side) { return true; }
            @Override
            public ItemStack handleInsertion(ItemStack stack, Direction side, boolean simulate) {
                if (burning) return stack;
                if (isCharcoal(stack)) {
                    ItemStack rem=stack.copy();
                    for (BloomeryBlockEntity be:getMultiblockGroup()) { if(rem.isEmpty())break; rem=be.tryInsert(rem,simulate); }
                    return rem;
                }
                if (isValidIron(stack)) {
                    ItemStack rem=stack.copy();
                    for (BloomeryBlockEntity be:getMultiblockGroup()) { if(rem.isEmpty())break; rem=be.tryInsertIron(rem,simulate); }
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
        ironItem    = tag.contains("Iron")  ? ItemStack.of(tag.getCompound("Iron"))  : ItemStack.EMPTY;
        steelOutput = tag.contains("Steel") ? ItemStack.of(tag.getCompound("Steel")) : ItemStack.EMPTY;
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
        if (cap==ForgeCapabilities.ITEM_HANDLER) return handlerOpt.cast();
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); handlerOpt.invalidate(); }

    // ── IItemHandler ─────────────────────────────────────────────────────────────

    private class BloomeryItemHandler implements IItemHandler {
        @Override public int getSlots() { return 3; }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return switch (slot) {
                case 0 -> { int t=getMultiblockGroup().stream().mapToInt(be->be.charcoalAmount).sum(); yield t>0?new ItemStack(Items.CHARCOAL,t):ItemStack.EMPTY; }
                case 1 -> ironItem.isEmpty()?ItemStack.EMPTY:ironItem.copy();
                case 2 -> { long c=getMultiblockGroup().stream().filter(be->!be.steelOutput.isEmpty()).count(); if(c==0) yield ItemStack.EMPTY; Item s=getMultiblockGroup().stream().filter(be->!be.steelOutput.isEmpty()).findFirst().map(be->be.steelOutput.getItem()).orElse(Items.IRON_INGOT); yield new ItemStack(s,(int)c); }
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (burning) return stack;
            return switch (slot) {
                case 0 -> { if(!isCharcoal(stack)) yield stack; ItemStack r=stack.copy(); for(BloomeryBlockEntity be:getMultiblockGroup()){if(r.isEmpty())break;r=be.tryInsert(r,simulate);} yield r; }
                case 1 -> { ItemStack r=stack.copy(); for(BloomeryBlockEntity be:getMultiblockGroup()){if(r.isEmpty())break;r=be.tryInsertIron(r,simulate);} yield r; }
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
                case 0 -> getMultiblockGroup().size()*MAX_PER_BLOCK;
                case 1,2 -> getMultiblockGroup().size();
                default -> 0;
            };
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (burning) return false;
            return switch (slot) {
                case 0 -> isCharcoal(stack);
                case 1 -> isValidIron(stack)&&charcoalAmount>=16;
                default -> false;
            };
        }
    }
}