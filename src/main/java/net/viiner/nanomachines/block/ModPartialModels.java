package net.viiner.nanomachines.block;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import net.viiner.nanomachines.Nanomachines;

public class ModPartialModels {

    public static final PartialModel PLASMA_CANNON_COG = block("plasma_cannon_cog");

    private static PartialModel block(String path) {
        return PartialModel.of(new ResourceLocation(Nanomachines.MOD_ID, "block/" + path));
    }
    private static PartialModel entity(String path) {
        return PartialModel.of(new ResourceLocation(Nanomachines.MOD_ID, "entity/" + path));
    }

    public static void init() {
    }
}