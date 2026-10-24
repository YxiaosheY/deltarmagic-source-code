package cn.blockforge.generated.frostboundthorns;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

/** 冰震术雪花粒子类型：把参数编解码接到 {@link IceShockSnowflakeOptions}。 */
public final class IceShockSnowflakeType extends ParticleType<IceShockSnowflakeOptions> {
    public IceShockSnowflakeType() {
        super(true, IceShockSnowflakeOptions.DESERIALIZER);
    }

    @Override
    public Codec<IceShockSnowflakeOptions> codec() {
        return IceShockSnowflakeOptions.CODEC;
    }
}
