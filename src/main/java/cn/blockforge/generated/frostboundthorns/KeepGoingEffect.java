package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 「继续前进」buff：玩家 1 分钟内连续释放 3 次及以上冰震术时获得（见 ModEvents 的施法记录），
 * 持有期间自身造成的 TrueIce 伤害翻倍（判定在 {@link TrueIceDamage#damage}）。
 * 纯标记型效果：不加属性、不做每刻逻辑，图标用冰震术同款雪花（textures/mob_effect/keep_going.png）。
 */
public final class KeepGoingEffect extends MobEffect {
    public KeepGoingEffect() {
        // BENEFICIAL = 增益类（状态条显示在 HUD 左侧），颜色取冰蓝
        super(MobEffectCategory.BENEFICIAL, 0x8FD8FF);
    }
}
