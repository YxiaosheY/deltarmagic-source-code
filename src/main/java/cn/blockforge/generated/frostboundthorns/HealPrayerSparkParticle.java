package cn.blockforge.generated.frostboundthorns;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * "治疗祷言" 的绿色烟花粒子：复用原版烟花火花贴图（spark_0~spark_7，
 * 1.20.1 里 minecraft:firework 这张图并不存在，引用它会出黑紫错误贴图），
 * 染成绿色，从施法者头顶生成后受重力下落（原版烟花粒子上飘，这里反着来），
 * 寿命末尾几刻渐隐。
 */
public final class HealPrayerSparkParticle extends TextureSheetParticle {
    private HealPrayerSparkParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        // 绿色：烟花火花贴图本身是白色，靠粒子颜色调制染色
        this.setColor(0.35F, 1.0F, 0.45F);
        this.friction = 0.96F;
        this.gravity = 0.06F;
        this.hasPhysics = false;
        this.quadSize = 0.16F + this.random.nextFloat() * 0.12F;
        this.setLifetime(14 + this.random.nextInt(8));
        // 初速：轻微横向散开 + 向下的小初速，配合正重力形成"从头顶抛下"的手感
        this.setParticleSpeed((this.random.nextDouble() - 0.5D) * 0.06D,
                -0.02D - this.random.nextDouble() * 0.04D,
                (this.random.nextDouble() - 0.5D) * 0.06D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age > this.lifetime - 5) {
            this.setAlpha(Math.max(0.0F, (this.lifetime - this.age) / 5.0F));
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
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            HealPrayerSparkParticle particle = new HealPrayerSparkParticle(level, x, y, z);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
