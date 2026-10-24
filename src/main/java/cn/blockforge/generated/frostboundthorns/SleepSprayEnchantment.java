package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * "睡眠喷雾" 附魔：法术类附魔，可以附魔在「雪花指环」或「荆棘指环」上（或附魔书本身）。
 * 佩戴后按 X 打开法术菜单选中"睡眠喷雾"，按 Z 对着 32 格内看向的生物释放：
 * 播放 snd_spell_pacify.ogg，陆续飘出三块 3×3×2 的雾团（抖动、放大、渐隐，各持续 1 秒），
 * 雾气覆盖范围（宽 3 格 × 高 2 格）内的生物每游戏刻生物数据 Tiredness +3，
 * 当 Tiredness 超过该生物最大生命值 × 3 时被强制杀死（不掉落物品，经验掉落两倍）。
 * 获取途径：战利品宝箱专属池 5%；图书管理员有 15% 概率出售（见 SleepSprayTradeManager）。
 * 附魔台不出现；为避免与原版"随机附魔书抽奖 / 随机村民交易池"重复计入概率，
 * isDiscoverable 与 isTradeable 均关闭，两条获取渠道都由本模组精确控制。
 */
public final class SleepSprayEnchantment extends Enchantment {
    private static final EnchantmentCategory CATEGORY = EnchantmentCategory.create(
            "sleep_spray", item -> item == GeneratedMod.THORN_RING.get()
                    || item == GeneratedMod.SNOWFLAKE_RING.get());

    public SleepSprayEnchantment() {
        super(Rarity.RARE, CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 附魔书可以携带这个附魔（宝箱掉落、村民出售、铁砧"书合书"都按这个放行）
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return true;
        }
        // 实际附到物品上时，只允许两种指环
        return stack.is(GeneratedMod.THORN_RING.get()) || stack.is(GeneratedMod.SNOWFLAKE_RING.get());
    }

    /** 附魔台永远不出现它（法术附魔一律不进附魔台）。 */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    /** 不进原版随机村民交易池：出售概率由 SleepSprayTradeManager 精确控制为 15%。 */
    @Override
    public boolean isTradeable() {
        return false;
    }

    /** 不进原版随机附魔书抽奖：宝箱概率由 5% 专属战利品池精确控制。 */
    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
