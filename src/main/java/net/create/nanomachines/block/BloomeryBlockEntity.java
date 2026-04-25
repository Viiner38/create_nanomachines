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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BloomeryBlockEntity extends SmartBlockEntity {

    // ---------------------------------------------------------------
    // Charcoal storage
    // ---------------------------------------------------------------

    /** Söe kogus. Ainult NW (controller) blokis on see täidetud multibloki korral. */
    private int charcoalAmount = 0;

    /**
     * Max söe arv.
     * SINGLE / NW controller = 16 / 64 vastavalt.
     * NE/SW/SE member blokid = 0 (ei hoia ise midagi).
     */
    private int maxCharcoal = 16;

    /** Kas see on 2x2 multibloki "master" (NW nurk ehk origin). */
    private boolean isController = true;

    // ---------------------------------------------------------------
    // Forge capability
    // ---------------------------------------------------------------

    private final LazyOptional<IItemHandler> itemHandlerOpt = LazyOptional.of(this::buildItemHandler);

    public BloomeryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ---------------------------------------------------------------
    // Create behaviours — Mechanical Arm, Chute, Belt input
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
    // Söe lisamine
    // ---------------------------------------------------------------

    /**
     * Proovib lisada söe. Kui pole controller, suunab päringu NW-bloki poole.
     * @return Ülejäänud stack (tühi = kõik võeti vastu)
     */
    public ItemStack tryInsert(ItemStack stack, boolean simulate) {
        if (!isCharcoal(stack)) return stack;

        if (!isController && level != null) {
            BloomeryBlockEntity controller = findController();
            if (controller != null) return controller.tryInsert(stack, simulate);
            return stack;
        }

        int space = maxCharcoal - charcoalAmount;
        if (space <= 0) return stack;

        int toInsert = Math.min(stack.getCount(), space);
        if (!simulate) {
            charcoalAmount += toInsert;
            setChanged();
            sendData();
        }

        if (toInsert >= stack.getCount()) return ItemStack.EMPTY;
        ItemStack remainder = stack.copy();
        remainder.setCount(stack.getCount() - toInsert);
        return remainder;
    }

    /**
     * Kutsutakse BloomeryBlock#entityInside kaudu kui mängija kukutab söe sisse.
     */
    public void absorbDroppedItem(ItemEntity itemEntity) {
        if (level == null || level.isClientSide) return;
        if (!isCharcoal(itemEntity.getItem())) return;

        ItemStack remaining = tryInsert(itemEntity.getItem().copy(), false);
        if (remaining.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.getItem().setCount(remaining.getCount());
        }
    }

    private boolean isCharcoal(ItemStack stack) {
        return stack.is(Items.CHARCOAL);
    }

    // ---------------------------------------------------------------
    // Multibloki API — kutsutakse BloomeryBlock'ist otse
    // ---------------------------------------------------------------

    /** Kutsutakse NW nurgablokile kui 2x2 moodustub. Max = 64. */
    public void setAsController() {
        this.isController = true;
        this.maxCharcoal = 64;
        if (charcoalAmount > maxCharcoal) charcoalAmount = maxCharcoal;
        setChanged();
        sendData();
    }

    /** Kutsutakse NE/SW/SE blokkidele. Ei hoia söe andmeid. */
    public void setAsMember() {
        this.isController = false;
        this.maxCharcoal = 0;
        this.charcoalAmount = 0;
        setChanged();
        sendData();
    }

    /** Kutsutakse kui multiblokk laguneb — tagasi standalone. Max = 16. */
    public void setAsStandalone() {
        this.isController = true;
        this.maxCharcoal = 16;
        if (charcoalAmount > 16) charcoalAmount = 16;
        setChanged();
        sendData();
    }

    // ---------------------------------------------------------------
    // Renderer abimeetodid
    // ---------------------------------------------------------------

    /**
     * Täitumissuhe 0.0–1.0 rendereri jaoks.
     * Mittecontroller blokid leiavad oma controlleri.
     */
    public float getFillRatio() {
        if (isController) {
            if (maxCharcoal == 0) return 0f;
            return (float) charcoalAmount / maxCharcoal;
        }
        BloomeryBlockEntity controller = findController();
        return controller != null ? controller.getFillRatio() : 0f;
    }

    public int getCharcoalAmount() {
        if (isController) return charcoalAmount;
        BloomeryBlockEntity ctrl = findController();
        return ctrl != null ? ctrl.charcoalAmount : 0;
    }

    public int getMaxCharcoal() {
        if (isController) return maxCharcoal;
        BloomeryBlockEntity ctrl = findController();
        return ctrl != null ? ctrl.maxCharcoal : 0;
    }

    /**
     * Leiab selle bloki NW (controller) bloki PART state'i põhjal.
     * Kasutab BloomeryBlock.PART enum-i mis sul juba olemas on.
     */
    @Nullable
    private BloomeryBlockEntity findController() {
        if (level == null) return null;
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof BloomeryBlock)) return null;

        BlockPos controllerPos = switch (state.getValue(BloomeryBlock.PART)) {
            case NE -> worldPosition.west();
            case SW -> worldPosition.north();
            case SE -> worldPosition.north().west();
            default -> worldPosition; // NW ja NONE — see ise on controller
        };

        if (level.getBlockEntity(controllerPos) instanceof BloomeryBlockEntity ctrl) {
            return ctrl;
        }
        return null;
    }

    // ---------------------------------------------------------------
    // NBT
    // ---------------------------------------------------------------

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        charcoalAmount = tag.getInt("Charcoal");
        maxCharcoal    = tag.getInt("MaxCharcoal");
        isController   = tag.getBoolean("IsController");
        if (maxCharcoal <= 0 && isController) maxCharcoal = 16;
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("Charcoal",         charcoalAmount);
        tag.putInt("MaxCharcoal",      maxCharcoal);
        tag.putBoolean("IsController", isController);
    }

    // ---------------------------------------------------------------
    // Capability
    // ---------------------------------------------------------------

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return itemHandlerOpt.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOpt.invalidate();
    }

    private IItemHandler buildItemHandler() {
        return new IItemHandler() {
            @Override public int getSlots() { return 1; }

            @Override
            public ItemStack getStackInSlot(int slot) {
                int amt = getCharcoalAmount();
                return amt > 0 ? new ItemStack(Items.CHARCOAL, amt) : ItemStack.EMPTY;
            }

            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return tryInsert(stack, simulate);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (!isController) {
                    BloomeryBlockEntity ctrl = findController();
                    return ctrl != null
                            ? ctrl.buildItemHandler().extractItem(slot, amount, simulate)
                            : ItemStack.EMPTY;
                }
                if (charcoalAmount == 0) return ItemStack.EMPTY;
                int extracted = Math.min(amount, charcoalAmount);
                if (!simulate) {
                    charcoalAmount -= extracted;
                    setChanged();
                    sendData();
                }
                return new ItemStack(Items.CHARCOAL, extracted);
            }

            @Override public int getSlotLimit(int slot) { return getMaxCharcoal(); }
            @Override public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return isCharcoal(stack);
            }
        };
    }
}