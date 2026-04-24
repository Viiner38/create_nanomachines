package net.create.nanomachines.block.entity;

import net.create.nanomachines.block.BloomeryBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class BloomeryBlockEntity extends BlockEntity {

    public static final int CHARCOAL_PER_BLOCK = 16;

    private int charcoalAmount = 0;
    private BlockPos controllerPos;

    private final IItemHandler inputHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || !stack.is(Items.CHARCOAL)) {
                return stack;
            }

            int inserted = insertCharcoal(stack.getCount(), simulate);
            if (inserted <= 0) {
                return stack;
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(inserted);
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(Items.CHARCOAL);
        }
    };

    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> inputHandler);

    public BloomeryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLOOMERY.get(), pos, state);
        this.controllerPos = pos;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public boolean isController() {
        return worldPosition.equals(controllerPos);
    }

    public BloomeryBlockEntity getController() {
        if (level == null) {
            return this;
        }

        BlockEntity be = level.getBlockEntity(controllerPos);
        if (be instanceof BloomeryBlockEntity bloomery) {
            return bloomery;
        }

        return this;
    }

    public void syncController(BlockPos newControllerPos) {
        if (!newControllerPos.equals(this.controllerPos)) {
            this.controllerPos = newControllerPos;
            notifySelfUpdate();
        }
    }

    public int getCharcoalAmount() {
        return charcoalAmount;
    }

    public int getTotalCharcoalAmount() {
        return getController().charcoalAmount;
    }

    public int getCapacity() {
        BloomeryBlockEntity controller = getController();
        BlockState state = controller.getBlockState();

        if (state.getBlock() instanceof BloomeryBlock
                && state.hasProperty(BloomeryBlock.STRUCTURE)
                && state.getValue(BloomeryBlock.STRUCTURE) == BloomeryBlock.StructureType.BOWL_2X2) {
            return 4 * CHARCOAL_PER_BLOCK;
        }

        return CHARCOAL_PER_BLOCK;
    }

    public float getFillFraction() {
        int capacity = getCapacity();
        return capacity <= 0 ? 0.0f : (float) getTotalCharcoalAmount() / (float) capacity;
    }

    public int getVisualLevel16() {
        return Mth.clamp(Mth.ceil(getFillFraction() * 16.0f), 0, 16);
    }

    public int insertCharcoal(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }

        BloomeryBlockEntity controller = getController();
        int space = controller.getCapacity() - controller.charcoalAmount;
        int inserted = Math.min(space, amount);

        if (inserted > 0 && !simulate) {
            controller.charcoalAmount += inserted;
            controller.syncVisuals();
        }

        return inserted;
    }

    public void setRawCharcoalAmount(int amount) {
        int clamped = Math.max(0, amount);
        if (this.charcoalAmount != clamped) {
            this.charcoalAmount = clamped;
            syncVisuals();
        }
    }

    private void notifySelfUpdate() {
        setChanged();

        if (level == null || level.isClientSide) {
            return;
        }

        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 3);
    }

    private void syncVisuals() {
        setChanged();

        if (level == null || level.isClientSide) {
            return;
        }

        sendUpdateIfBloomery(worldPosition);

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof BloomeryBlock)) {
            return;
        }

        if (isController() && state.getValue(BloomeryBlock.STRUCTURE) == BloomeryBlock.StructureType.BOWL_2X2) {
            BlockPos origin = getBlockPos();
            sendUpdateIfBloomery(origin.east());
            sendUpdateIfBloomery(origin.south());
            sendUpdateIfBloomery(origin.south().east());
        }
    }

    private void sendUpdateIfBloomery(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BloomeryBlock) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("CharcoalAmount", charcoalAmount);
        tag.putLong("ControllerPos", controllerPos.asLong());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        charcoalAmount = tag.getInt("CharcoalAmount");
        controllerPos = tag.contains("ControllerPos")
                ? BlockPos.of(tag.getLong("ControllerPos"))
                : worldPosition;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }
}