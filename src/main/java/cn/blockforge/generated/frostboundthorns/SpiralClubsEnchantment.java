package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * "螺旋桃击" 附魔：法术类附魔，只能附魔在锄头上（或附魔书本身）。
 * 手持带该附魔的锄头按 X 打开法术菜单选中"螺旋桃击"，按 Z 对着 32 格内看向的生物释放：
 * 消耗 16 TP，以该生物为圆心、约 4.5 格外圈，顺时针每游戏刻悬停生成 1 枚桃弹（共 12 枚），
 * 全部生成后再等 8 游戏刻，12 枚桃弹一齐朝目标飞掠，碰到的生物受 8 点伤害后消散。
 * 暂不做生存获取途径（创造模式标签页里给一本用于测试）。
 */
public final class SpiralClubsEnchantment extends Enchantment {
    private static final EnchantmentCategory CATEGORY = EnchantmentCategory.create(
            "spiral_clubs", item -> item instanceof HoeItem);

    public SpiralClubsEnchantment() {
        super(Rarity.RARE, CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 附魔书可以携带这个附魔（铁砧"书合书"按这个放行）
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return true;
        }
        // 实际附到物品上时，只允许锄头
        return stack.getItem() instanceof HoeItem;
    }

    /** 附魔台永远不出现它（法术附魔一律不进附魔台）。 */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    /** 暂不做生存获取：不进村民交易、不进随机附魔书抽奖。 */
    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
