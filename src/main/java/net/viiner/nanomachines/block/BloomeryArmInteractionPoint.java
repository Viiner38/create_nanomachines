package net.viiner.nanomachines.block;

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

    // ---------------------------------------------------------------
    // Kõik override'id - kui mõni annab "does not override" vea,
    // kustuta ainult see @Override annotatsioon (meetod jääb alles)
    // ---------------------------------------------------------------

    // Nihuta interaktsiooni punkt 2x2 struktuuri keskele
    protected Vec3 getInteractionPositionVector() {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BloomeryBlock
                && state.getValue(BloomeryBlock.STRUCTURE) == BloomeryBlock.StructureType.BOWL_2X2) {
            BlockPos nw = switch (state.getValue(BloomeryBlock.PART)) {
                case NW -> pos;
                case NE -> pos.west();
                case SW -> pos.north();
                case SE -> pos.north().west();
                default -> pos;
            };
            return Vec3.atLowerCornerOf(nw).add(1.0, 1.0, 1.0);
        }
        return Vec3.atLowerCornerOf(pos).add(0.5, 1.0, 0.5);
    }

    // Insert: täida kõik 2x2 blokid järjekorras
    public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be) {
            List<BloomeryBlockEntity> group = be.getMultiblockGroup();
            ItemStack remaining = stack.copy();
            for (BloomeryBlockEntity member : group) {
                if (remaining.isEmpty()) break;
                remaining = member.tryInsert(remaining, simulate);
            }
            return remaining;
        }
        return stack;
    }

    // Extract: tõmba kogu 2x2-st
    public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof BloomeryBlockEntity be) {
            return be.tryExtractMultiblock(amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    // Üks slot kogu 2x2 jaoks
    public int getSlotCount(ArmBlockEntity armBlockEntity) {
        return 1;
    }

    // ---------------------------------------------------------------
    // Type ja registreerimine
    // ---------------------------------------------------------------

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