package net.viiner.nanomachines.block;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.viiner.nanomachines.Nanomachines;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.viiner.nanomachines.block.bloomery.BloomeryBlockEntity;
import net.viiner.nanomachines.block.plasmacannon.PlasmaCannonBlockEntity;
import net.viiner.nanomachines.block.plasmacannon.PlasmaCannonRenderer;

public class ModBlockEntities {

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(Nanomachines.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Nanomachines.MOD_ID);

    public static final RegistryObject<BlockEntityType<BloomeryBlockEntity>> BLOOMERY =
            BLOCK_ENTITY_TYPES.register("bloomery", () ->
                    BlockEntityType.Builder.of(
                            (pos, state) -> new BloomeryBlockEntity(ModBlockEntities.BLOOMERY.get(), pos, state),
                            ModBlocks.BLOOMERY.get()
                    ).build(null)
            );

    // VIGA (parandatud): .visual(...) registreeris Flywheel visuaali selle
    // block entity jaoks. Flywheel'i aktiivsena olles võtab see renderdamise
    // üle ja PlasmaCannonRenderer.renderSafe() (kus beami kood asub) ei
    // pruugi kunagi käivituda. Eemaldatud - ainult .renderer(...) jäetakse
    // alles, mis langeb tagasi puhtale vanilla BlockEntityRenderer teele.
    public static final BlockEntityEntry<PlasmaCannonBlockEntity> PLASMACANNON = REGISTRATE
            .blockEntity("plasma_cannon", PlasmaCannonBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual.of(AllPartialModels.SHAFTLESS_COGWHEEL), true)
            .validBlock(() -> ModBlocks.PLASMACANNON.get())
            .renderer(() -> PlasmaCannonRenderer::new)
            .register();

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}