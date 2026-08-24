package net.viiner.nanomachines.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.viiner.nanomachines.Nanomachines;
import java.util.HashSet;


import java.util.Set;

@Mod.EventBusSubscriber(modid = Nanomachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModNetworking {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Nanomachines.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CHANNEL.registerMessage(
                    0,
                    PlasmaBeamPacket.class,
                    PlasmaBeamPacket::encode,
                    PlasmaBeamPacket::decode,
                    PlasmaBeamPacket::handle
            );
        });
    }

    public static void sendBeam(Level level, BlockPos origin, Direction facing, int length, int color, boolean active) {
        if (!(level instanceof ServerLevel sl)) return;
        PlasmaBeamPacket pkt = new PlasmaBeamPacket(origin, facing, length, color, active, sl.dimension());

        Set<ChunkPos> chunks = new HashSet<>();
        chunks.add(new ChunkPos(origin));
        int span = Math.max(length, 1);
        for (int i = 0; i <= span; i++) {
            chunks.add(new ChunkPos(origin.relative(facing, i)));
        }
        for (ChunkPos cp : chunks) {
            LevelChunk chunk = sl.getChunkSource().getChunkNow(cp.x, cp.z);
            if (chunk != null) {
                CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), pkt);
            }
        }
    }
}