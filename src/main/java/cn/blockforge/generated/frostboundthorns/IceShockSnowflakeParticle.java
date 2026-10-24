package cn.blockforge.generated.frostboundthorns;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

/**
 * 冰震术的雪花粒子（客户端表现，行为由服务端下发的参数决定）：
 * 悬停阶段——原地不动，等雪花环凑齐 8 片；
 * 到点后（与伤害结算同一刻）——环上的小雪花沿各自半径方向向外散开并淡出，
 * 中央的大雪花一边膨胀一边淡出。
 */
public final class IceShockSnowflakeParticle extends TextureSheetParticle {
    private final boolean center;
    private final float angle;
    private final int hold;
    private final float baseSize;
    private final int fadeTicks;
    private boolean released;

    private IceShockSnowflakeParticle(ClientLevel level, IceShockSnowflakeOptions options,
            double x, double y, double z) {
        super(level, x, y, z);
        this.center = options.center();
        this.angle = options.angle();
        this.hold = Math.max(0, options.hold());
        this.baseSize = options.size();
        this.quadSize = options.size();
        this.fadeTicks = this.center ? 18 : 14;
        this.lifetime = this.hold + this.fadeTicks + 1;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.hasPhysics = false;
        this.setParticleSpeed(0.0D, 0.0D, 0.0D);
        this.setColor(0.85F, 0.95F, 1.0F);
    }

    @Override
    public void tick() {
        if (!this.released && this.age >= this.hold) {
            this.released = true;
            if (!this.center) {
                // 雪花环炸开：沿生成时所在半径方向向外飞，带一点上飘
                this.friction = 0.94F;
                this.setParticleSpeed(Math.cos(this.angle) * 0.16D,
                        0.03D + this.random.nextDouble() * 0.05D,
                        Math.sin(this.angle) * 0.16D);
            }
        }
        if (this.released) {
            int out = this.age - this.hold;
            if (this.center) {
                // 中央大雪花：持续膨胀
                this.quadSize = this.baseSize * (1.0F + out * 0.10F);
            }
            this.setAlpha(Math.max(0.0F, 1.0F - out / (float) this.fadeTicks));
        } else {
            // 悬停阶段钉在原地
            this.setParticleSpeed(0.0D, 0.0D, 0.0D);
        }
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /** 注册进粒子引擎的工厂（贴图来自 particles/ice_shock_snowflake.json 的雪花图）。 */
    public static final class Provider implements ParticleProvider<IceShockSnowflakeOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(IceShockSnowflakeOptions options, ClientLevel level,
                double x, double y, double z, double dx, double dy, double dz) {
            IceShockSnowflakeParticle particle = new IceShockSnowflakeParticle(level, options, x, y, z);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
