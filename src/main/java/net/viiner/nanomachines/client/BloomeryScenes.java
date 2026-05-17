package net.viiner.nanomachines.client;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.ParticleEmitter;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.viiner.nanomachines.block.bloomery.BloomeryBlock;
import net.viiner.nanomachines.block.bloomery.BloomeryBlockEntity;
import net.viiner.nanomachines.item.ModItems;

public class BloomeryScenes {

    // ================================================================
    // MÕLEMAD STSEENID KASUTAVAD SAMA SKEEMI:
    // assets/create_nanomachines/ponder/bloomery/main.nbt
    //
    // Skeemi ehitus:
    //   Y=0: 5×5 checkered alusplaat (snow + white_concrete)
    //   Y=1: 9 bloomery't positsioonidel (1,1,1) kuni (3,1,3)
    //        SALVESTA SKEEMI 3×3 OLEKUS (structure=BOWL_3X3)
    //   Y=2: 9 stone blokki positsioonidel (1,2,1) kuni (3,2,3)
    //
    // Skeemi suurus: 5×3×5
    // ================================================================

    // Partikli Y stseeni-ruumis:
    //   blokk on grid Y=1 → ülemine pind Y=2.0
    //   tuli on 14/16 kõrgusel bloki SEES → Y = 2.0 - (1.0 - 14/16) = 1.875
    private static final double FIRE_FLOOR_Y = 14.0 / 16.0;
    private static final double PARTICLE_Y   = 2.0 - (1.0 - FIRE_FLOOR_Y); // 1.875

    // 5 eksplitsiitset spawn-positsiooni bloki sees (X/Z offset 0..1 vahel).
    // emitParticles spread=0f → partikkel sünnib TÄPSELT nendel koordinaatidel,
    // mitte clustrina tsentrist. Roteerime tick'i kaupa et täita kogu pind ajas.
    private static final double[][] SUB = {
            {0.15, 0.15}, {0.85, 0.15}, {0.5, 0.5}, {0.15, 0.85}, {0.85, 0.85}
    };

    // ──────────────────────────────────────────────────────────────────
    // STSEEN 1: SIZES — ilmuvad konfiguratsioonid järjest
    // ──────────────────────────────────────────────────────────────────
    public static void sizes(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("bloomery.sizes", "Bloomery Multiblocks");
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
    }

    // ──────────────────────────────────────────────────────────────────
    // STSEEN 2: USAGE — 3×3 kasutamine
    // ──────────────────────────────────────────────────────────────────
    public static void usage(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("bloomery.usage", "Using the Bloomery");
        scene.configureBasePlate(0, 0, 5);

        scene.world().restoreBlocks(util.select().everywhere());

        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                scene.world().modifyBlockEntityNBT(
                        util.select().position(util.grid().at(x, 1, z)),
                        BloomeryBlockEntity.class, nbt -> {
                            nbt.putInt("Charcoal", 0);
                            nbt.putBoolean("Burning", false);
                            nbt.putInt("BurnTick", 0);
                            nbt.remove("Iron");
                            nbt.remove("Steel");
                        });
            }
        }

        scene.showBasePlate();
        scene.idle(5);

        scene.world().showSection(
                util.select().fromTo(1, 1, 1, 3, 1, 3), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("text_1")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(2.5, 2.0, 0.5));
        scene.idle(70);

        // ── Charcoal sisestamine ──────────────────────────────────
        scene.overlay().showText(50)
                .text("text_2")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 2));
        scene.idle(15);

        scene.overlay().showControls(
                        util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.UP),
                        Pointing.DOWN, 30)
                .rightClick()
                .withItem(new ItemStack(Items.CHARCOAL));
        scene.idle(5);

        int[] charcoalSteps = {4, 8, 12, 16};
        for (int step : charcoalSteps) {
            final int charcoal = step;
            for (int x = 1; x <= 3; x++) {
                for (int z = 1; z <= 3; z++) {
                    scene.world().modifyBlockEntityNBT(
                            util.select().position(util.grid().at(x, 1, z)),
                            BloomeryBlockEntity.class,
                            nbt -> nbt.putInt("Charcoal", charcoal));
                }
            }
            scene.idle(12);
        }
        scene.idle(15);

        // ── Iron sisestamine ──────────────────────────────────────
        scene.overlay().showText(40)
                .text("text_3")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 2));
        scene.idle(15);

        scene.overlay().showControls(
                        util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.UP),
                        Pointing.DOWN, 40)
                .rightClick()
                .withItem(new ItemStack(Items.RAW_IRON));
        scene.idle(10);

        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                scene.world().modifyBlockEntityNBT(
                        util.select().position(util.grid().at(x, 1, z)),
                        BloomeryBlockEntity.class, nbt -> {
                            CompoundTag ironTag = new CompoundTag();
                            new ItemStack(Items.RAW_IRON).save(ironTag);
                            nbt.put("Iron", ironTag);
                        });
            }
        }
        scene.idle(20);

        // ── Flint and Steel ───────────────────────────────────────
        scene.overlay().showText(40)
                .text("text_4")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 1), Direction.NORTH));
        scene.idle(15);

        scene.overlay().showControls(
                        util.vector().blockSurface(util.grid().at(2, 1, 1), Direction.NORTH),
                        Pointing.RIGHT, 40)
                .rightClick()
                .withItem(new ItemStack(Items.FLINT_AND_STEEL));
        scene.idle(30);

        // ── Sea kõik põlevaks ─────────────────────────────────────
        setBurningState(scene, util, true);
        scene.idle(5);

        // ── Partiklid + NBT refresh ────────────────────────────────────────────────
        //
        // PARTIKLID:
        //   emitParticles spread=0f → partikkel sünnib TÄPSELT antud Vec3 punktis.
        //   spread mõjutab ainult velocity't, MITTE spawn-positsiooni — seega
        //   spread>0 ei aita katvusega, tekitab ainult velocity hajumist.
        //   Lahendus: SUB-massiivi 5 eksplitsiitset punkti bloki sees,
        //   roteerime tick'i kaupa → täidab kogu bloki pinna ajas naturaalselt.
        //   2 sub-positsiooni per tick per blokk = 2×9×55 = 990 timeline-kirjet.
        //
        // NBT REFRESH (iga 5 tick):
        //   Ponder jooksutab BE tick()-i päriselt — kui tick() dekrementeerib
        //   BurnTick-i ja lülitab Burning=false välja, siis burning_charcoal_layer
        //   kaob kohe. Lahendus: kirjuta Burning=true ja BurnTick=9999 üle
        //   iga 5 tick'i tagant nii NBT-s kui blockstate'is.

        ParticleEmitter flame = (level, x, y, z) ->
                level.addParticle(ParticleTypes.FLAME, x, PARTICLE_Y, z, 0, 0.02, 0);
        ParticleEmitter smoke = (level, x, y, z) ->
                level.addParticle(ParticleTypes.SMOKE, x, PARTICLE_Y + 0.06, z, 0, 0.015, 0);

        for (int tick = 0; tick < 55; tick++) {

            // NBT refresh iga 5 tick'i tagant — võitleb tick() reset'iga
            if (tick % 5 == 0) {
                setBurningState(scene, util, true);
            }

            int rot = tick % SUB.length;
            for (int gx = 1; gx <= 3; gx++) {
                for (int gz = 1; gz <= 3; gz++) {
                    // 2 roteeruvat sub-positsiooni per blokk per tick, spread=0f
                    for (int i = 0; i < 2; i++) {
                        int idx = (i + rot) % SUB.length;
                        Vec3 pos = util.vector().of(
                                gx + SUB[idx][0],
                                PARTICLE_Y,
                                gz + SUB[idx][1]);
                        scene.effects().emitParticles(pos, flame, 0f, 1);
                    }
                    // Smoke bloki tsentrist ~iga 4. tick
                    if ((tick + gx + gz) % 4 == 0) {
                        Vec3 smokePos = util.vector().of(gx + 0.5, PARTICLE_Y + 0.06, gz + 0.5);
                        scene.effects().emitParticles(smokePos, smoke, 0f, 1);
                    }
                }
            }

            scene.idle(1);
        }

        // ── Particle suppression — 9 stone blokki katavad terve bloomery ─────────
        scene.overlay().showText(60)
                .text("text_5")
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .placeNearTarget()
                .pointAt(util.vector().of(2.5, 3.5, 2.5));
        scene.idle(20);

        scene.world().showSection(
                util.select().fromTo(1, 2, 1, 3, 2, 3), Direction.DOWN);
        scene.idle(80);

        scene.world().hideSection(
                util.select().fromTo(1, 2, 1, 3, 2, 3), Direction.UP);
        scene.idle(20);

        // ── Põlemine lõppeb — steel layer ilmub ──────────────────
        scene.overlay().showText(50)
                .text("text_6")
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .placeNearTarget()
                .pointAt(util.vector().of(2.5, 1.5, 2.5));
        scene.idle(20);

        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                BlockPos p = util.grid().at(x, 1, z);
                scene.world().modifyBlock(p,
                        s -> s.setValue(BloomeryBlock.BURNING, false), false);
                scene.world().modifyBlockEntityNBT(
                        util.select().position(p), BloomeryBlockEntity.class, nbt -> {
                            nbt.putBoolean("Burning", false);
                            nbt.putInt("BurnTick", 0);
                            nbt.putInt("Charcoal", 0);
                            nbt.remove("Iron");
                            CompoundTag steelTag = new CompoundTag();
                            new ItemStack(ModItems.NANOMACHINES_STEEL.get()).save(steelTag);
                            nbt.put("Steel", steelTag);
                        });
            }
        }
        scene.idle(40);

        scene.markAsFinished();
    }

    // ── Abimeetodid ───────────────────────────────────────────────────

    /**
     * Seab kõik 9 blokki põlevaks (true) või mittepõlevaks (false).
     * Kutsutakse nii esmakordsel aktiveerimisel kui ka NBT refresh-tsüklis,
     * et tick() ei saaks Burning-olekut tagasi lülitada.
     * BurnTick=9999 tagab et tick() ei lülita põlemist välja animatsiooni ajal.
     */
    private static void setBurningState(SceneBuilder scene, SceneBuildingUtil util,
                                        boolean burning) {
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                BlockPos p = util.grid().at(x, 1, z);
                scene.world().modifyBlock(p,
                        s -> s.setValue(BloomeryBlock.BURNING, burning), false);
                scene.world().modifyBlockEntityNBT(
                        util.select().position(p), BloomeryBlockEntity.class, nbt -> {
                            nbt.putBoolean("Burning", burning);
                            nbt.putInt("BurnTick", burning ? 9999 : 0);
                            nbt.putInt("Charcoal", burning ? 16 : 0);
                        });
            }
        }
    }

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