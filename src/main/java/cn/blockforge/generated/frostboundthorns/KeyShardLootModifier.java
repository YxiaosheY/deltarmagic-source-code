package cn.blockforge.generated.frostboundthorns;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * 钥匙碎片 a / b / c 的宝箱来源：任何原版宝箱（minecraft:chests/*）开箱时，
 * 三片碎片各自独立掷 15% 概率，命中就往战利品里加一片。
 * 与「牙刷附魔书」用同一套全局战利品修正器机制（见 global_loot_modifiers.json）。
 */
public final class KeyShardLootModifier extends LootModifier {
    /** 每片碎片的独立刷新概率：15%。 */
    private static final float CHANCE = 0.15F;

    public static final Codec<KeyShardLootModifier> CODEC = RecordCodecBuilder.create(inst ->
            codecStart(inst).apply(inst, KeyShardLootModifier::new));

    private KeyShardLootModifier(LootItemCondition[] conditionsIn) {
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
        if (random.nextFloat() < CHANCE) {
            generatedLoot.add(new ItemStack(GeneratedMod.KEY_SHARD_A.get()));
        }
        if (random.nextFloat() < CHANCE) {
            generatedLoot.add(new ItemStack(GeneratedMod.KEY_SHARD_B.get()));
        }
        if (random.nextFloat() < CHANCE) {
            generatedLoot.add(new ItemStack(GeneratedMod.KEY_SHARD_C.get()));
        }
        return generatedLoot;
    }
}
