package net.viiner.nanomachines.block.plasmacannon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.viiner.nanomachines.client.ClientBeamTracker;
import net.viiner.nanomachines.sound.ModSounds;

public class PlasmaCannonBeamSound extends AbstractTickableSoundInstance {

    private final BlockPos origin;

    public PlasmaCannonBeamSound(BlockPos origin) {
        super(ModSounds.PLASMA_BEAM.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.origin = origin;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.attenuation = Attenuation.LINEAR;
        Vec3 pos = Vec3.atCenterOf(origin);
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        ClientBeamTracker.BeamState state = ClientBeamTracker.BEAMS.get(origin);
        if (state == null) {
            ClientBeamTracker.PLAYING_SOUNDS.remove(origin);
            stop();
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            ClientBeamTracker.PLAYING_SOUNDS.remove(origin);
            stop();
            return;
        }

        Vec3 nearest = nearestPoint(state, player.position());
        this.x = nearest.x;
        this.y = nearest.y;
        this.z = nearest.z;
        this.volume = 1.0f;
        this.pitch = 1.0f;
    }

    private static Vec3 nearestPoint(ClientBeamTracker.BeamState state, Vec3 playerPos) {
        int len = Math.max(1, state.length);
        Vec3 start = Vec3.atCenterOf(state.origin);
        Vec3 end = start.add(
                state.facing.getStepX() * len,
                state.facing.getStepY() * len,
                state.facing.getStepZ() * len
        );
        Vec3 seg = end.subtract(start);
        double segLenSq = seg.lengthSqr();
        if (segLenSq < 1.0e-6) return start;
        double t = Mth.clamp(playerPos.subtract(start).dot(seg) / segLenSq, 0.0, 1.0);
        return start.add(seg.scale(t));
    }
}