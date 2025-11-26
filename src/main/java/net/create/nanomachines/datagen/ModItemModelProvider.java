package net.create.nanomachines.datagen;

import net.create.nanomachines.Nanomachines;
import net.create.nanomachines.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;


public class ModItemModelProvider extends ItemModelProvider {
        public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, Nanomachines.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            handheldItem(ModItems.HF_MURASAMA);




        }

    private ItemModelBuilder handheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("minecraft:item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(Nanomachines.MOD_ID,"item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItem(RegistryObject<? extends Block> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(Nanomachines.MOD_ID,"item/" + item.getId().getPath()));
    }

}
