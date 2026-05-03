package net.viiner.nanomachines.item.custom;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.RegistryObject;

public class HFBladeItem extends SwordItem {
    private final RegistryObject<SimpleParticleType> sweepParticle;

    public HFBladeItem(
            Tier tier,
            int damage,
            float speed,
            Properties properties,
            RegistryObject<SimpleParticleType> sweepParticle) {
        super(tier, damage, speed, properties);
        this.sweepParticle = sweepParticle;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        if (action == ToolActions.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, action);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (!attacker.level().isClientSide()
                && attacker.level() instanceof ServerLevel serverLevel) {

            Vec3 look = attacker.getLookAngle().normalize();

            double x = attacker.getX() + look.x;
            double y = attacker.getY(0.6);
            double z = attacker.getZ() + look.z;

            serverLevel.sendParticles(
                    sweepParticle.get(),
                    x,
                    y,
                    z,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }
        return result;
    }
}