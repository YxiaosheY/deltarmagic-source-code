package cn.blockforge.generated.frostboundthorns;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 「TrueIce（真冰）」伤害类型的入口。伤害类型本体是数据驱动的：
 * <ul>
 * <li>定义：{@code data/deltamagic/damage_type/true_ice.json}（随模组内置数据包在服务器启动时加载）；</li>
 * <li>无视护甲：把它挂进原版标签 {@code data/minecraft/tags/damage_type/bypasses_armor.json}，
 *     玩家盔甲与盔甲韧性对这类伤害完全无效（与魔法伤害同一机制）。</li>
 * </ul>
 * 这里只提供两件事：按注册表取 {@link DamageSource}（附带施法者，保证击杀归属），
 * 以及「继续前进」buff 的伤害翻倍判定 {@link #damage}。
 */
public final class TrueIceDamage {
    /** 伤害类型的注册 id（deltamagic:true_ice）。 */
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "true_ice");
    public static final ResourceKey<DamageType> KEY = ResourceKey.create(Registries.DAMAGE_TYPE, ID);

    /** 「继续前进」buff 下 TrueIce 伤害的倍率。 */
    private static final float KEEP_GOING_MULTIPLIER = 2.0F;

    private TrueIceDamage() {
    }

    /** 以 caster 为来源实体与直接实体的 TrueIce 伤害源（怪物会记住攻击者，正常掉经验）。 */
    public static DamageSource source(ServerLevel level, @Nullable Entity caster) {
        Holder<DamageType> type = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(KEY);
        return caster == null ? new DamageSource(type) : new DamageSource(type, caster, caster);
    }

    /**
     * TrueIce 伤害的最终数值：攻击者（或其来源实体）身上有「继续前进」buff 时翻倍。
     * 冰震术、雪葬的真冰跳伤都从这里过一遍，保证规则一致。
     */
    public static float damage(float base, @Nullable LivingEntity attacker) {
        if (attacker != null && attacker.hasEffect(GeneratedMod.KEEP_GOING.get())) {
            return base * KEEP_GOING_MULTIPLIER;
        }
        return base;
    }
}
