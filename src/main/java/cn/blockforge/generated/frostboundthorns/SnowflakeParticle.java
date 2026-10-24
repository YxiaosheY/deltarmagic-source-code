package cn.blockforge.generated.frostboundthorns;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
public final class SnowflakeParticle extends TextureSheetParticle {
    private SnowflakeParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.friction = 0.965F;
        this.gravity = -0.009F;
        this.hasPhysics = false;
        this.quadSize = 0.09F + this.random.nextFloat() * 0.13F;
        this.setLifetime(38 + this.random.nextInt(38));
        // Give every flake a faster upward launch so the effect reads as a fountain.
        this.setParticleSpeed(xSpeed * 0.18D,
                0.15D + this.random.nextFloat() * 0.20D + Math.abs(ySpeed) * 0.55D,
                zSpeed * 0.18D);
        this.setColor(0.82F, 0.96F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age > this.lifetime - 12) {
            this.setAlpha(Math.max(0.0F, (this.lifetime - this.age) / 12.0F));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public net.minecraft.client.particle.Particle createParticle(SimpleParticleType type, ClientLevel level,
                double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SnowflakeParticle particle = new SnowflakeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
