package net.create.nanomachines.item;

import net.create.nanomachines.Nanomachines;
import net.create.nanomachines.item.custom.FuelItem;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Nanomachines.MOD_ID);

    public static final RegistryObject<Item> HF_MURASAMA = ITEMS.register("hf_murasama", ()
    -> new SwordItem (Tiers.NETHERITE, 7, -2.4f, new Item.Properties()
            .fireResistant().rarity(Rarity.EPIC).durability(2013)));

    public static final RegistryObject<Item> NANOMACHINES_STEELPICKAXE = ITEMS.register("steel_pickaxe", () ->
            new PickaxeItem(ModToolTiers.STEEL, -1, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELAXE = ITEMS.register("steel_axe", () ->
            new AxeItem(ModToolTiers.STEEL, 4, -3.1f, new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELHOE = ITEMS.register("steel_hoe", () ->
            new HoeItem(ModToolTiers.STEEL, -4, -1f, new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELSWORD = ITEMS.register("steel_sword", () ->
            new SwordItem(ModToolTiers.STEEL, 1, -2.4f, new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELSHOVEL = ITEMS.register("steel_shovel", () ->
            new ShovelItem(ModToolTiers.STEEL, 0, -3f, new Item.Properties()));

    public static final RegistryObject<Item> NANOMACHINES_STEELHELMET = ITEMS.register("steel_helmet", () ->
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELCHESTPLATE = ITEMS.register("steel_chestplate", () ->
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELLEGGINGS = ITEMS.register("steel_leggings", () ->
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELBOOTS = ITEMS.register("steel_boots", () ->
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> NANOMACHINES_COALDUST = ITEMS.register("coal_dust", ()
            -> new FuelItem(new Item.Properties(), 800));
    public static final RegistryObject<Item> NANOMACHINES_STEEL = ITEMS.register("steel_ingot", ()
            -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELSHEET = ITEMS.register("steel_sheet", ()
            -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_HFMOTOR = ITEMS.register("hf_motor", ()
            -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_HFHANDLE = ITEMS.register("hf_handle", ()
            -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);

    }
}