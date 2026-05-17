package net.viiner.nanomachines.item;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "create_nanomachines")
public class ModCauldronInteractions {

    private static final TagKey<Item> COLORED_HF_TAG =
            ItemTags.create(new ResourceLocation("create_nanomachines", "colored_hf"));

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        Item hfBlade = ForgeRegistries.ITEMS.getValue(new ResourceLocation("create_nanomachines", "hf_blade"));
        if (hfBlade == null) return;

        ForgeRegistries.ITEMS.tags()
                .getTag(COLORED_HF_TAG)
                .stream()
                .forEach(item -> CauldronInteraction.WATER.put(item, createCleanInteraction(hfBlade)));
    }

    private static CauldronInteraction createCleanInteraction(Item resultItem) {
        return (state, level, pos, player, hand, stack) -> {
            if (level.isClientSide) return InteractionResult.SUCCESS;

            ItemStack result = new ItemStack(resultItem);
            stack.shrink(1);

            if (stack.isEmpty()) {
                player.setItemInHand(hand, result);
            } else {
                if (!player.getInventory().add(result)) {
                    player.drop(result, false);
                }
            }

            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.2f);

            return InteractionResult.SUCCESS;
        };
    }
}