package net.viiner.nanomachines;

import com.mojang.logging.LogUtils;
import net.viiner.nanomachines.block.ModBlockEntities;
import net.viiner.nanomachines.block.ModBlocks;
import net.viiner.nanomachines.client.ModColors;
import net.viiner.nanomachines.item.ModCreativeModTabs;
import net.viiner.nanomachines.item.ModItems;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.viiner.nanomachines.sound.ModSounds;
import org.slf4j.Logger;

import static net.viiner.nanomachines.block.ModBlockEntities.REGISTRATE;

@Mod(Nanomachines.MOD_ID)
public class Nanomachines
{
    public static final String MOD_ID = "create_nanomachines";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Nanomachines(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModCreativeModTabs.register(modEventBus);
        ModItems.register(modEventBus);
        net.viiner.nanomachines.item.ModParticles.register(modEventBus);
        net.viiner.nanomachines.particle.ModParticles.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        REGISTRATE.registerEventListeners(modEventBus);
        ModSounds.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModColors::onRegisterBlockColors);
        modEventBus.addListener(ModColors::onRegisterItemColors);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }
}