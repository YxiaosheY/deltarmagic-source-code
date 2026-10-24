package cn.blockforge.generated.frostboundthorns;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 冰震术雪花粒子的参数（服务端 → 客户端随粒子包一起下发）。
 * 一份参数描述一片雪花：它是环上的还是中央的、朝哪个角度散开、
 * 原地悬停多少游戏刻后才开始散开/膨胀、初始多大。
 * 服务端按时间轴逐刻 sendParticles，环上雪花的 hold 依次递减，
 * 于是所有雪花都会精确地在"伤害结算"那一刻同时炸开。
 */
public final class IceShockSnowflakeOptions implements ParticleOptions {
    public static final Codec<IceShockSnowflakeOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("center").forGetter(options -> options.center),
            Codec.FLOAT.fieldOf("angle").forGetter(options -> options.angle),
            Codec.INT.fieldOf("hold").forGetter(options -> options.hold),
            Codec.FLOAT.fieldOf("size").forGetter(options -> options.size)
    ).apply(instance, (center, angle, hold, size) ->
            new IceShockSnowflakeOptions(GeneratedMod.ICE_SHOCK_SNOWFLAKE.get(), center, angle, hold, size)));

    public static final ParticleOptions.Deserializer<IceShockSnowflakeOptions> DESERIALIZER =
            new ParticleOptions.Deserializer<IceShockSnowflakeOptions>() {
                @Override
                public IceShockSnowflakeOptions fromCommand(ParticleType<IceShockSnowflakeOptions> type,
                        StringReader reader) throws CommandSyntaxException {
                    boolean center = reader.readInt() != 0;
                    reader.skipWhitespace();
                    float angle = (float) reader.readDouble();
                    reader.skipWhitespace();
                    int hold = reader.readInt();
                    reader.skipWhitespace();
                    float size = (float) reader.readDouble();
                    return new IceShockSnowflakeOptions(type, center, angle, hold, size);
                }

                @Override
                public IceShockSnowflakeOptions fromNetwork(ParticleType<IceShockSnowflakeOptions> type,
                        FriendlyByteBuf buffer) {
                    return new IceShockSnowflakeOptions(type,
                            buffer.readBoolean(), buffer.readFloat(), buffer.readVarInt(), buffer.readFloat());
                }
            };

    private final ParticleType<IceShockSnowflakeOptions> type;
    /** true = 环中央的大雪花（膨胀渐隐）；false = 雪花环上的小雪花（沿半径散开）。 */
    private final boolean center;
    /** 散开方向（弧度，与生成位置的角度一致，从正上方俯视按顺时针递增）。 */
    private final float angle;
    /** 原地悬停的游戏刻数，到点后小雪花散开 / 大雪花开始膨胀。 */
    private final int hold;
    /** 初始大小（方块）。 */
    private final float size;

    public IceShockSnowflakeOptions(ParticleType<IceShockSnowflakeOptions> type,
            boolean center, float angle, int hold, float size) {
        this.type = type;
        this.center = center;
        this.angle = angle;
        this.hold = hold;
        this.size = size;
    }

    @Override
    public ParticleType<IceShockSnowflakeOptions> getType() {
        return type;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(center);
        buffer.writeFloat(angle);
        buffer.writeVarInt(hold);
        buffer.writeFloat(size);
    }

    @Override
    public String writeToString() {
        return "deltamagic:ice_shock_snowflake center=" + center + " angle=" + angle
                + " hold=" + hold + " size=" + size;
    }

    public boolean center() {
        return center;
    }

    public float angle() {
        return angle;
    }

    public int hold() {
        return hold;
    }

    public float size() {
        return size;
    }
}
