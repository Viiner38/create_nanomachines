package net.viiner.nanomachines.block;

import com.simibubi.create.Create;
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


    public static final CreateRegistrate REGISTRATE = Create.registrate();

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Nanomachines.MOD_ID);

    public static final RegistryObject<BlockEntityType<BloomeryBlockEntity>> BLOOMERY =
            BLOCK_ENTITY_TYPES.register("bloomery", () ->
                    BlockEntityType.Builder.of(
                            (pos, state) -> new BloomeryBlockEntity(ModBlockEntities.BLOOMERY.get(), pos, state),
                            ModBlocks.BLOOMERY.get()
                    ).build(null)
            );


    public static final BlockEntityEntry<PlasmaCannonBlockEntity> PLASMACANNON = REGISTRATE
            .blockEntity("plasma_cannon", PlasmaCannonBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual.of(ModPartialModels.PLASMA_CANNON_COG), false)
            .validBlocks(ModBlocks.PLASMACANNON)
            .renderer(() -> PlasmaCannonRenderer::new)
            .register();




    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}