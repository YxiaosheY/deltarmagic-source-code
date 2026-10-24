package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * "朱红碎击" 附魔：普通（非宝藏）附魔，但**不会刷新在战利品宝箱里**——
 * 获取方式是合成（粗暴碎击附魔书 + 钻石 + 回响碎片 + 红石，见
 * data/deltamagic/recipes/crimson_smash_book.json），村民交易渠道照常保留。
 * 只能附魔在已经附魔"牙刷"的斧头上；附魔后按 X 打开菜单选中"朱红碎击"，再按 Z 释放。
 * 逻辑与粗暴碎击基本相同，只是弹幕贴图换成红色一套，且伤害 = 斧头攻击力 × 6.5、大小 = 斧头攻击力 ÷ 4.5。
 */
public final class CrimsonSmashEnchantment extends Enchantment {
    private static final EnchantmentCategory CATEGORY = EnchantmentCategory.create(
            "crimson_smash", item -> item instanceof AxeItem);

    public CrimsonSmashEnchantment() {
        super(Rarity.RARE, CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 附魔书可以携带这个附魔（合成产物、铁砧"书合书"合并都按这个放行；
        // 宝箱抽奖的开关不在这里，而是下面的 isDiscoverable）
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return true;
        }
        // 实际附到物品上时，只能附魔在附魔"牙刷"的斧头上（铁砧合并时校验目标物品）
        return stack.getItem() instanceof AxeItem
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.TOOTHBRUSH.get(), stack) > 0;
    }

    /**
     * 附魔台永远不出现它；宝箱的"随机附魔书"抽奖（enchant_with_levels 途径）也被禁用。
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    /**
     * 关键的一刀：原版战利品函数 enchant_randomly 筛候选附魔时，对"书"物品只看这里——
     * 返回 false 后，朱红碎击的附魔书不会从任何战利品宝箱（含村庄地牢里"书+随机附魔"的
     * 池子）刷出来。获取途径只剩合成（粗暴碎击书 + 钻石 + 回响碎片 + 红石），
     * 村民交易走 isTradeable 判定，不受影响、照常保留。
     */
    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
