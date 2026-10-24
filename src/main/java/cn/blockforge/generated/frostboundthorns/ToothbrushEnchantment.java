package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * "牙刷" 附魔：宝藏附魔。
 * 不进附魔台、不进村民交易、也不进宝箱的随机附魔抽奖，
 * 只通过 {@link ToothbrushBookLootModifier} 以 10% 的概率替换宝箱里的附魔书。
 */
public final class ToothbrushEnchantment extends Enchantment {
    private static final EnchantmentCategory CATEGORY = EnchantmentCategory.create(
            "toothbrush", item -> item instanceof AxeItem);

    public ToothbrushEnchantment() {
        super(Rarity.RARE, CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    /** 宝藏附魔。 */
    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    /** 村民（图书管理员）不卖这个附魔书。 */
    @Override
    public boolean isTradeable() {
        return false;
    }

    /** 不参与任何随机附魔抽奖，出现途径只有宝箱 10% 修正。 */
    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 附魔书可以作为载体（原版"书合书"），但最终只能附到斧头上
        return stack.is(Items.ENCHANTED_BOOK) || stack.getItem() instanceof AxeItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
