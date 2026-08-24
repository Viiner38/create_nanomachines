package net.viiner.nanomachines.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.viiner.nanomachines.Nanomachines;

public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Nanomachines.MOD_ID);

    public static final RegistryObject<ParticleType<PlasmaLightningOptions>> PLASMA_LIGHTNING =
            PARTICLES.register("plasma_lightning", () -> new ParticleType<>(true, PlasmaLightningOptions.DESERIALIZER) {
                @Override
                public Codec<PlasmaLightningOptions> codec() {
                    return PlasmaLightningOptions.CODEC;
                }
            });

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}