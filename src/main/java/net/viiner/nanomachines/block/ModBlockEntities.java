package net.viiner.nanomachines.block;

import net.viiner.nanomachines.Nanomachines;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Nanomachines.MOD_ID);

    public static final RegistryObject<BlockEntityType<BloomeryBlockEntity>> BLOOMERY =
            BLOCK_ENTITY_TYPES.register("bloomery", () ->
                    BlockEntityType.Builder.of(
                            (pos, state) -> new BloomeryBlockEntity(ModBlockEntities.BLOOMERY.get(), pos, state),
                            ModBlocks.BLOOMERY.get()
                    ).build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}