package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.item.Item;

/**
 * 雪花指环：与荆棘指环同为 Curios 戒指槽的指环，但没有"每秒自伤 1 点"的副作用，
 * 也不会触发法术 TP 减半（减半只认荆棘指环）。
 * 它是「冰震术」「睡眠喷雾」「雪葬」的载体之一（雪葬在雪花指环上需要 200 TP，
 * 超过 TP 上限 100，所以必须再戴一枚荆棘指环把消耗减半到 100 才放得出来）。
 */
public final class SnowflakeRingItem extends Item {
    public SnowflakeRingItem() {
        super(new Item.Properties().stacksTo(1));
    }
}
