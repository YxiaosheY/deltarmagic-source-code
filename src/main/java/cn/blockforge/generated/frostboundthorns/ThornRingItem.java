package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.item.Item;

/**
 * 荆棘指环：Curios 戒指槽的指环。佩戴时每秒自伤 1 点（不会扣死，最低保留半颗心），
 * 作为补偿——佩戴它期间，所有法术释放所需的 TP 一律减半（雪葬 200→100、粗暴碎击 50→25 等）。
 */
public final class ThornRingItem extends Item {
    public ThornRingItem() {
        super(new Item.Properties().stacksTo(1));
    }
}
