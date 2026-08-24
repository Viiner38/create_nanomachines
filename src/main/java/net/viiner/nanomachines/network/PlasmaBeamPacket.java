package net.viiner.nanomachines.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.viiner.nanomachines.client.PlasmaCannonClient;

import java.util.function.Supplier;

public record PlasmaBeamPacket(
        BlockPos origin,
        Direction facing,
        int length,
        int color,
        boolean active,
        ResourceKey<Level> dimension
) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(origin);
        buf.writeEnum(facing);
        buf.writeVarInt(length);
        buf.writeInt(color);
        buf.writeBoolean(active);
        buf.writeResourceLocation(dimension.location());
    }

    public static PlasmaBeamPacket decode(FriendlyByteBuf buf) {
        return new PlasmaBeamPacket(
                buf.readBlockPos(),
                buf.readEnum(Direction.class),
                buf.readVarInt(),
                buf.readInt(),
                buf.readBoolean(),
                ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation())
        );
    }

    public static void handle(PlasmaBeamPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PlasmaCannonClient.handleBeamPacket(msg))
        );
        ctx.get().setPacketHandled(true);
    }
}