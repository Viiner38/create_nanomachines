package net.viiner.nanomachines.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientBeamTracker {

    public static final Map<BlockPos, BeamState> BEAMS = new ConcurrentHashMap<>();
    public static final Set<BlockPos> PLAYING_SOUNDS = ConcurrentHashMap.newKeySet();

    public static void clear() {
        BEAMS.clear();
        PLAYING_SOUNDS.clear();
    }

    public static class BeamState {
        public final BlockPos origin;
        public final Direction facing;
        public final int length;
        public final int color;
        public final ResourceKey<Level> dimension;

        public BeamState(BlockPos origin, Direction facing, int length, int color, ResourceKey<Level> dimension) {
            this.origin = origin;
            this.facing = facing;
            this.length = length;
            this.color = color;
            this.dimension = dimension;
        }
    }
}