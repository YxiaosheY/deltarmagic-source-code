package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * "冰震术" 附魔：法术类附魔，可以附魔在「雪花指环」或「荆棘指环」上（或附魔书本身）。
 * 佩戴后按 X 打开法术菜单选中"冰震术"，按 Z 对着 32 格内看向的生物释放：
 * 目标周身顺时针亮起 8 片小雪花围成一圈，中央凝出一片大雪花，随后雪花环炸开、
 * 大雪花膨胀渐隐，目标受到无视护甲的魔法伤害并被追加冻结值。
 * 获取途径：战利品宝箱（5% 专属池 + 随机附魔书抽奖）与村民交易；附魔台不出现。
 */
public final class IceShockEnchantment extends Enchantment {
    private static final EnchantmentCategory CATEGORY = EnchantmentCategory.create(
            "ice_shock", item -> item == GeneratedMod.THORN_RING.get()
                    || item == GeneratedMod.SNOWFLAKE_RING.get());

    public IceShockEnchantment() {
        super(Rarity.RARE, CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 附魔书可以携带这个附魔（宝箱掉落、铁砧"书合书"都按这个放行）
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

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
