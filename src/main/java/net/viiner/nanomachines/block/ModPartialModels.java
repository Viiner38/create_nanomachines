package net.viiner.nanomachines.block;

import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class ModPartialModels {

    public static final PartialModel


            PLASMA_CANNON_COG = block("plasma_cannon/plasma_cannon_cog");



    private static PartialModel block(String path) {
        return PartialModel.of(Create.asResource("block/" + path));
    }

    private static PartialModel entity(String path) {
        return PartialModel.of(Create.asResource("entity/" + path));
    }

    public static void init() {
    }

}