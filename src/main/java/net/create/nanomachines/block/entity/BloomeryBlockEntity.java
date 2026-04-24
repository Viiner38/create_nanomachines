package net.create.nanomachines.block.entity;

import net.create.nanomachines.block.BloomeryBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BloomeryBlockEntity extends BlockEntity {

    public static final int CHARCOAL_PER_BLOCK = 16;

    private int charcoalAmount = 0;
    private BlockPos controllerPos;

    public BloomeryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLOOMERY.get(), pos, state);
        this.controllerPos = pos;
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(BlockPos controllerPos) {
        if (!controllerPos.equals(this.controllerPos)) {
            this.controllerPos = controllerPos;
            setChanged();
        }
    }

    public boolean isController() {
        return worldPosition.equals(controllerPos);
    }

    public int getCharcoalAmount() {
        return charcoalAmount;
    }

    public void setCharcoalAmount(int charcoalAmount) {
        int clamped = Mth.clamp(charcoalAmount, 0, getCapacity());
        if (this.charcoalAmount != clamped) {
            this.charcoalAmount = clamped;
            setChanged();
        }
    }

    public int addCharcoal(int amount) {
        if (amount <= 0) return 0;

        int inserted = Math.min(amount, getCapacity() - charcoalAmount);
        if (inserted > 0) {
            charcoalAmount += inserted;
            setChanged();
        }
        return inserted;
    }

    public int getCapacity() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof BloomeryBlock bloomeryBlock
                && state.hasProperty(BloomeryBlock.STRUCTURE)
                && state.getValue(BloomeryBlock.STRUCTURE) == BloomeryBlock.StructureType.BOWL_2X2) {
            return 4 * CHARCOAL_PER_BLOCK;
        }
        return CHARCOAL_PER_BLOCK;
    }

    public float getFillFraction() {
        return getCapacity() == 0 ? 0 : (float) charcoalAmount / (float) getCapacity();
    }

    public int getVisualLevel16() {
        return Mth.clamp(Mth.ceil(getFillFraction() * 16.0f), 0, 16);
    }
    public void setRawCharcoalAmount(int amount) {
        this.charcoalAmount = Math.max(0, amount);
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void syncController(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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
        if (tag.contains("ControllerPos")) {
            controllerPos = BlockPos.of(tag.getLong("ControllerPos"));
        } else {
            controllerPos = worldPosition;
        }
    }
}