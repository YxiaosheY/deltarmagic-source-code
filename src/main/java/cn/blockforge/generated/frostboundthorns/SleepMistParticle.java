package cn.blockforge.generated.frostboundthorns;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * "睡眠喷雾"的雾气粒子（spr_icespell_mist.png）：
 * 服务端每次在雾心周围铺 18 片本粒子（3 列 × 3 行 × 2 层，合起来是一整块 3×3×2 的雾），
 * 每片全程抖动（每刻随机微位移）、逐渐放大、透明度渐隐，从出现到消失共 1 秒（20 游戏刻）。
 * 服务端按 0/6/12 刻依次放出三块，覆盖判定（Tiredness +3）也在服务端做，这里只负责画面表现。
 */
public final class SleepMistParticle extends TextureSheetParticle {
    /** 寿命：1 秒（20 游戏刻），"抖动放大渐隐1秒后消失"。 */
    private static final int LIFETIME_TICKS = 20;
    /** 初始大小与每刻的膨胀量（方块）：20 刻从 1.1 长到约 1.5，一片雾片正好盖住一格多，18 片叠成一块雾。 */
    private static final float BASE_SIZE = 1.1F;
    private static final float GROW_PER_TICK = 0.02F;
    /** 出生时的透明度：半透明雾，多层叠加才显出体积感而不会糊成一堵墙。 */
    private static final float START_ALPHA = 0.65F;

    private SleepMistParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.lifetime = LIFETIME_TICKS;
        this.quadSize = BASE_SIZE;
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.gravity = 0.0F;
    }

    @Override
    public void tick() {
        // 抖动：每刻给一点随机微速度（hasPhysics=false，位置只按速度推进，不会飘走）
        this.setParticleSpeed((this.random.nextDouble() - 0.5D) * 0.03D,
                (this.random.nextDouble() - 0.5D) * 0.02D,
                (this.random.nextDouble() - 0.5D) * 0.03D);
        // 放大 + 渐隐：按存活刻数线性推进，第 20 刻完全透明并消失
        this.quadSize = BASE_SIZE + GROW_PER_TICK * this.age;
        this.setAlpha(Math.max(0.0F, START_ALPHA * (1.0F - this.age / (float) LIFETIME_TICKS)));
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /** 注册进粒子引擎的工厂（贴图来自 particles/sleep_mist.json 的雾气图）。 */
    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                double x, double y, double z, double dx, double dy, double dz) {
            SleepMistParticle particle = new SleepMistParticle(level, x, y, z);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
