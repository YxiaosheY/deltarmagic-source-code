package cn.blockforge.generated.frostboundthorns;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** "粗暴碎击" 飞行拖尾：使用投射物自身 beam 贴图的 7 帧，随机取一帧点缀在弹道上。 */
public final class RoughSmashTrailParticle extends TextureSheetParticle {
    private RoughSmashTrailParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.friction = 0.92F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        // 贴图是垫到 64x64 居中的 43x62 光刃，方形四边形直接显示就不会变形。
        this.quadSize = 0.38F + this.random.nextFloat() * 0.16F;
        this.setLifetime(6 + this.random.nextInt(5));
        this.setParticleSpeed((this.random.nextDouble() - 0.5D) * 0.02D,
                0.005D + this.random.nextDouble() * 0.015D,
                (this.random.nextDouble() - 0.5D) * 0.02D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age > this.lifetime - 3) {
            this.setAlpha(Math.max(0.0F, (this.lifetime - this.age) / 3.0F));
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
            RoughSmashTrailParticle particle = new RoughSmashTrailParticle(level, x, y, z);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
