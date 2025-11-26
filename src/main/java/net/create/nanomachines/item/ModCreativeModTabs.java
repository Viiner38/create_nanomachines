package net.create.nanomachines.item;

import net.create.nanomachines.Nanomachines;
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

                    })
                    .build());




    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
