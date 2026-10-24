package cn.blockforge.generated.frostboundthorns;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * "牙刷"宝藏附魔书的战利品来源：
 * 任何原版宝箱（minecraft:chests/*）生成出来的附魔书，有 10% 的概率被替换成
 * 携带"牙刷"附魔的附魔书。除此之外没有其他获取途径（不进附魔台 / 村民 / 随机抽奖）。
 */
public final class ToothbrushBookLootModifier extends LootModifier {
    /** 替换概率：10%。 */
    private static final float CHANCE = 0.10F;

    public static final Codec<ToothbrushBookLootModifier> CODEC = RecordCodecBuilder.create(inst ->
            codecStart(inst).apply(inst, ToothbrushBookLootModifier::new));

    private ToothbrushBookLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation tableId = context.getQueriedLootTableId();
        // 只处理原版宝箱战利品表（例如 minecraft:chests/simple_dungeon）
        if (tableId == null || !"minecraft".equals(tableId.getNamespace())
                || !tableId.getPath().startsWith("chests/")) {
            return generatedLoot;
        }
        RandomSource random = context.getRandom();
        for (int i = 0; i < generatedLoot.size(); ++i) {
            ItemStack stack = generatedLoot.get(i);
            if (stack.is(Items.ENCHANTED_BOOK) && random.nextFloat() < CHANCE) {
                generatedLoot.set(i, EnchantedBookItem.createForEnchantment(
                        new EnchantmentInstance(GeneratedMod.TOOTHBRUSH.get(), 1)));
            }
        }
        return generatedLoot;
    }
}
