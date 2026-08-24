package net.viiner.nanomachines.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.viiner.nanomachines.Nanomachines;
import net.viiner.nanomachines.block.plasmacannon.PlasmaCannonBeamSound;
import net.viiner.nanomachines.network.PlasmaBeamPacket;
import net.viiner.nanomachines.particle.PlasmaLightningOptions;

@Mod.EventBusSubscriber(modid = Nanomachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlasmaCannonClient {

    public static void handleBeamPacket(PlasmaBeamPacket msg) {
        BlockPos origin = msg.origin();
        if (!msg.active()) {
            ClientBeamTracker.BEAMS.remove(origin);
            return;
        }
        ClientBeamTracker.BEAMS.put(origin, new ClientBeamTracker.BeamState(
                origin, msg.facing(), msg.length(), msg.color(), msg.dimension()
        ));
        if (ClientBeamTracker.PLAYING_SOUNDS.add(origin)) {
            Minecraft.getInstance().getSoundManager().play(new PlasmaCannonBeamSound(origin));
        }
    }

    public static void spawnLightningParticles(ClientLevel level) {
        for (ClientBeamTracker.BeamState s : ClientBeamTracker.BEAMS.values()) {
            if (!s.dimension.equals(level.dimension()) || s.length <= 0) continue;
            var rand = level.random;
            if (rand.nextInt(2) != 0) continue;

            double t = 0.4 + rand.nextDouble() * s.length;
            double x = s.origin.getX() + 0.5 + s.facing.getStepX() * t + (rand.nextDouble() - 0.5) * 0.14;
            double y = s.origin.getY() + 0.5 + s.facing.getStepY() * t + (rand.nextDouble() - 0.5) * 0.14;
            double z = s.origin.getZ() + 0.5 + s.facing.getStepZ() * t + (rand.nextDouble() - 0.5) * 0.14;
            level.addParticle(new PlasmaLightningOptions(s.color, rand.nextInt(3)), true, x, y, z, 0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientBeamTracker.clear();
    }
}