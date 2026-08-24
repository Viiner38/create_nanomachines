package net.viiner.nanomachines.client;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.viiner.nanomachines.block.bloomery.BloomeryBlock;
import net.viiner.nanomachines.block.bloomery.BloomeryBlockEntity;

public class BloomeryScenes {

    public static void sizes(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("bloomery.bloomery", "Bloomery Multiblocks");
        scene.configureBasePlate(0, 0, 5);

        scene.world().restoreBlocks(util.select().everywhere());
        resetAll9ToSingle(scene, util);

        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                scene.world().modifyBlockEntityNBT(
                        util.select().position(util.grid().at(x, 1, z)),
                        BloomeryBlockEntity.class, nbt -> {
                            nbt.putInt("Charcoal", 0);
                            nbt.putBoolean("Burning", false);
                            nbt.remove("Iron");
                            nbt.remove("Steel");
                        });
            }
        }

        scene.showBasePlate();
        scene.idle(5);

        BlockPos center = util.grid().at(2, 1, 2);

        // ── 1×1 ──────────────────────────────────────────────────
        scene.world().showSection(util.select().position(center), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(40)
                .text("text_1")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(center, Direction.NORTH));
        scene.idle(50);

        // ── 3×1 ──────────────────────────────────────────────────
        scene.world().showSection(
                util.select().position(util.grid().at(1, 1, 2)), Direction.DOWN);
        scene.idle(8);
        scene.world().showSection(
                util.select().position(util.grid().at(3, 1, 2)), Direction.DOWN);
        scene.idle(10);

        scene.world().modifyBlock(util.grid().at(1, 1, 2), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.LINE_3X1)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.X_W), false);
        scene.world().modifyBlock(center, s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.LINE_3X1)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.X_M), false);
        scene.world().modifyBlock(util.grid().at(3, 1, 2), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.LINE_3X1)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.X_E), false);

        scene.overlay().showText(90)
                .text("text_2")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 2));
        scene.idle(100);

        // ── 2×2 ──────────────────────────────────────────────────
        scene.world().hideSection(
                util.select().position(util.grid().at(3, 1, 2)), Direction.DOWN);
        scene.idle(5);

        scene.world().modifyBlock(center, s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.SINGLE)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.NONE), false);
        scene.world().modifyBlock(util.grid().at(1, 1, 2), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.SINGLE)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.NONE), false);

        scene.world().showSection(
                util.select().position(util.grid().at(1, 1, 1)), Direction.DOWN);
        scene.idle(8);
        scene.world().showSection(
                util.select().position(util.grid().at(2, 1, 1)), Direction.DOWN);
        scene.idle(10);

        scene.world().modifyBlock(util.grid().at(1, 1, 1), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_2X2)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.NW), false);
        scene.world().modifyBlock(util.grid().at(2, 1, 1), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_2X2)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.NE), false);
        scene.world().modifyBlock(util.grid().at(1, 1, 2), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_2X2)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.SW), false);
        scene.world().modifyBlock(center, s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_2X2)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.SE), false);

        scene.overlay().showText(80)
                .text("text_3")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(center, Direction.NORTH));
        scene.idle(90);

        // ── 3×3 — näita 5 uut blokki mis polnud 2×2-s ───────────
        scene.world().showSection(
                util.select().position(util.grid().at(3, 1, 1)), Direction.DOWN);
        scene.world().showSection(
                util.select().position(util.grid().at(3, 1, 2)), Direction.DOWN);
        scene.world().showSection(
                util.select().position(util.grid().at(1, 1, 3)), Direction.DOWN);
        scene.world().showSection(
                util.select().position(util.grid().at(2, 1, 3)), Direction.DOWN);
        scene.world().showSection(
                util.select().position(util.grid().at(3, 1, 3)), Direction.DOWN);
        scene.idle(10);

        setAll3x3States(scene, util);

        scene.overlay().showText(50)
                .text("text_4")
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 2));
        scene.idle(60);

        // ── Wooden Slabs ───────────────────────
        scene.world().showSection(
                util.select().fromTo(1, 2, 1, 3, 2, 3), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("text_5")
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .placeNearTarget()
                .pointAt(util.vector().of(2.5, 3.0, 2.5));
        scene.idle(70);

        scene.markAsFinished();
    }

    // ── Stuff ───────────────────────────────────────────────────

    private static void resetAll9ToSingle(SceneBuilder scene, SceneBuildingUtil util) {
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                scene.world().modifyBlock(util.grid().at(x, 1, z), s -> s
                        .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.SINGLE)
                        .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.NONE)
                        .setValue(BloomeryBlock.BURNING, false), false);
            }
        }
    }

    private static void setAll3x3States(SceneBuilder scene, SceneBuildingUtil util) {
        scene.world().modifyBlock(util.grid().at(1, 1, 1), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_3X3)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.NW), false);
        scene.world().modifyBlock(util.grid().at(2, 1, 1), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_3X3)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.N), false);
        scene.world().modifyBlock(util.grid().at(3, 1, 1), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_3X3)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.NE), false);
        scene.world().modifyBlock(util.grid().at(1, 1, 2), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_3X3)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.W), false);
        scene.world().modifyBlock(util.grid().at(2, 1, 2), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_3X3)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.C), false);
        scene.world().modifyBlock(util.grid().at(3, 1, 2), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_3X3)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.E), false);
        scene.world().modifyBlock(util.grid().at(1, 1, 3), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_3X3)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.SW), false);
        scene.world().modifyBlock(util.grid().at(2, 1, 3), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_3X3)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.S), false);
        scene.world().modifyBlock(util.grid().at(3, 1, 3), s -> s
                .setValue(BloomeryBlock.STRUCTURE, BloomeryBlock.StructureType.BOWL_3X3)
                .setValue(BloomeryBlock.PART, BloomeryBlock.BowlPart.SE), false);
    }
}