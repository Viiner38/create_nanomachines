package net.viiner.nanomachines.item;

import net.viiner.nanomachines.Nanomachines;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Nanomachines.MOD_ID);

    public static final RegistryObject<SimpleParticleType> WHITESWEEP_ATTACK =
            PARTICLES.register("whitesweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ORANGESWEEP_ATTACK =
            PARTICLES.register("orangesweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MAGENTASWEEP_ATTACK =
            PARTICLES.register("magentasweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LIGHTBLUESWEEP_ATTACK =
            PARTICLES.register("lightbluesweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> YELLOWSWEEP_ATTACK =
            PARTICLES.register("yellowsweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LIMESWEEP_ATTACK =
            PARTICLES.register("limesweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> PINKSWEEP_ATTACK =
            PARTICLES.register("pinksweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> GRAYSWEEP_ATTACK =
            PARTICLES.register("graysweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LIGHTGRAYSWEEP_ATTACK =
            PARTICLES.register("lightgraysweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CYANSWEEP_ATTACK =
            PARTICLES.register("cyansweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> PURPLESWEEP_ATTACK =
            PARTICLES.register("purplesweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BLUESWEEP_ATTACK =
            PARTICLES.register("bluesweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BROWNSWEEP_ATTACK =
            PARTICLES.register("brownsweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> GREENSWEEP_ATTACK =
            PARTICLES.register("greensweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> REDSWEEP_ATTACK =
            PARTICLES.register("redsweep_attack", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BLACKSWEEP_ATTACK =
            PARTICLES.register("blacksweep_attack", () -> new SimpleParticleType(false));

    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }
}
