package net.viiner.nanomachines.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public record PlasmaLightningOptions(int color, int variant) implements ParticleOptions {

    public static final Codec<PlasmaLightningOptions> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("color").forGetter(PlasmaLightningOptions::color),
            Codec.INT.fieldOf("variant").forGetter(PlasmaLightningOptions::variant)
    ).apply(i, PlasmaLightningOptions::new));

    public static final Deserializer<PlasmaLightningOptions> DESERIALIZER = new Deserializer<>() {
        @Override
        public PlasmaLightningOptions fromCommand(ParticleType<PlasmaLightningOptions> type, StringReader reader)
                throws CommandSyntaxException {
            reader.expect(' ');
            int color = reader.readInt();
            reader.expect(' ');
            int variant = reader.readInt();
            return new PlasmaLightningOptions(color, variant);
        }

        @Override
        public PlasmaLightningOptions fromNetwork(ParticleType<PlasmaLightningOptions> type, FriendlyByteBuf buf) {
            return new PlasmaLightningOptions(buf.readInt(), buf.readVarInt());
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ModParticles.PLASMA_LIGHTNING.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(color);
        buf.writeVarInt(variant);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %d %d",
                ForgeRegistries.PARTICLE_TYPES.getKey(getType()), color, variant);
    }
}