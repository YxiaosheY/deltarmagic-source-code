package cn.blockforge.generated.frostboundthorns;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;

/**
 * 「丑魔锭斧头升级」锻造配方：模板槽放下界合金升级模板、
 * 底板槽放任意斧头、材料槽放丑魔锭 → 得到同一把斧头，但：
 * 1. 攻击力在原有基础上 +8（不是把伤害设成 8：1.20.1 里往 NBT 写自定义属性后，
 *    武器自带的默认属性会被整表顶掉，所以升级时还要把斧头自身的基础攻击 /
 *    攻击速度一并复制进 NBT 属性列表，详见 {@link #copyDefaultMainhandModifiers}；
 *    tooltip 与实战伤害自动生效，粗暴碎击的弹伤 / 弹体大小按实际攻击力计算，
 *    所以也一并变强）；
 * 2. 打上 NBT 标记，释放粗暴碎击时 TP 消耗从 50 降到 40（见 ModEvents）；
 * 3. tooltip 追加两行说明。
 * 已升级过的斧头不能再升级（matches 直接拦掉，不浪费材料）。
 */
public final class JevilIngotSmithingRecipe implements SmithingRecipe {
    /** 升级标记所在的 NBT 键。 */
    public static final String TAG_JEVIL_UPGRADED = "DeltamagicJevilUpgraded";
    /** 原版存放属性修饰符列表的 NBT 键（ItemStack#getAttributeModifiers 读的就是它）。 */
    private static final String TAG_ATTRIBUTE_MODIFIERS = "AttributeModifiers";
    /** 攻击加成数值。 */
    private static final double ATTACK_BONUS = 8.0D;
    /** 粗暴碎击在升级斧头上的 TP 消耗（提示文案用，实际判定见 ModEvents）。 */
    private static final int ROUGH_SMASH_TP_COST_UPGRADED = 40;
    /** 固定 UUID：同一把斧头即使被数据复制也只会有一个加成，不会叠层。 */
    private static final UUID UPGRADE_UUID =
            UUID.fromString("3f9c0e2a-8b7d-4f16-9a5e-2c7d81e4b603");

    private final ResourceLocation id;
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    public JevilIngotSmithingRecipe(ResourceLocation id, Ingredient template, Ingredient base, Ingredient addition) {
        this.id = id;
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    /** 斧头是否已被丑魔锭升级过。 */
    public static boolean isJevilUpgraded(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag().getBoolean(TAG_JEVIL_UPGRADED);
    }

    /** 给斧头盖上升级：标记 + 保留斧头自带基础属性 + 攻击力 +8 + 两行 tooltip 说明。 */
    public static void applyUpgrade(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getBoolean(TAG_JEVIL_UPGRADED)) {
            return; // 幂等：重复调用不叠加
        }
        tag.putBoolean(TAG_JEVIL_UPGRADED, true);
        // 必须先把斧头自带的默认主手属性（基础攻击 / 攻击速度）复制进 NBT 列表，
        // 再写 +8——否则 1.20.1 下 NBT 属性表会整表顶掉默认属性，升级后所有斧头
        // 的攻击力都只剩"徒手 1 + 8"，等于把伤害设成 8 而不是在原基础上加 8。
        copyDefaultMainhandModifiers(stack);
        stack.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(UPGRADE_UUID, "Jevil ingot upgrade",
                        ATTACK_BONUS, AttributeModifier.Operation.ADDITION),
                EquipmentSlot.MAINHAND);
        appendLore(stack, Component.literal("*在原有攻击力基础上 +8*")
                .withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE).withItalic(true)));
        appendLore(stack, Component.literal("*Jevil 的力量*；粗暴碎击 TP 消耗降至 "
                + ROUGH_SMASH_TP_COST_UPGRADED)
                .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true)));
    }

    /**
     * 旧 jar 做出来的升级斧头自愈：那批物品缺了应复制进 NBT 的默认属性，
     * 攻击力被"顶"成了只剩 +8。补一份即可，按 UUID 去重、幂等可反复调用
     * （ModEvents 的背包巡检每 20 刻对升级斧头调一次）。
     */
    public static void ensureBaseModifiers(ItemStack stack) {
        if (isJevilUpgraded(stack)) {
            copyDefaultMainhandModifiers(stack);
        }
    }

    /**
     * 把物品自身默认的主手属性修饰符逐条写进 NBT 的 AttributeModifiers 列表，
     * 已存在的同 UUID 条目跳过（去重 + 幂等）。
     */
    private static void copyDefaultMainhandModifiers(ItemStack stack) {
        Set<UUID> owned = collectNbtModifierIds(stack);
        Multimap<Attribute, AttributeModifier> defaults =
                stack.getItem().getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
        for (Map.Entry<Attribute, AttributeModifier> entry : defaults.entries()) {
            if (owned.add(entry.getValue().getId())) {
                stack.addAttributeModifier(entry.getKey(), entry.getValue(), EquipmentSlot.MAINHAND);
            }
        }
    }

    /** 收集 NBT AttributeModifiers 列表里已有的修饰符 UUID。 */
    private static Set<UUID> collectNbtModifierIds(ItemStack stack) {
        Set<UUID> ids = new HashSet<>();
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_ATTRIBUTE_MODIFIERS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_ATTRIBUTE_MODIFIERS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                AttributeModifier modifier = AttributeModifier.load(list.getCompound(i));
                if (modifier != null) {
                    ids.add(modifier.getId());
                }
            }
        }
        return ids;
    }

    /** 往 display.Lore 末尾追加一行（原版 tooltip 的灰色小字区）。 */
    private static void appendLore(ItemStack stack, Component line) {
        CompoundTag display = stack.getOrCreateTagElement("display");
        ListTag lore;
        if (display.get("Lore") instanceof ListTag existing) {
            lore = existing;
        } else {
            lore = new ListTag();
            display.put("Lore", lore);
        }
        lore.add(StringTag.valueOf(Component.Serializer.toJson(line)));
    }

    // ------------------------------------------------------------------
    // SmithingRecipe / Recipe 接口
    // 锻造台输入槽顺序：0=模板 1=底板 2=材料
    // ------------------------------------------------------------------
    @Override
    public boolean matches(Container container, Level level) {
        ItemStack baseStack = container.getItem(1);
        return this.template.test(container.getItem(0))
                && this.base.test(baseStack)
                && this.addition.test(container.getItem(2))
                && !isJevilUpgraded(baseStack); // 已升级的斧头不再吃第二次材料
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registries) {
        ItemStack output = container.getItem(1).copy();
        applyUpgrade(output);
        return output;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    public Ingredient getBase() {
        return this.base;
    }

    public Ingredient getAddition() {
        return this.addition;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        // 输出是"进来的那把斧头"，这里取底板配方的第一件物品做代表（配方书 / 统计展示用）
        ItemStack[] candidates = this.base.getItems();
        return candidates.length > 0 ? candidates[0].copy() : ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return GeneratedMod.JEVIL_INGOT_SMITHING_SERIALIZER.get();
    }

    /** JSON / 网络序列化器：template、base、addition 三份 Ingredient。 */
    public static final class Serializer implements RecipeSerializer<JevilIngotSmithingRecipe> {
        @Override
        public JevilIngotSmithingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new JevilIngotSmithingRecipe(id,
                    Ingredient.fromJson(json.get("template")),
                    Ingredient.fromJson(json.get("base")),
                    Ingredient.fromJson(json.get("addition")));
        }

        @Override
        public JevilIngotSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new JevilIngotSmithingRecipe(id,
                    Ingredient.fromNetwork(buf),
                    Ingredient.fromNetwork(buf),
                    Ingredient.fromNetwork(buf));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, JevilIngotSmithingRecipe recipe) {
            recipe.template.toNetwork(buf);
            recipe.base.toNetwork(buf);
            recipe.addition.toNetwork(buf);
        }
    }
}
