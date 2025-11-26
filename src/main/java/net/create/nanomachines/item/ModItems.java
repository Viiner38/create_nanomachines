package net.create.nanomachines.item;

import net.create.nanomachines.Nanomachines;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Nanomachines.MOD_ID);


    public static final RegistryObject<Item> HF_MURASAMA = ITEMS.register("hf_murasama", ()
    -> new SwordItem (Tiers.NETHERITE, 7, -2.4f, new Item.Properties()
            .fireResistant().durability(2013)));





    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);

}
}