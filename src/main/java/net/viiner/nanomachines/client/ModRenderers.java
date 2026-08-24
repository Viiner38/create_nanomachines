package net.viiner.nanomachines.client;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.viiner.nanomachines.Nanomachines;
import net.viiner.nanomachines.block.ModBlockEntities;
import net.viiner.nanomachines.block.ModBlocks;
import net.viiner.nanomachines.block.ModPartialModels;
import net.viiner.nanomachines.block.bloomery.BloomeryRenderer;
import net.viiner.nanomachines.particle.ModParticles;
import net.viiner.nanomachines.particle.PlasmaLightningParticle;

@Mod.EventBusSubscriber(modid = Nanomachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModRenderers {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.BLOOMERY.get(),
                BloomeryRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.PLASMACANNON.get(),
                net.viiner.nanomachines.block.plasmacannon.PlasmaCannonRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.PLASMA_LIGHTNING.get(), PlasmaLightningParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ModPartialModels.PLASMA_CANNON_COG.modelLocation());
        ModPartialModels.init();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModPartialModels.init();
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.PLASMACANNON.get(),
                    RenderType.cutout()
            );
            PonderIndex.addPlugin(new BloomeryPonderPlugin());
        });
    }
}