package net.create.nanomachines.block;

import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BloomeryBlockEntity extends SmartBlockEntity {

    public static final int MAX_PER_BLOCK = 16;
    private int charcoalAmount = 0;

    private final BloomeryItemHandler itemHandler = new BloomeryItemHandler();
    private final LazyOptional<IItemHandler> itemHandlerOpt = LazyOptional.of(() -> itemHandler);

    public BloomeryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ---------------------------------------------------------------
    // Create behaviours
    // ---------------------------------------------------------------

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this) {
            @Override
            public boolean canInsertFromSide(Direction side) {
                return true;
            }

            @Override
            public ItemStack handleInsertion(ItemStack stack, Direction side, boolean simulate) {
                return tryInsert(stack, simulate);
            }
        });
    }

    // ---------------------------------------------------------------
    // Üksikbloki sisemine soe lisamine / eemaldamine
    // ---------------------------------------------------------------

    public ItemStack tryInsert(ItemStack stack, boolean simulate) {
        if (!isCharcoal(stack)) return stack;
        int space = MAX_PER_BLOCK - charcoalAmount;
        if (space <= 0) return stack;
        int toInsert = Math.min(stack.getCount(), space);
        if (!simulate) {
            charcoalAmount += toInsert;
            setChanged();
            sendData();
        }
        if (toInsert >= stack.getCount()) return ItemStack.EMPTY;
        return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - toInsert);
    }

    /** Võtab söe ainult sellest blokist — kasutatakse sisemiselt. */
    public ItemStack tryExtractLocal(int amount, boolean simulate) {
        if (charcoalAmount == 0) return ItemStack.EMPTY;
        int extracted = Math.min(amount, charcoalAmount);
        if (!simulate) {
            charcoalAmount -= extracted;
            setChanged();
            sendData();
        }
        return new ItemStack(Items.CHARCOAL, extracted);
    }

    /**
     * Võtab söe kogu 2x2 struktuurist (või ainult sellest blokist kui SINGLE).
     * Järjekord: NW → NE → SW → SE.
     */
    public ItemStack tryExtractMultiblock(int amount, boolean simulate) {
        List<BloomeryBlockEntity> group = getMultiblockGroup();
        if (group.size() == 1) return tryExtractLocal(amount, simulate);

        // Simulate pass — arvuta kui palju saab kätte
        int remaining = amount;
        for (BloomeryBlockEntity be : group) {
            if (remaining <= 0) break;
            ItemStack got = be.tryExtractLocal(remaining, true);
            remaining -= got.getCount();
        }
        int totalExtracted = amount - remaining;
        if (totalExtracted == 0) return ItemStack.EMPTY;

        // Tegelik pass
        if (!simulate) {
            int toTake = totalExtracted;
            for (BloomeryBlockEntity be : group) {
                if (toTake <= 0) break;
                ItemStack got = be.tryExtractLocal(toTake, false);
                toTake -= got.getCount();
            }
        }
        return new ItemStack(Items.CHARCOAL, totalExtracted);
    }

    // Asenda AINULT see meetod BloomeryBlockEntity.java-s
    public void absorbDroppedItem(ItemEntity itemEntity) {
        if (level == null || level.isClientSide) return;

        // Võta OTSEVIIDE stackile — mitte copy
        // Nii näeb iga järgmine tick/kutse kohe uuendatud kogust
        ItemStack itemStack = itemEntity.getItem();
        if (!isCharcoal(itemStack) || itemStack.isEmpty()) return;

        List<BloomeryBlockEntity> group = getMultiblockGroup();
        for (BloomeryBlockEntity be : group) {
            if (itemStack.isEmpty()) break;

            int space = MAX_PER_BLOCK - be.charcoalAmount;
            if (space <= 0) continue;

            int toInsert = Math.min(itemStack.getCount(), space);
            be.charcoalAmount += toInsert;
            be.setChanged();
            be.sendData();

            // Shrink KOHE samal viitel — järgmine kutse näeb uut kogust
            itemStack.shrink(toInsert);
        }

        if (itemStack.isEmpty()) {
            itemEntity.discard();
        }
    }

    private boolean isCharcoal(ItemStack stack) {
        return stack.is(Items.CHARCOAL);
    }

    // ---------------------------------------------------------------
    // Multibloki rühm — leiab kõik 4 blokki (või ainult enda)
    // ---------------------------------------------------------------

    /**
     * Tagastab kõik BloomeryBlockEntity'd samas 2x2 struktuuris,
     * järjekorras NW, NE, SW, SE. Kui SINGLE, tagastab ainult enda.
     */
    public List<BloomeryBlockEntity> getMultiblockGroup() {
        List<BloomeryBlockEntity> group = new ArrayList<>();
        if (level == null) { group.add(this); return group; }

        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof BloomeryBlock)) { group.add(this); return group; }

        if (state.getValue(BloomeryBlock.STRUCTURE) != BloomeryBlock.StructureType.BOWL_2X2) {
            group.add(this);
            return group;
        }

        // Leia NW (origin) nurk
        BlockPos nw = switch (state.getValue(BloomeryBlock.PART)) {
            case NW -> worldPosition;
            case NE -> worldPosition.west();
            case SW -> worldPosition.north();
            case SE -> worldPosition.north().west();
            default -> worldPosition;
        };

        BlockPos[] positions = { nw, nw.east(), nw.south(), nw.south().east() };
        for (BlockPos pos : positions) {
            if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be) {
                group.add(be);
            }
        }

        if (group.isEmpty()) { group.add(this); }
        return group;
    }

    // ---------------------------------------------------------------
    // Renderer — ühtlane täitumissuhe kogu 2x2 struktuuris
    // ---------------------------------------------------------------

    /**
     * Täitumissuhe 0.0–1.0.
     * 2x2 korral: (kõigi 4 bloki söe kokku) / 64.
     * SINGLE korral: enda söe / 16.
     */
    public float getFillRatio() {
        if (level == null) return (float) charcoalAmount / MAX_PER_BLOCK;

        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof BloomeryBlock)) return (float) charcoalAmount / MAX_PER_BLOCK;
        if (state.getValue(BloomeryBlock.STRUCTURE) != BloomeryBlock.StructureType.BOWL_2X2) {
            return (float) charcoalAmount / MAX_PER_BLOCK;
        }

        List<BloomeryBlockEntity> group = getMultiblockGroup();
        int totalCharcoal = 0;
        int totalMax = 0;
        for (BloomeryBlockEntity be : group) {
            totalCharcoal += be.charcoalAmount;
            totalMax += MAX_PER_BLOCK;
        }
        if (totalMax == 0) return 0f;
        return (float) totalCharcoal / totalMax;
    }

    public int getCharcoalAmount() { return charcoalAmount; }
    public int getMaxCharcoal()    { return MAX_PER_BLOCK; }

    // ---------------------------------------------------------------
    // NBT
    // ---------------------------------------------------------------

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        charcoalAmount = tag.getInt("Charcoal");
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("Charcoal", charcoalAmount);
    }

    // ---------------------------------------------------------------
    // Capability
    // ---------------------------------------------------------------

    // UUS — tagastab handler KÕIKIDELE külgedele sh null (mida Arm kasutab)
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return itemHandlerOpt.cast(); // side ei loe — kõik küljed OK
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOpt.invalidate();
    }

    // ---------------------------------------------------------------
    // IItemHandler — kasutab multibloki ekstraktsiooni
    // ---------------------------------------------------------------

    private class BloomeryItemHandler implements IItemHandler {

        @Override
        public int getSlots() { return 1; }

        @Override
        public ItemStack getStackInSlot(int slot) {
            // Näita kogu 2x2 sisu
            List<BloomeryBlockEntity> group = getMultiblockGroup();
            int total = group.stream().mapToInt(be -> be.charcoalAmount).sum();
            return total > 0 ? new ItemStack(Items.CHARCOAL, total) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            // Distribueerib kogu 2x2 gruppi
            if (!isCharcoal(stack)) return stack;
            List<BloomeryBlockEntity> group = getMultiblockGroup();
            ItemStack remaining = stack.copy();
            for (BloomeryBlockEntity be : group) {
                if (remaining.isEmpty()) break;
                remaining = be.tryInsert(remaining, simulate);
            }
            return remaining;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            // Ekstraktib kogu 2x2-st
            return tryExtractMultiblock(amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            // 16 per blokk * bloki arv grupis (max 64)
            return getMultiblockGroup().size() * MAX_PER_BLOCK;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return isCharcoal(stack);
        }
    }
}
