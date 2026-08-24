package net.viiner.nanomachines.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class PlasmaLightningParticle extends TextureSheetParticle {

    protected PlasmaLightningParticle(ClientLevel level, double x, double y, double z,
                                      SpriteSet sprites, int color, int variant) {
        super(level, x, y, z, 0, 0, 0);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.hasPhysics = false;
        this.gravity = 0;
        this.lifetime = 3 + this.random.nextInt(4);
        this.quadSize = 0.14f + this.random.nextFloat() * 0.10f;
        this.roll = this.random.nextFloat() * (float) Math.PI;
        this.oRoll = this.roll;

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        this.rCol = r * 0.7f + 0.3f;
        this.gCol = g * 0.7f + 0.3f;
        this.bCol = b * 0.7f + 0.3f;
        this.alpha = 0.9f;
        this.setSprite(sprites.get(Math.floorMod(variant, 3), 3));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float t = 1f - (float) this.age / this.lifetime;
        this.alpha = 0.9f * t;
        this.quadSize *= 0.95f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<PlasmaLightningOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(PlasmaLightningOptions options, ClientLevel level,
                                       double x, double y, double z, double vx, double vy, double vz) {
            return new PlasmaLightningParticle(level, x, y, z, sprites, options.color(), options.variant());
        }
    }
}