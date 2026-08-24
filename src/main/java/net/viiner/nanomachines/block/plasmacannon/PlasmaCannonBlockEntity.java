package net.viiner.nanomachines.block.plasmacannon;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import net.viiner.nanomachines.network.ModNetworking;

import java.util.List;

public class PlasmaCannonBlockEntity extends KineticBlockEntity {

    public static final float MAX_RPM = 256f;
    public static final int BEAM_RANGE = 256;
    private static final int WARMUP_TICKS = 10;
    private static final float DAMAGE_PER_SEC = 7.0f;
    private static final float BREAK_HARDNESS_PER_SEC = 4.0f;

    private int warmupTimer = 0;
    private boolean isFiring = false;
    private int damageAccumulator = 0;
    private int beamColor = 0xFFFFFF;
    private int lastSentLen = -1;
    private float breakProgress = 0f;
    private BlockPos breakingPos = null;

    public PlasmaCannonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setBeamColor(DyeColor dye) {
        float[] c = dye.getTextureDiffuseColors();
        beamColor = (Math.round(c[0] * 255f) << 16) | (Math.round(c[1] * 255f) << 8) | Math.round(c[2] * 255f);
        sync();
    }

    public void resetBeamColor() {
        beamColor = 0xFFFFFF;
        sync();
    }

    public int getBeamColor() {
        return beamColor;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;

        boolean onServer = !level.isClientSide();
        boolean isPowered = level.hasNeighborSignal(worldPosition);
        boolean isAtMaxRPM = Math.abs(getSpeed()) >= MAX_RPM;
        boolean wasFiring = isFiring;

        if (isPowered && isAtMaxRPM) {
            if (warmupTimer < WARMUP_TICKS) {
                warmupTimer++;
                if (onServer) spawnMuzzleParticles();
            }
            isFiring = warmupTimer >= WARMUP_TICKS;
        } else {
            warmupTimer = 0;
            isFiring = false;
            damageAccumulator = 0;
        }

        if (!onServer) return;

        if (wasFiring != isFiring) {
            sendData();
            if (wasFiring) {
                spawnMuzzleParticles();
                clearBreak();
                ModNetworking.sendBeam(level, worldPosition, getFacing(), 0, beamColor, false);
                lastSentLen = -1;
            }
        }

        if (isFiring) {
            int len = computeBeamLength();
            tryBreakEnd(len);
            len = computeBeamLength();

            if (len != lastSentLen || level.getGameTime() % 5 == 0) {
                ModNetworking.sendBeam(level, worldPosition, getFacing(), len, beamColor, true);
                lastSentLen = len;
            }

            damageAccumulator++;
            if (damageAccumulator >= 20) {
                damageAccumulator = 0;
                applyDamageAlongBeam(len);
            }
        }
    }

    public Direction getFacing() {
        BlockState s = getBlockState();
        return s.hasProperty(PlasmaCannonBlock.FACING) ? s.getValue(PlasmaCannonBlock.FACING) : Direction.NORTH;
    }

    private int computeBeamLength() {
        Direction d = getFacing();
        for (int i = 1; i <= BEAM_RANGE; i++) {
            BlockState state = level.getBlockState(worldPosition.relative(d, i));
            if (!isBeamPassthrough(state)) return i - 1;
        }
        return BEAM_RANGE;
    }

    private static boolean isBeamPassthrough(BlockState state) {
        return state.isAir() || state.getBlock() instanceof LiquidBlock || state.getBlock() == Blocks.FIRE;
    }

    private void tryBreakEnd(int len) {
        if (len >= BEAM_RANGE) {
            clearBreak();
            return;
        }
        Direction d = getFacing();
        BlockPos hit = worldPosition.relative(d, len + 1);
        BlockState state = level.getBlockState(hit);
        float hardness = state.getDestroySpeed(level, hit);
        if (isBeamPassthrough(state) || hardness < 0) {
            clearBreak();
            return;
        }

        if (breakingPos == null || !breakingPos.equals(hit)) {
            clearBreak();
            breakingPos = hit.immutable();
            breakProgress = 0f;
        }

        float needed = Math.max(hardness, 0.3f);
        breakProgress += BREAK_HARDNESS_PER_SEC / 20.0f;
        int stage = Mth.clamp((int) ((breakProgress / needed) * 10.0f), 0, 9);
        level.destroyBlockProgress(breakerId(), hit, stage);

        if (breakProgress >= needed) {
            level.destroyBlock(hit, true);
            clearBreak();
        }
    }

    private void clearBreak() {
        if (breakingPos != null) {
            level.destroyBlockProgress(breakerId(), breakingPos, -1);
        }
        breakingPos = null;
        breakProgress = 0f;
    }

    private int breakerId() {
        return worldPosition.hashCode();
    }

    private void applyDamageAlongBeam(int len) {
        Direction d = getFacing();
        for (int i = 1; i <= len; i++) {
            List<LivingEntity> hits = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(worldPosition.relative(d, i)).inflate(0.2)
            );
            if (!hits.isEmpty()) {
                hits.forEach(e -> e.hurt(level.damageSources().magic(), DAMAGE_PER_SEC));
                return;
            }
        }
    }

    private void spawnMuzzleParticles() {
        if (!(level instanceof ServerLevel sl)) return;
        Direction d = getFacing();
        double x = worldPosition.getX() + 0.5 + d.getStepX() * 0.65;
        double y = worldPosition.getY() + 0.5 + d.getStepY() * 0.65;
        double z = worldPosition.getZ() + 0.5 + d.getStepZ() * 0.65;
        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 8, 0.15, 0.15, 0.15, 0.05);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("WarmupTimer", warmupTimer);
        tag.putBoolean("IsFiring", isFiring);
        tag.putInt("BeamColor", beamColor);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        warmupTimer = tag.getInt("WarmupTimer");
        isFiring = tag.getBoolean("IsFiring");
        int newColor = tag.contains("BeamColor") ? tag.getInt("BeamColor") : 0xFFFFFF;
        if (clientPacket && newColor != beamColor) {
            beamColor = newColor;
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
                requestModelDataUpdate();
            }
        } else {
            beamColor = newColor;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("IsFiring", isFiring);
        tag.putInt("BeamColor", beamColor);
        return tag;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return IForgeBlockEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    public void remove() {
        if (level != null && !level.isClientSide()) {
            clearBreak();
            ModNetworking.sendBeam(level, worldPosition, getFacing(), 0, beamColor, false);
        }
        super.remove();
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide()) {
            ModNetworking.sendBeam(level, worldPosition, getFacing(), 0, beamColor, false);
        }
        super.onChunkUnloaded();
    }

    public boolean isFiring() {
        return isFiring;
    }

    public int getWarmup() {
        return warmupTimer;
    }

    public int getClientBeamLength() {
        if (level == null) return 0;
        return computeBeamLength();
    }
}