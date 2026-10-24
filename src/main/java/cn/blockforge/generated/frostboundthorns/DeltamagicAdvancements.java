package cn.blockforge.generated.frostboundthorns;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * deltamagic 进度页用的三个自定义触发器（原版触发器覆盖不到的判定点）：
 * <ul>
 *   <li>{@link SpellCastTrigger}：成功释放某个法术（睡眠喷雾等）时触发；</li>
 *   <li>{@link SnowGraveKillTrigger}：生物被雪葬的跳伤打死时触发（可按死者类型过滤）；</li>
 *   <li>{@link GrindstoneDisenchantTrigger}：在砂轮里把某个附魔真正磨掉时触发。</li>
 * </ul>
 * 触发器在模组构造期注册（{@link #initialize()}），进度 JSON 里用
 * {@code deltamagic:spell_cast / deltamagic:snow_grave_kill / deltamagic:grindstone_disenchant} 引用。
 */
public final class DeltamagicAdvancements {

    /** 法术释放触发器（conditions: {"spell": "deltamagic:sleep_spray"}，缺省则任意法术）。 */
    public static final SpellCastTrigger SPELL_CAST = new SpellCastTrigger();
    /** 雪葬击杀触发器（conditions: {"victim": {"type": "minecraft:chicken"}}，缺省则任意生物）。 */
    public static final SnowGraveKillTrigger SNOW_GRAVE_KILL = new SnowGraveKillTrigger();
    /** 砂轮祛魔触发器（conditions: {"enchantment": "deltamagic:snow_burial"}，缺省则任意附魔）。 */
    public static final GrindstoneDisenchantTrigger GRINDSTONE_DISENCHANT = new GrindstoneDisenchantTrigger();

    private static boolean initialized;

    /** 把三个自定义触发器登记进原版触发器表（只需一次，在模组构造期调用）。 */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        CriteriaTriggers.register(SPELL_CAST);
        CriteriaTriggers.register(SNOW_GRAVE_KILL);
        CriteriaTriggers.register(GRINDSTONE_DISENCHANT);
    }

    private static ResourceLocation readLocation(JsonObject json, String key) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return new ResourceLocation(json.get(key).getAsString());
        }
        return null;
    }

    /** 附魔在注册表里的 ID（砂轮判定时用来和 JSON 里的字符串比对）。 */
    public static ResourceLocation enchantmentId(Enchantment enchantment) {
        return BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
    }

    // ------------------------------------------------------------------

    /** 成功施放法术：{@code trigger(player, 法术ID)}。 */
    public static final class SpellCastTrigger extends SimpleCriterionTrigger<SpellCastTrigger.Instance> {
        public static final ResourceLocation ID =
                new ResourceLocation(GeneratedMod.MOD_ID, "spell_cast");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        protected Instance createInstance(JsonObject json, ContextAwarePredicate player,
                DeserializationContext context) {
            return new Instance(player, readLocation(json, "spell"));
        }

        public void cast(ServerPlayer player, ResourceLocation spell) {
            trigger(player, instance -> instance.matches(spell));
        }

        static final class Instance extends AbstractCriterionTriggerInstance {
            private final ResourceLocation spell;

            Instance(ContextAwarePredicate player, ResourceLocation spell) {
                super(ID, player);
                this.spell = spell;
            }

            boolean matches(ResourceLocation cast) {
                return spell == null || spell.equals(cast);
            }

            @Override
            public JsonObject serializeToJson(SerializationContext context) {
                JsonObject json = super.serializeToJson(context);
                if (spell != null) {
                    json.addProperty("spell", spell.toString());
                }
                return json;
            }
        }
    }

    // ------------------------------------------------------------------

    /** 被雪葬跳伤击杀：{@code trigger(caster, 死者)}，JSON 里可按原版实体谓词过滤死者。 */
    public static final class SnowGraveKillTrigger extends SimpleCriterionTrigger<SnowGraveKillTrigger.Instance> {
        public static final ResourceLocation ID =
                new ResourceLocation(GeneratedMod.MOD_ID, "snow_grave_kill");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        protected Instance createInstance(JsonObject json, ContextAwarePredicate player,
                DeserializationContext context) {
            ContextAwarePredicate victim = json.has("victim")
                    ? EntityPredicate.fromJson(json, "victim", context)
                    : null;
            return new Instance(player, victim);
        }

        public void kill(ServerPlayer caster, Entity victim) {
            trigger(caster, instance -> instance.matches(caster, victim));
        }

        static final class Instance extends AbstractCriterionTriggerInstance {
            private final ContextAwarePredicate victim;

            Instance(ContextAwarePredicate player, ContextAwarePredicate victim) {
                super(ID, player);
                this.victim = victim;
            }

            boolean matches(ServerPlayer caster, Entity killed) {
                return victim == null
                        || victim.matches(EntityPredicate.createContext(caster, killed));
            }

            @Override
            public JsonObject serializeToJson(SerializationContext context) {
                JsonObject json = super.serializeToJson(context);
                if (victim != null) {
                    json.add("victim", victim.toJson(context));
                }
                return json;
            }
        }
    }

    // ------------------------------------------------------------------

    /** 砂轮祛魔：{@code trigger(player, 被磨掉的附魔)}。 */
    public static final class GrindstoneDisenchantTrigger
            extends SimpleCriterionTrigger<GrindstoneDisenchantTrigger.Instance> {
        public static final ResourceLocation ID =
                new ResourceLocation(GeneratedMod.MOD_ID, "grindstone_disenchant");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        protected Instance createInstance(JsonObject json, ContextAwarePredicate player,
                DeserializationContext context) {
            return new Instance(player, readLocation(json, "enchantment"));
        }

        public void disenchant(ServerPlayer player, Enchantment enchantment) {
            ResourceLocation id = enchantmentId(enchantment);
            trigger(player, instance -> instance.matches(id));
        }

        static final class Instance extends AbstractCriterionTriggerInstance {
            private final ResourceLocation enchantment;

            Instance(ContextAwarePredicate player, ResourceLocation enchantment) {
                super(ID, player);
                this.enchantment = enchantment;
            }

            boolean matches(ResourceLocation ground) {
                return enchantment == null || enchantment.equals(ground);
            }

            @Override
            public JsonObject serializeToJson(SerializationContext context) {
                JsonObject json = super.serializeToJson(context);
                if (enchantment != null) {
                    json.addProperty("enchantment", enchantment.toString());
                }
                return json;
            }
        }
    }

    private DeltamagicAdvancements() {
    }
}
