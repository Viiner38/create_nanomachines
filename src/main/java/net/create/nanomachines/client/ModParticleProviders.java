package net.create.nanomachines.client;


import net.create.nanomachines.Nanomachines;
import net.create.nanomachines.client.particle.RedSweepParticle;
import net.create.nanomachines.item.ModParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Nanomachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModParticleProviders {

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.REDSWEEP_ATTACK.get(), RedSweepParticle.Provider::new);
    }
}