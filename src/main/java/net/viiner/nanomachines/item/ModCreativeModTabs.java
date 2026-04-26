package net.viiner.nanomachines.item;

import net.viiner.nanomachines.Nanomachines;
import net.viiner.nanomachines.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Nanomachines.MOD_ID);

    public static final RegistryObject<CreativeModeTab> NANOMACHINES_TAB = CREATIVE_MODE_TABS.register("nanomachines_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.HF_MURASAMA.get()))
                    .title(Component.translatable("creativetab.nanomachines_tab"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModItems.HF_MURASAMA.get());

                        output.accept(ModItems.NANOMACHINES_COALDUST.get());
                        output.accept(ModItems.NANOMACHINES_STEEL.get());
                        output.accept(ModItems.NANOMACHINES_STEELSHEET.get());

                        output.accept(ModItems.NANOMACHINES_STEELPICKAXE.get());
                        output.accept(ModItems.NANOMACHINES_STEELAXE.get());
                        output.accept(ModItems.NANOMACHINES_STEELSHOVEL.get());
                        output.accept(ModItems.NANOMACHINES_STEELHOE.get());
                        output.accept(ModItems.NANOMACHINES_STEELSWORD.get());

                        output.accept(ModItems.NANOMACHINES_STEELHELMET.get());
                        output.accept(ModItems.NANOMACHINES_STEELCHESTPLATE.get());
                        output.accept(ModItems.NANOMACHINES_STEELLEGGINGS.get());
                        output.accept(ModItems.NANOMACHINES_STEELBOOTS.get());
                        output.accept(ModItems.NANOMACHINES_HFMOTOR.get());
                        output.accept(ModItems.NANOMACHINES_HFHANDLE.get());
                        output.accept(ModItems.NANOMACHINES_HFBLADE.get());
                        output.accept(ModItems.NANOMACHINES_HEART.get());

                        output.accept(ModBlocks.BLOOMERY.get());


                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
