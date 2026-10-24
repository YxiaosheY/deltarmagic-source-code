package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * "治疗祷言" 附魔：法术类附魔，可以附魔在任何头盔上（原版各类材质头盔与龟壳）。
 * 佩戴带该附魔的头盔后，按 X 打开法术菜单选中"治疗祷言"，按 Z 释放：消耗 32 TP。
 * 十字准星瞄准着生物时：治疗该生物 [其最大生命值 × 0.75] 点生命；
 * 未瞄准任何生物时：给自己恢复 [护甲值 × 0.8] 点生命值。
 * 两种情况都在被治疗者头顶抛下少许绿色烟花粒子，并播放吟唱音效 snd_power.ogg。
 * 获取途径：仅创造模式物品栏（战利品宝箱、村民交易、附魔台一律不出现）。
 */
public final class HealPrayerEnchantment extends Enchantment {
    public HealPrayerEnchantment() {
        // ARMOR_HEAD 类别天然覆盖所有"头盔"物品（铁/金/链甲/钻/下界合金/龟壳），
        // 铁砧"书合装备"的兼容性检查走 canEnchant，所以不需要额外的铁砧分支。
        super(Rarity.RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 附魔书可以携带这个附魔（创造拿书、铁砧"书合书"都按这个放行）
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return true;
        }
        return super.canEnchant(stack);
    }

    /** 附魔台永远不出现它（创造限定法术附魔）。 */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    /** 村民不卖这本书。 */
    @Override
    public boolean isTradeable() {
        return false;
    }

    /** 任何战利品宝箱都刷不出它。 */
    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
