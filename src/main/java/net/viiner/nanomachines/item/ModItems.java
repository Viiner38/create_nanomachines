package net.viiner.nanomachines.item;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.viiner.nanomachines.Nanomachines;
import net.viiner.nanomachines.item.custom.HFBladeItem;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Nanomachines.MOD_ID);

    private static RegistryObject<Item> registerHFBlade(
            String name, RegistryObject<SimpleParticleType> particle) {
        return ITEMS.register(name, () -> new HFBladeItem(ModToolTiers.STEEL, 5, -2.4f,
                        new Item.Properties().rarity(Rarity.UNCOMMON).durability(2013), particle));}

    //HF BLADES
    public static final RegistryObject<Item> HF_BLADE =
            registerHFBlade("hf_blade", ModParticles.GRAYSWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_RED =
            registerHFBlade("hf_blade_red", ModParticles.REDSWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_PINK =
            registerHFBlade("hf_blade_pink", ModParticles.PINKSWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_GREEN =
            registerHFBlade("hf_blade_green", ModParticles.GREENSWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_LIGHT_BLUE =
            registerHFBlade("hf_blade_light_blue", ModParticles.LIGHTBLUESWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_LIGHT_GRAY =
            registerHFBlade("hf_blade_light_gray", ModParticles.LIGHTGRAYSWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_CYAN =
            registerHFBlade("hf_blade_cyan", ModParticles.CYANSWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_PURPLE =
            registerHFBlade("hf_blade_purple", ModParticles.PURPLESWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_YELLOW =
            registerHFBlade("hf_blade_yellow", ModParticles.YELLOWSWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_ORANGE =
            registerHFBlade("hf_blade_orange", ModParticles.ORANGESWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_WHITE =
            registerHFBlade("hf_blade_white", ModParticles.WHITESWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_MAGENTA =
            registerHFBlade("hf_blade_magenta", ModParticles.MAGENTASWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_LIME =
            registerHFBlade("hf_blade_lime", ModParticles.LIMESWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_BROWN =
            registerHFBlade("hf_blade_brown", ModParticles.BROWNSWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_BLUE =
            registerHFBlade("hf_blade_blue", ModParticles.BLUESWEEP_ATTACK);
    public static final RegistryObject<Item> HF_BLADE_BLACK =
            registerHFBlade("hf_blade_black", ModParticles.BLACKSWEEP_ATTACK);

    //TOOLS
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

    //ARMOR
    public static final RegistryObject<Item> NANOMACHINES_STEELHELMET = ITEMS.register("steel_helmet", () ->
                    new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELCHESTPLATE = ITEMS.register("steel_chestplate", () ->
                    new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELLEGGINGS = ITEMS.register("steel_leggings", () ->
                    new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELBOOTS = ITEMS.register("steel_boots", () ->
                    new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.BOOTS, new Item.Properties()));

    //MATERIALS
    public static final RegistryObject<Item> NANOMACHINES_STEEL = ITEMS.register("steel_ingot", () ->
                    new Item(new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_STEELSHEET = ITEMS.register("steel_sheet", () ->
                    new Item(new Item.Properties()));

    //SWORD PARTS
    public static final RegistryObject<Item> NANOMACHINES_HFMOTOR = ITEMS.register("hf_motor", () ->
                    new Item(new Item.Properties()));
    public static final RegistryObject<Item> NANOMACHINES_HFHANDLE = ITEMS.register("hf_handle", () ->
                    new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NANOMACHINES_BLADE_PART = ITEMS.register("blade_part", () ->
                    new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NANOMACHINES_INCOMPLETE_BLADE_PART = ITEMS.register("incomplete_blade_part", () ->
            new SequencedAssemblyItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NANOMACHINES_INCOMPLETE_HFMOTOR = ITEMS.register("incomplete_hf_motor", () ->
            new SequencedAssemblyItem(new Item.Properties()));

    //NANOMACHINES
    public static final RegistryObject<Item> NANOMACHINES_HEART = ITEMS.register("nm_heart", () ->
                    new Item(new Item.Properties().rarity(Rarity.EPIC)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}