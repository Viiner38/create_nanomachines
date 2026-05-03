package net.viiner.nanomachines.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.viiner.nanomachines.Nanomachines;
import net.viiner.nanomachines.client.particle.HFSweepParticle;
import net.viiner.nanomachines.item.ModParticles;

@Mod.EventBusSubscriber(
        modid = Nanomachines.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModParticleProviders {

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {

        event.registerSpriteSet(ModParticles.REDSWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PINKSWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GREENSWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GRAYSWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.LIGHTBLUESWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.WHITESWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.ORANGESWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.MAGENTASWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.YELLOWSWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.LIMESWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.LIGHTGRAYSWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.CYANSWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PURPLESWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BLUESWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BROWNSWEEP_ATTACK.get(), HFSweepParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BLACKSWEEP_ATTACK.get(), HFSweepParticle.Provider::new);

    }
}