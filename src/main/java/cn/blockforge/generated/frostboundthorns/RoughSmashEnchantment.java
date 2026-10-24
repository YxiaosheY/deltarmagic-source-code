package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * "粗暴碎击" 附魔：普通（非宝藏）附魔，附魔书可出现在村民交易与战利品宝箱中。
 * 只能附魔在已经附魔"牙刷"的斧头上；附魔后按 X 打开菜单选中"粗暴碎击"，再按 Z 释放。
 */
public final class RoughSmashEnchantment extends Enchantment {
    private static final EnchantmentCategory CATEGORY = EnchantmentCategory.create(
            "rough_smash", item -> item instanceof AxeItem);

    public RoughSmashEnchantment() {
        super(Rarity.RARE, CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 附魔书本身可以携带这个附魔（宝箱里"附魔书+随机附魔"的战利品池按这个判定放行）
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return true;
        }
        // 但实际附到物品上时，只能附魔在附魔"牙刷"的斧头上
        return stack.getItem() instanceof AxeItem
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.TOOTHBRUSH.get(), stack) > 0;
    }

    /**
     * 只对附魔书放行：宝箱里用 enchant_with_levels 直接生成"附魔书"的池子也能抽到粗暴碎击；
     * 附魔台的槽位根本放不进附魔书，而斧头/剑等真实物品返回 false，
     * 所以玩家永远无法在附魔台里刷出这个附魔（获取途径：战利品宝箱 + 村民交易）。
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
