package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * "OK治疗" 附魔：法术类附魔，可以附魔在任何头盔上（与治疗祷言同槽位）。
 * 佩戴后按 X 打开法术菜单选中"OK治疗"、按 Z 释放：消耗 90 TP 起（随使用次数下降，最低 75，
 * 戴荆棘指环还能减半），召唤 1 枚治疗光球围绕自身 30 秒（同时最多 9 枚），
 * 期间每 3 秒恢复 [玩家最大生命值 × 0.25 + 治疗量]；对准空气右键可把 1 枚光球朝视线方向射出，
 * 命中实体后为其恢复 [实体最大生命值 × 0.65 + 治疗量] 并附带 30 秒 1 级抗性提升。
 * 每使用 1 次治疗量永久 +2、TP 消耗永久 -1（两者都在使用 15 次后封顶）。
 * 获取途径：工作台合成（治疗祷言附魔书 + 紫色染料 + 骨粉 + 书与笔）；
 * 附魔台、战利品宝箱、村民交易全部不出现。
 */
public final class OkHealEnchantment extends Enchantment {
    public OkHealEnchantment() {
        // ARMOR_HEAD 类别天然覆盖所有"头盔"物品（铁/金/链甲/钻/下界合金/龟壳），
        // 铁砧"书合装备"的兼容性检查走 canEnchant，所以不需要额外的铁砧分支。
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 附魔书可以携带这个附魔（合成产物、铁砧"书合书"都按这个放行）
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return true;
        }
        return super.canEnchant(stack);
    }

    /** 附魔台永远不出现它（"不可刷新在任何地方"）。 */
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
