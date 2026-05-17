package net.viiner.nanomachines.block.bloomery;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.viiner.nanomachines.Nanomachines;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = Nanomachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BloomeryArmInteractionPoint extends ArmInteractionPoint {

    public BloomeryArmInteractionPoint(ArmInteractionPointType type, Level level,
                                       BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    // ── Interaction position — center of the multiblock ──────────────────────────

    protected Vec3 getInteractionPositionVector() {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BloomeryBlock)) {
            return Vec3.atLowerCornerOf(pos).add(0.5, 1.0, 0.5);
        }

        BloomeryBlock.StructureType structure = state.getValue(BloomeryBlock.STRUCTURE);
        BloomeryBlock.BowlPart part = state.getValue(BloomeryBlock.PART);

        switch (structure) {
            case BOWL_2X2 -> {
                BlockPos nw = switch (part) {
                    case NW -> pos;
                    case NE -> pos.west();
                    case SW -> pos.north();
                    case SE -> pos.north().west();
                    default -> pos;
                };
                // Center of 2x2 = nw + (1, 1, 1)
                return Vec3.atLowerCornerOf(nw).add(1.0, 1.0, 1.0);
            }
            case LINE_3X1 -> {
                boolean isZ = part == BloomeryBlock.BowlPart.Z_N
                        || part == BloomeryBlock.BowlPart.Z_M
                        || part == BloomeryBlock.BowlPart.Z_S;
                if (isZ) {
                    BlockPos first = switch (part) {
                        case Z_N -> pos;
                        case Z_M -> pos.north();
                        case Z_S -> pos.north().north();
                        default  -> pos;
                    };
                    // Center of 3x1 along Z = first + (0.5, 1, 1.5)
                    return Vec3.atLowerCornerOf(first).add(0.5, 1.0, 1.5);
                } else {
                    BlockPos first = switch (part) {
                        case X_W -> pos;
                        case X_M -> pos.west();
                        case X_E -> pos.west().west();
                        default  -> pos;
                    };
                    // Center of 3x1 along X = first + (1.5, 1, 0.5)
                    return Vec3.atLowerCornerOf(first).add(1.5, 1.0, 0.5);
                }
            }
            case BOWL_3X3 -> {
                BlockPos cnw = switch (part) {
                    case NW -> pos;
                    case N  -> pos.west();
                    case NE -> pos.west().west();
                    case W  -> pos.north();
                    case C  -> pos.north().west();
                    case E  -> pos.north().west().west();
                    case SW -> pos.north().north();
                    case S  -> pos.north().north().west();
                    case SE -> pos.north().north().west().west();
                    default   -> pos;
                };
                // Center of 3x3 = cnw + (1.5, 1, 1.5)
                return Vec3.atLowerCornerOf(cnw).add(1.5, 1.0, 1.5);
            }
            default -> { return Vec3.atLowerCornerOf(pos).add(0.5, 1.0, 0.5); }
        }
    }

    // ── Insert — distribute across multiblock ────────────────────────────────────

    public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
        if (!(level.getBlockEntity(pos) instanceof BloomeryBlockEntity be)) return stack;

        List<BloomeryBlockEntity> group = be.getMultiblockGroup();
        if (group.isEmpty()) return stack;

        ItemStack remaining = stack.copy();

        if (be.isCharcoal(remaining)) {
            for (BloomeryBlockEntity member : group) {
                if (remaining.isEmpty()) break;
                remaining = member.tryInsert(remaining, simulate);
            }
        } else if (be.isValidIron(remaining)) {
            for (BloomeryBlockEntity member : group) {
                if (remaining.isEmpty()) break;
                remaining = member.tryInsertIron(remaining, simulate);
            }
        }

        return remaining;
    }

    // ── Extract — from whole multiblock ──────────────────────────────────────────

    public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
        if (!(level.getBlockEntity(pos) instanceof BloomeryBlockEntity be))
            return ItemStack.EMPTY;
        // Try steel first, then charcoal
        ItemStack steel = be.tryExtractSteelMultiblock(amount, simulate);
        if (!steel.isEmpty()) return steel;
        return be.tryExtractMultiblock(amount, simulate);
    }

    public int getSlotCount(ArmBlockEntity armBlockEntity) {
        return 1;
    }

    // ── Type & registration ───────────────────────────────────────────────────────

    private static final ArmInteractionPointType TYPE = new ArmInteractionPointType() {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof BloomeryBlock;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new BloomeryArmInteractionPoint(this, level, pos, state);
        }
    };

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        event.register(
                CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key(),
                new ResourceLocation("create_nanomachines", "bloomery"),
                () -> TYPE
        );
    }
}