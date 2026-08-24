package net.viiner.nanomachines.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.viiner.nanomachines.block.ModBlocks;
import net.viiner.nanomachines.block.plasmacannon.PlasmaCannonBlockEntity;

/**
 * ModColors
 *
 * Registers IBlockColor and IItemColor handlers so the glass parts of the
 * Plasma Cannon model are tinted to match the stored beam colour.
 *
 * The model JSON must mark the glass faces with "tintindex": 0.
 * See assets/nanomachines/models/block/plasma_cannon.json.
 *
 * Wire up in NanomachinesMod:
 * <pre>
 *   modBus.addListener(ModColors::onRegisterBlockColors);
 *   modBus.addListener(ModColors::onRegisterItemColors);
 * </pre>
 */
@OnlyIn(Dist.CLIENT)
public class ModColors {

    /**
     * Block colour handler.
     *
     * tintIndex 0 → glass panes / lens element → returns stored beam colour.
     * Any other tintIndex → returns -1 (no tint, uses texture colour as-is).
     */
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColor plasmaColor = (state, level, pos, tintIndex) -> {
            if (tintIndex != 0 || level == null || pos == null) return 0xFFFFFF;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PlasmaCannonBlockEntity cannon) {
                return cannon.getBeamColor();
            }
            return 0xFFFFFF; // default white
        };

        event.register(plasmaColor, ModBlocks.PLASMACANNON.get());
    }

    /**
     * Item colour handler — tints the inventory item's glass element too.
     *
     * The BlockItem has no BlockEntity, so we fall back to white for items
     * that have never been dyed, and read the stored colour from NBT for
     * items that were picked up after being dyed.
     *
     * NBT path: tag.BlockEntityTag.BeamColor (written automatically by
     * BlockEntity#saveToItem when the block is broken).
     */
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColor plasmaItemColor = (stack, tintIndex) -> {
            if (tintIndex != 0) return 0xFFFFFF;
            var tag = stack.getTagElement("BlockEntityTag");
            if (tag != null && tag.contains("BeamColor")) {
                return tag.getInt("BeamColor");
            }
            return 0xFFFFFF; // default white
        };

        event.register(plasmaItemColor,
                ModBlocks.PLASMACANNON.get().asItem());
    }
}
