package cn.blockforge.generated.frostboundthorns;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ModEvents {
    public static final ModEvents INSTANCE = new ModEvents();

    private static final int CHARGE_TICKS = 20;
    private static final int EFFECT_TICKS = 100;
    private static final int COOLDOWN_TICKS = 2000;
    /** 雪葬：每 0.5 秒（10 游戏刻）结算一次伤害。 */
    private static final int SNOW_BURIAL_INTERVAL = 10;
    /** 雪葬：每次结算造成的伤害值。 */
    private static final float SNOW_BURIAL_DAMAGE = 20.0F;
    /** 雪葬：结算时把生物冻结值写成的数值（原版 140 = 完全冻结阈值）。 */
    private static final int SNOW_BURIAL_TICKS_FROZEN = 140;
    private static final double EFFECT_RADIUS = 2.0D;
    private static final int PARTICLES_PER_TICK = 36;
    /**
     * TP 上限固定 100 点。雪葬的基础消耗 200 高于上限是刻意的限制：
     * 单靠攒 TP 永远到不了 200，只有佩戴荆棘指环把消耗减半为 100 时才能攒满释放。
     */
    private static final int TP_MAX = 100;
    /** 雪葬：每次释放消耗的基础 TP（高于 TP_MAX；佩戴荆棘指环减半为 100，见 {@link #thornCost}）。 */
    private static final int TP_SNOW_BURIAL_COST = 200;
    private static final int TP_PASSIVE_TICKS = 100;
    private static final int TP_SHIELD_TICKS = 20;
    private static final int TP_PASSIVE_GAIN = 1;
    /** 举盾时每秒（20 游戏刻）回复 5 点 TP。 */
    private static final int TP_SHIELD_GAIN = 5;
    /** 盾牌成功格挡一次"有源伤害"（能追溯到来源实体的攻击）时回复的 TP。 */
    private static final int TP_SHIELD_BLOCK_GAIN = 16;
    /** 粗暴碎击：每次释放消耗的基础 TP（佩戴荆棘指环时减半，见 {@link #thornCost}）。 */
    private static final int TP_ROUGH_SMASH_COST = 50;
    /** 粗暴碎击：斧头用丑魔锭升级过后，每次释放只消耗这么多 TP。 */
    private static final int TP_ROUGH_SMASH_COST_JEVIL = 40;
    /** 朱红碎击：每次释放消耗的基础 TP（比粗暴碎击更强的进阶法术；佩戴荆棘指环时减半）。 */
    private static final int TP_CRIMSON_SMASH_COST = 60;
    /** 粗暴碎击：投射物伤害 = 斧头攻击力 × 5。 */
    private static final double ROUGH_SMASH_DAMAGE_MULT = 5.0D;
    /** 粗暴碎击：投射物大小 = 斧头攻击力 ÷ 5（保留一位小数）。 */
    private static final double ROUGH_SMASH_SCALE_DIVISOR = 5.0D;
    /** 朱红碎击：投射物伤害 = 斧头攻击力 × 6.5。 */
    private static final double CRIMSON_SMASH_DAMAGE_MULT = 6.5D;
    /** 朱红碎击：投射物大小 = 斧头攻击力 ÷ 4.5（保留一位小数）。 */
    private static final double CRIMSON_SMASH_SCALE_DIVISOR = 4.5D;
    /** 冰震术：每次释放消耗的基础 TP（佩戴荆棘指环时减半，见 {@link #thornCost}）。 */
    private static final int TP_ICE_SHOCK_COST = 16;
    /** 治疗祷言：每次释放消耗的基础 TP（佩戴荆棘指环时减半，见 {@link #thornCost}）。 */
    private static final int TP_HEAL_PRAYER_COST = 32;
    /** 治疗祷言：恢复量 = 护甲值 × 该系数（点生命值）。 */
    private static final float HEAL_PRAYER_ARMOR_MULT = 0.8F;
    /** 治疗祷言：头顶绿色烟花粒子的数量（"少许"）。 */
    private static final int HEAL_PRAYER_PARTICLE_COUNT = 14;
    /** 治疗祷言：准星瞄准生物时，目标恢复量 = 该生物最大生命值 × 该系数。 */
    private static final float HEAL_PRAYER_TARGET_MULT = 0.75F;
    /** 治疗祷言：准星瞄准生物的搜索距离（格），与冰震术的瞄准口径一致。 */
    private static final double HEAL_PRAYER_AIM_RANGE = 32.0D;
    /** 睡眠喷雾：每次释放消耗的基础 TP（佩戴荆棘指环时减半，见 {@link #thornCost}）。 */
    private static final int TP_SLEEP_SPRAY_COST = 32;
    /** 睡眠喷雾：准星瞄准生物的搜索距离（格），与冰震术的瞄准口径一致。 */
    private static final double SLEEP_SPRAY_AIM_RANGE = 32.0D;
    /** 睡眠喷雾：三朵雾气依次出现的间隔刻数（第 0 / 6 / 12 刻各放一朵）。 */
    private static final int SLEEP_SPRAY_MIST_INTERVAL = 6;
    /** 睡眠喷雾：一次释放放出的雾气朵数。 */
    private static final int SLEEP_SPRAY_MIST_COUNT = 3;
    /** 睡眠喷雾：每朵雾气的生效刻数（1 秒 = 20 刻，与客户端粒子寿命一致）。 */
    private static final int SLEEP_SPRAY_MIST_LIFETIME = 20;
    /** 睡眠喷雾：整个法术的结束刻数 = 最后一朵的出生刻(12) + 寿命(20)。 */
    private static final int SLEEP_SPRAY_END_TICK =
            (SLEEP_SPRAY_MIST_COUNT - 1) * SLEEP_SPRAY_MIST_INTERVAL + SLEEP_SPRAY_MIST_LIFETIME;
    /** 睡眠喷雾：雾气水平覆盖半径（格）。左右、前后各 1.5 格 → 合起来宽 3 格。 */
    private static final double SLEEP_SPRAY_MIST_HALF_WIDTH = 1.5D;
    /** 睡眠喷雾：雾气垂直覆盖半径（格）。上下各 1 格 → 合起来高 2 格。 */
    private static final double SLEEP_SPRAY_MIST_HALF_HEIGHT = 1.0D;
    /** 睡眠喷雾：雾块水平方向每层铺的列数偏移（-1 / 0 / +1 列，列间距 1 格，配合雾片大小正好盖满宽 3 格）。 */
    private static final int[] SLEEP_SPRAY_MIST_GRID_OFFSETS = {-1, 0, 1};
    /** 睡眠喷雾：雾块竖直方向的层偏移（米/格，±0.5 两层，配合雾片大小盖满高 2 格）。 */
    private static final double[] SLEEP_SPRAY_MIST_LAYERS = {-0.5D, 0.5D};
    /** 睡眠喷雾：雾片位置的随机抖动幅度（格），让雾块边缘自然、不像贴了张方纸。 */
    private static final double SLEEP_SPRAY_MIST_JITTER = 0.18D;
    /** 睡眠喷雾：雾气覆盖内的生物每游戏刻获得的 Tiredness。 */
    private static final int SLEEP_SPRAY_TIREDNESS_PER_TICK = 3;
    /** 螺旋桃击 / 方钻上冲：每次释放消耗的基础 TP（佩戴荆棘指环时减半，见 {@link #thornCost}）。 */
    private static final int TP_JEVIL_SPELL_COST = 16;
    /** 螺旋桃击 / 方钻上冲：准星瞄准生物的搜索距离（格），与其它法术口径一致。 */
    private static final double JEVIL_SPELL_AIM_RANGE = 32.0D;
    /** 螺旋桃击：外圈桃弹数量（每游戏刻生成 1 枚，共 12 刻）。 */
    private static final int SPIRAL_CLUB_COUNT = 12;
    /** 螺旋桃击：桃弹环绕目标的半径（格）。 */
    private static final double SPIRAL_CLUB_RADIUS = 4.5D;
    /** 螺旋桃击：最后一枚桃弹出生后，再等多少游戏刻全体起飞。 */
    private static final int SPIRAL_CLUB_FLY_WAIT = 8;
    /** 螺旋桃击：起飞时刻 = 第 11 刻生成完最后 1 枚 + 再等 8 刻 = 第 19 刻。 */
    private static final int SPIRAL_CLUB_FLY_TICK = SPIRAL_CLUB_COUNT - 1 + SPIRAL_CLUB_FLY_WAIT;
    /** 螺旋桃击：桃弹命中伤害。 */
    private static final float SPIRAL_CLUB_DAMAGE = 8.0F;
    /** 方钻上冲：以目标为圆心的生效半径（格）。 */
    private static final double DIAMOND_UPPERCUT_RADIUS = 5.0D;
    /** 方钻上冲：持续时间 20 秒 = 400 游戏刻。 */
    private static final int DIAMOND_UPPERCUT_DURATION = 400;
    /** 方钻上冲：每 3 游戏刻从地面钻出 1 枚方钻弹（20 秒共约 134 枚）。 */
    private static final int DIAMOND_UPPERCUT_INTERVAL = 3;
    /** 方钻上冲：方钻弹命中伤害。 */
    private static final float DIAMOND_UPPERCUT_DAMAGE = 5.0F;
    /** 恶魔镰刀：每次释放消耗的基础 TP（比桃击 / 上冲更贵一档；佩戴荆棘指环时减半）。 */
    private static final int TP_JEVIL_SCYTHE_COST = 32;
    /** 恶魔镰刀：一次召唤的镰刀数量（四象限各 1 柄）。 */
    private static final int JEVIL_SCYTHE_COUNT = 4;
    /** 恶魔镰刀：镰刀命中伤害。 */
    private static final float JEVIL_SCYTHE_DAMAGE = 25.0F;
    /** 恶魔镰刀：整场持续 15 秒 = 300 游戏刻（与 SpellBullet 的镰刀寿命一致），期间不允许叠放。 */
    private static final int JEVIL_SCYTHE_DURATION = 300;
    /** OK治疗：首次释放的基础 TP 消耗（每使用 1 次永久 -1，最低 {@link #OK_HEAL_COST_FLOOR}；戴荆棘指环在最终值上再减半）。 */
    private static final int TP_OK_HEAL_COST = 90;
    /** OK治疗：TP 消耗成长到底的数值（使用 15 次后 90-15=75，之后停止成长）。 */
    private static final int OK_HEAL_COST_FLOOR = 75;
    /** OK治疗：成长封顶的使用次数（15 次后治疗量与 TP 消耗都不再变化）。 */
    private static final int OK_HEAL_MAX_GROWTH_USES = 15;
    /** OK治疗：每次使用永久增加的治疗量（点）。 */
    private static final int OK_HEAL_BONUS_PER_USE = 2;
    /** OK治疗：同时存在的环绕光球上限（枚）。 */
    public static final int OK_HEAL_MAX_ORBS = 9;
    /** OK治疗：光环治疗间隔，3 秒 = 60 游戏刻。 */
    private static final int OK_HEAL_AURA_INTERVAL = 60;
    /** OK治疗：光环自疗量 = 玩家最大生命值 × 该系数 + 治疗量。 */
    private static final float OK_HEAL_SELF_MULT = 0.25F;
    /** OK治疗：玩家永久存档里记录累计使用次数的 NBT 键。 */
    private static final String NBT_OK_HEAL_USES = "OkHealUses";
    /** 冰震术：施法者佩戴荆棘指环时消耗减半（全局规则，见 {@link #thornCost}）。 */
    /** 荆棘指环：佩戴副作用每秒造成的损失。 */
    private static final float THORN_RING_DAMAGE = 1.0F;
    /** 荆棘指环：佩戴副作用允许把玩家扣到的最低血量（1.0 = 半颗心，即"保留 1 滴血"，永远不会把自己扣死）。 */
    private static final float THORN_RING_MIN_HEALTH = 1.0F;
    /** 「紧迫一点」在原版宝箱战利品表里的出现概率。 */
    private static final float TENSION_BIT_CHANCE = 0.50F;
    /** 「紧迫宝石」在原版宝箱战利品表里的出现概率。 */
    private static final float TENSION_GEM_CHANCE = 0.25F;
    /** 「紧迫满点」在原版宝箱战利品表里的出现概率。 */
    private static final float TENSION_FULL_CHANCE = 0.05F;
    /** 冰震术：第 0~7 刻每刻顺时针在目标周围生成 1 片小雪花，共 8 片成环。 */
    private static final int ICE_SHOCK_RING_COUNT = 8;
    /** 冰震术：第 8 刻在雪花环中央生成 1 片大雪花。 */
    private static final int ICE_SHOCK_CENTER_TICK = 8;
    /** 冰震术：第 9 刻结算伤害；同一刻环上雪花散开、中央雪花膨胀渐隐。 */
    private static final int ICE_SHOCK_DAMAGE_TICK = 9;
    /** 冰震术：第 10 刻清理施法者状态（粒子在客户端自行播完）。 */
    private static final int ICE_SHOCK_END_TICK = 10;
    /** 冰震术：雪花环半径（格）。 */
    private static final double ICE_SHOCK_RING_RADIUS = 0.85D;
    /** 冰震术：常规无视护甲伤害。 */
    private static final float ICE_SHOCK_DAMAGE = 15.0F;
    /** 冰震术：目标冻结值达标后的强化无视护甲伤害（替代常规伤害）。 */
    private static final float ICE_SHOCK_FROZEN_DAMAGE = 30.0F;
    /** 冰震术：每次释放给生物 TicksFrozen 追加的量。 */
    private static final int ICE_SHOCK_FROZEN_GAIN = 40;
    /** 冰震术：追加后 TicksFrozen 达到该阈值，本次伤害提升为 30。 */
    private static final int ICE_SHOCK_FROZEN_THRESHOLD = 130;
    /** 「继续前进」触发窗口：1 分钟 = 1200 游戏刻内数释放次数。 */
    private static final int KEEP_GOING_WINDOW_TICKS = 1200;
    /** 「继续前进」触发所需的最少释放次数（达到后每次再放都续上）。 */
    private static final int KEEP_GOING_CASTS_REQUIRED = 3;
    /** 「继续前进」buff 持续时间：30 秒 = 600 游戏刻（再次达成条件时重置回满）。 */
    private static final int KEEP_GOING_DURATION_TICKS = 600;
    private static final UUID TOOTHBRUSH_SPEED_MODIFIER_ID = UUID.fromString("7b1c3b6b-7c4a-4b9e-9b49-1f8c25f0d1b2");

    private final Map<UUID, Boolean> grindstoneSnowBurialState = new HashMap<>();
    private final Map<UUID, Integer> tpValues = new HashMap<>();
    private final Map<UUID, Integer> tpPassiveTimers = new HashMap<>();
    private final Map<UUID, Integer> tpShieldTimers = new HashMap<>();
    private final Map<UUID, Integer> lastSyncedTp = new HashMap<>();
    private final Map<UUID, Integer> lastSyncedSpells = new HashMap<>();

    @SubscribeEvent
    public void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (event.getSlotType() != net.minecraft.world.entity.EquipmentSlot.MAINHAND
                || !(stack.getItem() instanceof AxeItem)
                || EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.TOOTHBRUSH.get(), stack) <= 0) {
            return;
        }

        float axeDamage = ((AxeItem) stack.getItem()).getAttackDamage();
        double speedBonus = Math.min(1.0D, Math.max(0.0D, axeDamage / 10.0D));
        event.addModifier(Attributes.ATTACK_SPEED,
                new AttributeModifier(TOOTHBRUSH_SPEED_MODIFIER_ID, "Toothbrush attack speed", speedBonus,
                        AttributeModifier.Operation.ADDITION));
    }
    private final Map<UUID, ChargeState> chargingPlayers = new HashMap<>();
    private final Map<UUID, SnowBurialEffect> activeEffects = new HashMap<>();
    private final Map<UUID, IceShockEffect> activeIceShock = new HashMap<>();
    private final Map<UUID, SleepSprayEffect> activeSleepSpray = new HashMap<>();
    /** 螺旋桃击吟唱中：每刻沿外圈补生 1 枚桃弹（生满 12 枚后本条目即结束）。 */
    private final Map<UUID, SpiralClubsEffect> activeSpiralClubs = new HashMap<>();
    /** 方钻上冲生效中：20 秒内在半径 5 格的圆面上持续钻出方钻弹。 */
    private final Map<UUID, DiamondUppercutEffect> activeDiamondUppercut = new HashMap<>();
    /** 恶魔镰刀冷却中：UUID → 镰刀场结束的世界游戏刻（15 秒内不允许叠放；镰刀弹自己会活满寿命）。 */
    private final Map<UUID, Long> jevilScytheUntil = new HashMap<>();
    /** OK治疗光环计时器：UUID → 距上一次 3 秒自疗已走的刻数（只在有环绕光球时结算）。 */
    private final Map<UUID, Integer> okHealAuraTimers = new HashMap<>();
    /** OK治疗射球去重：UUID → 上一次受理射球请求的世界游戏刻（同一刻并包进来只射一枚）。 */
    private final Map<UUID, Long> okHealFireTicks = new HashMap<>();
    /** 冰震术成功释放的时刻（世界游戏刻，升序队列），供「继续前进」按 1 分钟滑动窗口计数。 */
    private final Map<UUID, java.util.ArrayDeque<Long>> iceShockCastTimes = new HashMap<>();

    public static void handleSnowBurialInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleInput(player);
        }
    }

    public static void handleRoughSmashInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleRoughSmash(player);
        }
    }

    public static void handleCrimsonSmashInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleCrimsonSmash(player);
        }
    }

    public static void handleIceShockInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleIceShock(player);
        }
    }

    public static void handleHealPrayerInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleHealPrayer(player);
        }
    }

    public static void handleSleepSprayInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleSleepSpray(player);
        }
    }

    public static void handleSpiralClubsInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleSpiralClubs(player);
        }
    }

    public static void handleDiamondUppercutInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleDiamondUppercut(player);
        }
    }

    public static void handleJevilScytheInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleJevilScythe(player);
        }
    }

    public static void handleOkHealInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleOkHeal(player);
        }
    }

    /** 客户端"对准空气右键"事件转来的包：把一枚环绕中的治疗光球沿视线射出。 */
    public static void handleOkHealFireInput(ServerPlayer player, boolean pressed) {
        if (pressed) {
            INSTANCE.handleOkHealFire(player);
        }
    }

    @SubscribeEvent
    public void onAnvilRepair(AnvilRepairEvent event) {
        // 创造模式下铁砧会跳过原版兼容性检查（生存里红色禁止标志照常生效）。
        // 所以在"取出成品"这一刻把不该存在的附魔强制剥掉：
        // 牙刷只允许挂在斧头（或附魔书上），粗暴碎击 / 朱红碎击只允许挂在带牙刷的斧头（或附魔书上）。
        stripIllegalEnchantments(event.getOutput());
        if (hasSnowBurial(event.getOutput()) && !hasSnowBurial(event.getLeft())) {
            playSoundForPlayer(event.getEntity(), GeneratedMod.OMINOUS_SOUND.get(), 1.0F, 0.9F);
        }
    }

    /**
     * 从"物品"角度强制附魔归属：牙刷只能出现在斧头（或附魔书本身）上，
     * 粗暴碎击 / 朱红碎击只能出现在已带牙刷的斧头（或附魔书本身）上。不合法就地移除该附魔，
     * 其余附魔原样保留。覆盖 /enchant 指令、创造铁砧等一切绕过界面的途径。
     */
    private static void stripIllegalEnchantments(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()
                || !stack.getOrCreateTag().contains("Enchantments")) {
            return;
        }
        int toothbrush = EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.TOOTHBRUSH.get(), stack);
        int roughSmash = EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.ROUGH_SMASH.get(), stack);
        int crimsonSmash = EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.CRIMSON_SMASH.get(), stack);
        int iceShock = EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.ICE_SHOCK.get(), stack);
        int healPrayer = EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.HEAL_PRAYER.get(), stack);
        int sleepSpray = EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.SLEEP_SPRAY.get(), stack);
        int spiralClubs = EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.SPIRAL_CLUBS.get(), stack);
        int diamondUppercut = EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.DIAMOND_UPPERCUT.get(), stack);
        int jevilScythe = EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.JEVIL_SCYTHE.get(), stack);
        int okHeal = EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.OK_HEAL.get(), stack);
        if (toothbrush <= 0 && roughSmash <= 0 && crimsonSmash <= 0 && iceShock <= 0 && healPrayer <= 0
                && sleepSpray <= 0 && spiralClubs <= 0 && diamondUppercut <= 0 && jevilScythe <= 0
                && okHeal <= 0) {
            return;
        }
        boolean onAxe = stack.getItem() instanceof AxeItem;
        boolean onBook = stack.is(Items.ENCHANTED_BOOK);
        boolean onRing = stack.is(GeneratedMod.THORN_RING.get()) || stack.is(GeneratedMod.SNOWFLAKE_RING.get());
        // 附魔书可以作为载体（书合书是原版行为），但最终物品必须是斧头
        boolean badToothbrush = toothbrush > 0 && !onAxe && !onBook;
        boolean badRoughSmash = roughSmash > 0
                && !onBook && !(onAxe && (toothbrush > 0 || hasToothbrush(stack)));
        boolean badCrimsonSmash = crimsonSmash > 0
                && !onBook && !(onAxe && (toothbrush > 0 || hasToothbrush(stack)));
        // 冰震术只允许出现在两种指环（或附魔书本身）上
        boolean badIceShock = iceShock > 0 && !onBook && !onRing;
        // 治疗祷言只允许出现在头盔上（或附魔书本身）
        boolean onHelmet = stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor
                && armor.getEquipmentSlot() == EquipmentSlot.HEAD;
        boolean badHealPrayer = healPrayer > 0 && !onBook && !onHelmet;
        // OK治疗与治疗祷言同槽位：只允许出现在头盔上（或附魔书本身）
        boolean badOkHeal = okHeal > 0 && !onBook && !onHelmet;
        // 睡眠喷雾只允许出现在两种指环（或附魔书本身）上
        boolean badSleepSpray = sleepSpray > 0 && !onBook && !onRing;
        // 螺旋桃击 / 方钻上冲 / 恶魔镰刀只允许出现在锄头上（或附魔书本身）
        boolean onHoe = stack.getItem() instanceof HoeItem;
        boolean badSpiralClubs = spiralClubs > 0 && !onBook && !onHoe;
        boolean badDiamondUppercut = diamondUppercut > 0 && !onBook && !onHoe;
        boolean badJevilScythe = jevilScythe > 0 && !onBook && !onHoe;
        if (!badToothbrush && !badRoughSmash && !badCrimsonSmash && !badIceShock && !badHealPrayer
                && !badSleepSpray && !badSpiralClubs && !badDiamondUppercut && !badJevilScythe
                && !badOkHeal) {
            return;
        }
        Map<net.minecraft.world.item.enchantment.Enchantment, Integer> clean =
                new java.util.LinkedHashMap<>(EnchantmentHelper.getEnchantments(stack));
        if (badToothbrush) {
            clean.remove(GeneratedMod.TOOTHBRUSH.get());
        }
        if (badRoughSmash) {
            clean.remove(GeneratedMod.ROUGH_SMASH.get());
        }
        if (badCrimsonSmash) {
            clean.remove(GeneratedMod.CRIMSON_SMASH.get());
        }
        if (badIceShock) {
            clean.remove(GeneratedMod.ICE_SHOCK.get());
        }
        if (badHealPrayer) {
            clean.remove(GeneratedMod.HEAL_PRAYER.get());
        }
        if (badOkHeal) {
            clean.remove(GeneratedMod.OK_HEAL.get());
        }
        if (badSleepSpray) {
            clean.remove(GeneratedMod.SLEEP_SPRAY.get());
        }
        if (badSpiralClubs) {
            clean.remove(GeneratedMod.SPIRAL_CLUBS.get());
        }
        if (badDiamondUppercut) {
            clean.remove(GeneratedMod.DIAMOND_UPPERCUT.get());
        }
        if (badJevilScythe) {
            clean.remove(GeneratedMod.JEVIL_SCYTHE.get());
        }
        EnchantmentHelper.setEnchantments(clean, stack);
    }

    /** 定期清洗玩家背包（兜住 /enchant 指令、创造复制等一切来源），每 20 游戏刻一次。 */
    private static void sanitizePlayerInventory(ServerPlayer player) {
        net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            stripIllegalEnchantments(stack);
            // 旧 jar 升级出来的丑魔锭斧头缺自带基础属性（攻击力被顶成只剩 +8），逐件补齐
            JevilIngotSmithingRecipe.ensureBaseModifiers(stack);
        }
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        if ((event.getLeft().is(GeneratedMod.THORN_RING.get())
                        || event.getLeft().is(GeneratedMod.SNOWFLAKE_RING.get()))
                && (event.getRight().is(GeneratedMod.SNOW_BURIAL_BOOK.get())
                        || bookGrants(event.getRight(), GeneratedMod.SNOW_BURIAL.get()))
                && !hasSnowBurial(event.getLeft())) {
            // 雪葬：荆棘指环与雪花指环都可以附（基础消耗 200 高于 TP 上限 100，
            // 只有佩戴荆棘指环减半为 100 才攒得满、放得出）
            ItemStack output = event.getLeft().copy();
            output.enchant(GeneratedMod.SNOW_BURIAL.get(), 1);
            event.setOutput(output);
            event.setCost(12);
            event.setMaterialCost(1);
        } else if (event.getLeft().getItem() instanceof AxeItem
                && bookGrants(event.getRight(), GeneratedMod.TOOTHBRUSH.get())
                && !hasToothbrush(event.getLeft())) {
            ItemStack output = event.getLeft().copy();
            output.enchant(GeneratedMod.TOOTHBRUSH.get(), 1);
            event.setOutput(output);
            event.setCost(12);
            event.setMaterialCost(1);
        } else if (event.getLeft().getItem() instanceof AxeItem
                && bookGrants(event.getRight(), GeneratedMod.ROUGH_SMASH.get())
                && hasToothbrush(event.getLeft())
                && !hasRoughSmash(event.getLeft())) {
            // 粗暴碎击只能附魔在附魔"牙刷"的斧头上
            ItemStack output = event.getLeft().copy();
            output.enchant(GeneratedMod.ROUGH_SMASH.get(), 1);
            event.setOutput(output);
            event.setCost(12);
            event.setMaterialCost(1);
        } else if (event.getLeft().getItem() instanceof AxeItem
                && bookGrants(event.getRight(), GeneratedMod.CRIMSON_SMASH.get())
                && hasToothbrush(event.getLeft())
                && !hasCrimsonSmash(event.getLeft())) {
            // 朱红碎击同样只能附魔在附魔"牙刷"的斧头上
            ItemStack output = event.getLeft().copy();
            output.enchant(GeneratedMod.CRIMSON_SMASH.get(), 1);
            event.setOutput(output);
            event.setCost(12);
            event.setMaterialCost(1);
        } else if ((event.getLeft().is(GeneratedMod.THORN_RING.get())
                        || event.getLeft().is(GeneratedMod.SNOWFLAKE_RING.get()))
                && bookGrants(event.getRight(), GeneratedMod.ICE_SHOCK.get())
                && !hasIceShock(event.getLeft())) {
            // 冰震术：雪花指环与荆棘指环都可以附
            ItemStack output = event.getLeft().copy();
            output.enchant(GeneratedMod.ICE_SHOCK.get(), 1);
            event.setOutput(output);
            event.setCost(12);
            event.setMaterialCost(1);
        } else if ((event.getLeft().is(GeneratedMod.THORN_RING.get())
                        || event.getLeft().is(GeneratedMod.SNOWFLAKE_RING.get()))
                && bookGrants(event.getRight(), GeneratedMod.SLEEP_SPRAY.get())
                && !hasSleepSpray(event.getLeft())) {
            // 睡眠喷雾：雪花指环与荆棘指环都可以附
            ItemStack output = event.getLeft().copy();
            output.enchant(GeneratedMod.SLEEP_SPRAY.get(), 1);
            event.setOutput(output);
            event.setCost(12);
            event.setMaterialCost(1);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        ServerPlayer player = (ServerPlayer) event.player;
        if (player.tickCount % 20 == 0) {
            // 只有荆棘指环带"每秒掉 1 点血"的副作用，雪花指环没有
            findEquippedRing(player).filter(ring -> ring.is(GeneratedMod.THORN_RING.get()))
                    .ifPresent(ring -> hurtForThornRing(player));
        }

        tickCharge(player);
        tickSnowBurial(player);
        tickIceShock(player);
        tickSleepSpray(player);
        tickSpiralClubs(player);
        tickDiamondUppercut(player);
        tickOkHealAura(player);
        tickGrindstone(player);
        tickTp(player);
        // 玩家站进 jevil_room 内部时放出 Jevil（每间房只触发一次，内部按 10 刻自查节流）
        JevilRoomManager.onPlayerTick(player);
        // 每 20 游戏刻清洗一次背包：把出现在非斧头物品上的牙刷/粗暴碎击剥掉
        if (player.tickCount % 20 == 0) {
            sanitizePlayerInventory(player);
        }
        if (player.tickCount % 5 == 0) {
            syncTpToClient(player);
        }
    }

    /**
     * 荆棘指环佩戴自伤：每秒造成 {@link #THORN_RING_DAMAGE} 点损失，但绝不致命——
     * 伤害会被截断到"当前血量 - {@link #THORN_RING_MIN_HEALTH}"，血少时最多保留 1 滴血。
     * 伤害源也从原来的"世界之外"（带 bypasses_invulnerability 标签，无视血量直接处死、
     * 不死图腾也拦不住，正是这个 bug 的根源）换成普通魔法伤害：可被正常死亡保护兜住。
     * 创造/旁观模式与已死亡状态跳过（生存玩家受伤后短暂无敌帧内这一下会自动落空）。
     */
    private static void hurtForThornRing(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator() || !player.isAlive()) {
            return;
        }
        float damage = Math.min(THORN_RING_DAMAGE, player.getHealth() - THORN_RING_MIN_HEALTH);
        if (damage > 0.0F) {
            player.hurt(player.damageSources().magic(), damage);
        }
    }

    /**
     * TP 值或可用法术有变化时推给客户端，驱动左侧 TP 条与顶部法术菜单。
     * 每 5 tick 调用一次，切换手持物 / 戴卸戒指后菜单内容会很快刷新。
     */
    private void syncTpToClient(ServerPlayer player) {
        UUID id = player.getUUID();
        int tp = tpValues.getOrDefault(id, 0);
        int spells = computeSpellBits(player);
        Integer lastTp = lastSyncedTp.get(id);
        Integer lastSpells = lastSyncedSpells.get(id);
        if (lastTp == null || lastTp != tp || lastSpells == null || lastSpells != spells) {
            lastSyncedTp.put(id, tp);
            lastSyncedSpells.put(id, spells);
            NetworkHandler.syncTp(player, tp, spells);
        }
    }

    /**
     * 计算玩家当前可释放的法术位图（与服务端真正能施法的条件一致）：
     * 粗暴碎击 / 朱红碎击 = 主手持带对应附魔的斧头；雪葬 = 戒指槽佩戴带该附魔的荆棘 / 雪花指环。
     */
    public static int computeSpellBits(LivingEntity player) {
        int bits = 0;
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof AxeItem && hasRoughSmash(main)) {
            bits |= SpellMenuState.SPELL_ROUGH_SMASH;
        }
        if (main.getItem() instanceof AxeItem && hasCrimsonSmash(main)) {
            bits |= SpellMenuState.SPELL_CRIMSON_SMASH;
        }
        if (findEquippedRing(player).filter(ModEvents::hasSnowBurial).isPresent()) {
            bits |= SpellMenuState.SPELL_SNOW_BURIAL;
        }
        if (findEquippedRing(player).filter(ModEvents::hasIceShock).isPresent()) {
            bits |= SpellMenuState.SPELL_ICE_SHOCK;
        }
        if (findEquippedRing(player).filter(ModEvents::hasSleepSpray).isPresent()) {
            bits |= SpellMenuState.SPELL_SLEEP_SPRAY;
        }
        if (hasHealPrayer(player.getItemBySlot(EquipmentSlot.HEAD))) {
            bits |= SpellMenuState.SPELL_HEAL_PRAYER;
        }
        if (hasOkHeal(player.getItemBySlot(EquipmentSlot.HEAD))) {
            bits |= SpellMenuState.SPELL_OK_HEAL;
        }
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof HoeItem && hasSpiralClubs(mainHand)) {
            bits |= SpellMenuState.SPELL_SPIRAL_CLUBS;
        }
        if (mainHand.getItem() instanceof HoeItem && hasDiamondUppercut(mainHand)) {
            bits |= SpellMenuState.SPELL_DIAMOND_UPPERCUT;
        }
        if (mainHand.getItem() instanceof HoeItem && hasJevilScythe(mainHand)) {
            bits |= SpellMenuState.SPELL_JEVIL_SCYTHE;
        }
        return bits;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Patchouli 是可选兼容：没安装时门控方法立即返回，不会触碰 Patchouli 类。
            PatchouliGate.onPlayerLoggedIn(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        chargingPlayers.remove(id);
        activeEffects.remove(id);
        activeIceShock.remove(id);
        activeSleepSpray.remove(id);
        activeSpiralClubs.remove(id);
        activeDiamondUppercut.remove(id);
        jevilScytheUntil.remove(id);
        okHealAuraTimers.remove(id);
        okHealFireTicks.remove(id);
        iceShockCastTimes.remove(id);
        grindstoneSnowBurialState.remove(id);
        tpValues.remove(id);
        tpPassiveTimers.remove(id);
        tpShieldTimers.remove(id);
        lastSyncedTp.remove(id);
        lastSyncedSpells.remove(id);
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();
        if (id.getNamespace().equals("minecraft") && id.getPath().startsWith("chests/")) {
            LootPool pool = LootPool.lootPool()
                    .name("deltamagic_snow_burial_treasure")
                    .when(LootItemRandomChanceCondition.randomChance(0.05F))
                    .add(LootItem.lootTableItem(GeneratedMod.SNOW_BURIAL_BOOK.get())
                            .apply(new SetEnchantmentsFunction.Builder()
                                    .withEnchantment(GeneratedMod.SNOW_BURIAL.get(), ConstantValue.exactly(1.0F))))
                    .setRolls(ConstantValue.exactly(1.0F))
                    .build();
            event.getTable().addPool(pool);
            // 「紧迫」系列 TP 补给：三个物品各自独立判定，一个宝箱里可能同时出现多个。
            event.getTable().addPool(tensionPool(GeneratedMod.TENSION_BIT.get(),
                    "deltamagic_tension_bit", TENSION_BIT_CHANCE));
            event.getTable().addPool(tensionPool(GeneratedMod.TENSION_GEM.get(),
                    "deltamagic_tension_gem", TENSION_GEM_CHANCE));
            event.getTable().addPool(tensionPool(GeneratedMod.TENSION_FULL.get(),
                    "deltamagic_tension_full", TENSION_FULL_CHANCE));
            // 睡眠喷雾附魔书：5% 概率刷进任何原版战利品宝箱（isDiscoverable=false，
            // 不进原版随机附魔书抽奖，出现概率完全由这个专属池控制）
            event.getTable().addPool(LootPool.lootPool()
                    .name("deltamagic_sleep_spray_treasure")
                    .when(LootItemRandomChanceCondition.randomChance(0.05F))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                            .apply(new SetEnchantmentsFunction.Builder()
                                    .withEnchantment(GeneratedMod.SLEEP_SPRAY.get(), ConstantValue.exactly(1.0F))))
                    .setRolls(ConstantValue.exactly(1.0F))
                    .build());
            // 牙刷：宝藏附魔，由 ToothbrushBookLootModifier 以 10% 概率替换宝箱里的附魔书。
            // 粗暴碎击：普通附魔，随原版"随机附魔书"抽奖刷进战利品宝箱（isDiscoverable + canEnchant 附魔书放行），
            // 村民交易由 isTradeable 自动纳入。
            // 朱红碎击：isDiscoverable=false，任何战利品宝箱都刷不出它；靠合成配方获取
            // （粗暴碎击书 + 钻石 + 回响碎片 + 红石，见 recipes/crimson_smash_book.json），村民交易渠道保留。
            // 冰震术：与粗暴碎击同路——isDiscoverable + canEnchant 对附魔书放行，
            // 随原版"随机附魔书"抽奖刷进战利品宝箱；附魔台由 canApplyAtEnchantingTable=false 挡掉。
        }
    }

    /** 生成一个"按概率出 1 个指定物品"的独立战利品池，供「紧迫」系列挂进原版宝箱表。 */
    private static LootPool tensionPool(net.minecraft.world.item.Item item, String name, float chance) {
        return LootPool.lootPool()
                .name(name)
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(LootItem.lootTableItem(item))
                .setRolls(ConstantValue.exactly(1.0F))
                .build();
    }

    private void handleInput(ServerPlayer player) {
        UUID id = player.getUUID();
        Optional<ItemStack> ring = findEquippedRing(player).filter(ModEvents::hasSnowBurial);
        if (chargingPlayers.containsKey(id)
                || activeEffects.containsKey(id)
                || ring.isEmpty()
                || player.getCooldowns().isOnCooldown(ring.get().getItem())) {
            return;
        }
        int tp = tpValues.getOrDefault(id, 0);
        // 雪葬基础消耗 200 TP（TP 上限 100）；佩戴荆棘指环减半为 100，攒满即可释放
        int cost = thornCost(player, TP_SNOW_BURIAL_COST);
        if (tp < cost) {
            showActionBar(player, "TP 不足（" + tp + "/" + cost + "），无法释放雪葬");
            return;
        }
        chargingPlayers.put(id, new ChargeState());
        playSoundForPlayer(player, GeneratedMod.SNOWGRAVE_4_SOUND.get(), 1.0F, 1.0F);
    }

    private void handleRoughSmash(ServerPlayer player) {
        ItemStack axe = player.getMainHandItem();
        if (!(axe.getItem() instanceof AxeItem) || !hasRoughSmash(axe)) {
            return;
        }
        UUID id = player.getUUID();
        int tp = tpValues.getOrDefault(id, 0);
        // 丑魔锭升级过的斧头释放粗暴碎击只要 40 TP；再戴荆棘指环还能继续减半
        int base = JevilIngotSmithingRecipe.isJevilUpgraded(axe)
                ? TP_ROUGH_SMASH_COST_JEVIL : TP_ROUGH_SMASH_COST;
        int cost = thornCost(player, base);
        if (tp < cost) {
            showActionBar(player, "TP 不足（" + tp + "/" + cost + "），无法释放粗暴碎击");
            return;
        }
        tpValues.put(id, tp - cost);
        syncTpToClient(player);
        double axeDamage = roughSmashAxeDamage(axe);
        float damage = (float) (axeDamage * ROUGH_SMASH_DAMAGE_MULT);
        // 大小 = 斧头伤害 / 5，四舍五入保留一位小数；下限 0.2 保证任何斧头都看得见弹幕
        float scale = Math.max(0.2F, Math.round(axeDamage / ROUGH_SMASH_SCALE_DIVISOR * 10.0F) / 10.0F);
        spawnSmash(player, damage, scale, false);
    }

    private void handleCrimsonSmash(ServerPlayer player) {
        ItemStack axe = player.getMainHandItem();
        if (!(axe.getItem() instanceof AxeItem) || !hasCrimsonSmash(axe)) {
            return;
        }
        UUID id = player.getUUID();
        int tp = tpValues.getOrDefault(id, 0);
        int cost = thornCost(player, TP_CRIMSON_SMASH_COST);
        if (tp < cost) {
            showActionBar(player, "TP 不足（" + tp + "/" + cost + "），无法释放朱红碎击");
            return;
        }
        tpValues.put(id, tp - cost);
        syncTpToClient(player);
        double axeDamage = roughSmashAxeDamage(axe);
        float damage = (float) (axeDamage * CRIMSON_SMASH_DAMAGE_MULT);
        // 大小 = 斧头伤害 / 4.5，四舍五入保留一位小数；下限 0.2 保证任何斧头都看得见弹幕
        float scale = Math.max(0.2F, Math.round(axeDamage / CRIMSON_SMASH_SCALE_DIVISOR * 10.0F) / 10.0F);
        spawnSmash(player, damage, scale, true);
    }

    /**
     * 准星选靶：替代原版 ProjectileUtil.getHitResultOnViewVector。
     * 此前各处判定都额外要求 living.isPickable()，而末影龙的 isPickable() 恒为 false，
     * 于是这类"游戏规定不可拾取选中"的生物永远锁不上目标；且末影龙的主碰撞盒只罩住
     * 躯干，头、尾、翅膀都是分体。这里沿视线自己做几何求交：不再看 isPickable，
     * 末影龙按分体逐个判定，方块遮挡保留（视线被方块挡住的距离外依然选不中）。
     */
    private <T extends LivingEntity> T findTargetOnView(ServerPlayer player, double range, Class<T> type) {
        Level level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        if (look.lengthSqr() < 1.0E-6) {
            return null;
        }
        look = look.normalize();
        Vec3 end = eye.add(look.scale(range));
        double maxDistSq = range * range;
        // 视线先撞到的方块位置为限：和原版一样不允许隔墙选靶
        HitResult blockHit = level.clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            end = blockHit.getLocation();
            maxDistSq = Math.max(eye.distanceToSqr(end), 1.0E-4);
        }
        // 候选搜索框：沿视线方向铺开并加宽 12 格。末影龙的头/尾/翅能伸出主碰撞盒十几格，
        // 只按主盒检索会把整条龙漏掉；是否真被瞄准仍以射线求交为准，加宽不会误选
        AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(12.0);
        T best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (Entity entity : level.getEntities(player, search, type::isInstance)) {
            if (!(entity instanceof LivingEntity living) || living == player || !living.isAlive()) {
                continue;
            }
            Vec3 hitPos = clipEntityAgainstRay(living, eye, end);
            if (hitPos == null) {
                continue;
            }
            double distSq = eye.distanceToSqr(hitPos);
            if (distSq <= maxDistSq && distSq < bestDistSq) {
                bestDistSq = distSq;
                best = type.cast(living);
            }
        }
        return best;
    }

    /** 射线与生物求交，返回最近命中点；末影龙一类的多分体生物按头、颈、躯干、尾、翅逐个分体判定。 */
    private static Vec3 clipEntityAgainstRay(LivingEntity entity, Vec3 from, Vec3 to) {
        if (entity instanceof EnderDragon dragon) {
            Vec3 closest = null;
            double closestDistSq = Double.MAX_VALUE;
            for (EnderDragonPart part : dragon.getSubEntities()) {
                Vec3 hit = part.getBoundingBox().inflate(part.getPickRadius()).clip(from, to).orElse(null);
                if (hit != null) {
                    double distSq = from.distanceToSqr(hit);
                    if (distSq < closestDistSq) {
                        closestDistSq = distSq;
                        closest = hit;
                    }
                }
            }
            if (closest != null) {
                return closest;
            }
        }
        return entity.getBoundingBox().inflate(entity.getPickRadius()).clip(from, to).orElse(null);
    }

    /**
     * 释放冰震术：需佩戴带该附魔的指环；基础耗 16 TP，戴荆棘指环减半为 8 TP；
     * 必须看向 32 格内的生物才起效（没目标不扣 TP）。
     */
    private void handleIceShock(ServerPlayer player) {
        java.util.Optional<ItemStack> ring = findEquippedRing(player).filter(ModEvents::hasIceShock);
        if (ring.isEmpty()) {
            return;
        }
        UUID id = player.getUUID();
        if (activeIceShock.containsKey(id)) {
            return; // 上一轮雪花环还没绽开，不允许叠着放
        }
        int cost = thornCost(player, TP_ICE_SHOCK_COST);
        int tp = tpValues.getOrDefault(id, 0);
        if (tp < cost) {
            showActionBar(player, "TP 不足（" + tp + "/" + cost + "），无法释放冰震术");
            return;
        }
        LivingEntity target = findTargetOnView(player, 32.0D, LivingEntity.class);
        if (target == null) {
            showActionBar(player, "没有看向生物，无法释放冰震术");
            return;
        }
        tpValues.put(id, tp - cost);
        syncTpToClient(player);
        ServerLevel level = (ServerLevel) player.level();
        // 吟唱音效 snd_icespell.ogg（世界声源，附近玩家都能听到）
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                GeneratedMod.ICE_SPELL_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        activeIceShock.put(id, new IceShockEffect(level, target.getId()));
        recordIceShockCast(player);
    }

    /**
     * 记录一次成功的冰震术释放（扣了 TP、锁定了目标才算），并按 1 分钟滑动窗口检查
     * 「继续前进」：窗口内攒够 3 次（含本 1 分钟内第 4、5……次）就给玩家挂上 / 续满该 buff。
     */
    private void recordIceShockCast(ServerPlayer player) {
        long now = player.level().getGameTime();
        java.util.ArrayDeque<Long> casts =
                iceShockCastTimes.computeIfAbsent(player.getUUID(), key -> new java.util.ArrayDeque<>());
        casts.addLast(now);
        while (!casts.isEmpty() && now - casts.peekFirst() > KEEP_GOING_WINDOW_TICKS) {
            casts.pollFirst(); // 滑出 1 分钟窗口的旧记录不再计数
        }
        if (casts.size() >= KEEP_GOING_CASTS_REQUIRED) {
            // addEffect 对同一效果取"剩余时间更长者"，所以每次达成条件都把 buff 重置回满 30 秒
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    GeneratedMod.KEEP_GOING.get(), KEEP_GOING_DURATION_TICKS, 0, false, true));
        }
    }

    /**
     * 释放治疗祷言：需佩戴带该附魔的头盔；消耗 32 TP（戴荆棘指环减半）。
     * 若十字准星瞄准着 32 格内的生物（非自身的活体），则改为治疗该生物：
     * 恢复 [目标最大生命值 × 0.75] 点生命值；未瞄准任何生物时执行原本的效果：
     * 给自己恢复 [护甲值 × 0.8] 点生命值。
     * 两种情况都在被治疗者头顶抛下少许绿色烟花粒子并播放 snd_power_1.ogg。
     * TP 不足、恢复量为 0（护甲值为 0）或目标/自身已满血时不扣 TP，只在动作栏提示。
     */
    private void handleHealPrayer(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!hasHealPrayer(helmet)) {
            return;
        }
        UUID id = player.getUUID();
        int tp = tpValues.getOrDefault(id, 0);
        int cost = thornCost(player, TP_HEAL_PRAYER_COST);
        if (tp < cost) {
            showActionBar(player, "TP 不足（" + tp + "/" + cost + "），无法释放治疗祷言");
            return;
        }
        // 准星优先：瞄到生物就治它（搜索距离与判定口径与冰震术一致），没瞄到才治自己
        LivingEntity aimed = findTargetOnView(player, HEAL_PRAYER_AIM_RANGE, LivingEntity.class);
        if (aimed != null) {
            handleHealPrayerOnTarget(player, aimed, tp, cost);
            return;
        }
        float healAmount = player.getArmorValue() * HEAL_PRAYER_ARMOR_MULT;
        if (healAmount <= 0.0F) {
            showActionBar(player, "护甲值为 0，无法释放治疗祷言");
            return;
        }
        if (player.getHealth() >= player.getMaxHealth()) {
            showActionBar(player, "生命值已满，无法释放治疗祷言");
            return;
        }
        tpValues.put(id, tp - cost);
        syncTpToClient(player);
        player.heal(healAmount);
        GreenFlash.trigger(player);
        ServerLevel level = (ServerLevel) player.level();
        playHealPrayerChant(level, player);
        spawnHealPrayerSparks(level, player);
    }

    /**
     * 治疗祷言的"瞄准生物"分支：消耗治疗祷言 TP（戴荆棘指环减半），给准星瞄到的生物恢复
     * [其最大生命值 × 0.75] 点生命（原版 heal 会自动封顶到最大生命值），
     * 绿色烟花粒子从该生物头顶抛下。目标已满血时不扣 TP，只在动作栏提示。
     */
    private void handleHealPrayerOnTarget(ServerPlayer player, LivingEntity target, int tp, int cost) {
        if (target.getHealth() >= target.getMaxHealth()) {
            showActionBar(player, "瞄准的生物生命值已满，无法释放治疗祷言");
            return;
        }
        tpValues.put(player.getUUID(), tp - cost);
        syncTpToClient(player);
        target.heal(target.getMaxHealth() * HEAL_PRAYER_TARGET_MULT);
        GreenFlash.trigger(target);
        ServerLevel level = (ServerLevel) player.level();
        // 吟唱声仍在玩家处响起（是玩家在施法），粒子落在被治疗的生物头顶
        playHealPrayerChant(level, player);
        spawnHealPrayerSparks(level, target);
    }

    /** 治疗祷言 / OK治疗的吟唱音效 snd_power_1.ogg（世界声源，附近玩家都能听到）。 */
    private static void playHealPrayerChant(ServerLevel level, Entity caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                GeneratedMod.POWER_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 在被治疗者头顶抛下少许绿色烟花粒子：生成点略高于头顶，粒子自带向下初速 + 正重力。 */
    private static void spawnHealPrayerSparks(ServerLevel level, Entity healed) {
        double headTop = healed.getY() + healed.getBbHeight() + 0.7D;
        level.sendParticles(GeneratedMod.HEAL_PRAYER_SPARK.get(),
                healed.getX(), headTop, healed.getZ(),
                HEAL_PRAYER_PARTICLE_COUNT, 0.45D, 0.25D, 0.45D, 0.0D);
    }

    /**
     * 释放 OK治疗：需佩戴带该附魔的头盔；基础耗 90 TP（每使用 1 次永久 -1、最低 75，
     * 戴荆棘指环在最终值上再减半）。每次成功释放召唤 1 枚治疗光球围绕自身 30 秒，
     * 同时最多 9 枚（满员时不扣 TP，只提示）。释放成功即计入永久成长：
     * 治疗量 +2、TP 消耗 -1（使用 15 次后双双封顶）。
     * 动作栏里的法术名随当前消耗变化：90 = 究极治疗、89~76 = OK治疗、75 = 进阶治疗。
     */
    private void handleOkHeal(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!hasOkHeal(helmet)) {
            return;
        }
        UUID id = player.getUUID();
        ServerLevel level = player.serverLevel();
        int orbiting = countOrbitingOrbs(level, player);
        if (orbiting >= OK_HEAL_MAX_ORBS) {
            showActionBar(player, "治疗光球已达上限 " + OK_HEAL_MAX_ORBS + " 枚，无法再召唤");
            return;
        }
        int growth = okHealGrowth(player);
        String spellName = okHealSpellName(TP_OK_HEAL_COST - growth);
        int tp = tpValues.getOrDefault(id, 0);
        int cost = thornCost(player, TP_OK_HEAL_COST - growth);
        if (tp < cost) {
            showActionBar(player, "TP 不足（" + tp + "/" + cost + "），无法释放" + spellName);
            return;
        }
        tpValues.put(id, tp - cost);
        syncTpToClient(player);
        // 先按"本次释放前"的治疗量召唤光球，再记入成长：治疗量 +2、TP 消耗 -1（15 次封顶）
        HealSmallEntity.summon(level, player, orbiting, growth * OK_HEAL_BONUS_PER_USE);
        setOkHealUses(player, getOkHealUses(player) + 1);
        playHealPrayerChant(level, player);
        showActionBar(player, spellName + "！治疗光球 " + (orbiting + 1) + "/" + OK_HEAL_MAX_ORBS
                + "，当前治疗量 +" + (growth * OK_HEAL_BONUS_PER_USE));
    }

    /**
     * 对准空气右键：把最早召唤的一枚仍在环绕的治疗光球沿视线方向射出。
     * 没有环绕中的光球（没施放过 / 全部已射出或过期）时静默忽略，不提示、不扣任何东西。
     * 一次右键只射一枚：客户端已改为"按下边沿"发包（一次物理点击一个包），
     * 这里再按实体 id 取最早召唤的那枚，射出后剩余光球的均分重排由 launch() 内部完成。
     */
    private void handleOkHealFire(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        // 同一游戏刻里并包到达（如连点帧重合、客户端补发）只受理第一次
        long gameTick = level.getGameTime();
        Long last = okHealFireTicks.get(player.getUUID());
        if (last != null && last == gameTick) {
            return;
        }
        okHealFireTicks.put(player.getUUID(), gameTick);
        java.util.List<Entity> orbs = level.getEntities(player, player.getBoundingBox().inflate(8.0D),
                entity -> entity instanceof HealSmallEntity orb && !orb.isLaunched() && orb.isOwnedBy(player));
        if (orbs.isEmpty()) {
            return;
        }
        Entity oldest = orbs.get(0);
        for (Entity orb : orbs) {
            if (orb.getId() < oldest.getId()) {
                oldest = orb;
            }
        }
        ((HealSmallEntity) oldest).launch(player.getLookAngle());
    }

    /**
     * OK治疗光环：每 3 秒（60 刻）检查一次，玩家身边还有环绕光球就自疗
     * [最大生命值 × 0.25 + 治疗量] 点并撒绿色火花；满血时跳过（不浪费节拍，下一秒还会再查）。
     */
    private void tickOkHealAura(ServerPlayer player) {
        UUID id = player.getUUID();
        int timer = okHealAuraTimers.merge(id, 1, Integer::sum);
        if (timer < OK_HEAL_AURA_INTERVAL) {
            return;
        }
        okHealAuraTimers.put(id, 0);
        ServerLevel level = player.serverLevel();
        if (countOrbitingOrbs(level, player) <= 0) {
            return;
        }
        if (player.getHealth() >= player.getMaxHealth()) {
            return;
        }
        player.heal(player.getMaxHealth() * OK_HEAL_SELF_MULT + okHealGrowth(player) * OK_HEAL_BONUS_PER_USE);
        GreenFlash.trigger(player);
        spawnHealPrayerSparks(level, player);
    }

    /** 数一数玩家身边还有多少枚仍在环绕（未射出）的治疗光球。 */
    private static int countOrbitingOrbs(ServerLevel level, ServerPlayer owner) {
        return level.getEntities(owner, owner.getBoundingBox().inflate(8.0D),
                entity -> entity instanceof HealSmallEntity orb && !orb.isLaunched()
                        && orb.isOwnedBy(owner)).size();
    }

    /** OK治疗的永久成长进度：已累计使用次数（15 次封顶，之后治疗量与 TP 消耗都不再变化）。 */
    private static int okHealGrowth(net.minecraft.world.entity.player.Player player) {
        return Math.min(getOkHealUses(player), OK_HEAL_MAX_GROWTH_USES);
    }

    /** 按当前 TP 消耗取法术名：90 = 究极治疗，89~76 = OK治疗，75（及以下）= 进阶治疗。 */
    private static String okHealSpellName(int baseCost) {
        if (baseCost >= TP_OK_HEAL_COST) {
            return "究极治疗";
        }
        if (baseCost > OK_HEAL_COST_FLOOR) {
            return "OK治疗";
        }
        return "进阶治疗";
    }

    /** 玩家的 OK治疗 累计使用次数（存在玩家永久存档里，跨死亡/下线保留）。 */
    private static int getOkHealUses(net.minecraft.world.entity.player.Player player) {
        return player.getPersistentData().getInt(NBT_OK_HEAL_USES);
    }

    private static void setOkHealUses(net.minecraft.world.entity.player.Player player, int uses) {
        player.getPersistentData().putInt(NBT_OK_HEAL_USES, uses);
    }

    /**
     * 释放睡眠喷雾：需佩戴带该附魔的指环；基础耗 32 TP，戴荆棘指环减半为 16 TP；
     * 必须看向 32 格内的生物（只认怪物类，玩家不吃这套）才起效（没目标不扣 TP）。
     * 起效后由 tickSleepSpray 铺三朵雾并做覆盖判定。
     */
    private void handleSleepSpray(ServerPlayer player) {
        java.util.Optional<ItemStack> ring = findEquippedRing(player).filter(ModEvents::hasSleepSpray);
        if (ring.isEmpty()) {
            return;
        }
        UUID id = player.getUUID();
        if (activeSleepSpray.containsKey(id)) {
            return; // 上一轮雾气还没散，不允许叠着放
        }
        int cost = thornCost(player, TP_SLEEP_SPRAY_COST);
        int tp = tpValues.getOrDefault(id, 0);
        if (tp < cost) {
            showActionBar(player, "TP 不足（" + tp + "/" + cost + "），无法释放睡眠喷雾");
            return;
        }
        Mob target = findTargetOnView(player, SLEEP_SPRAY_AIM_RANGE, Mob.class);
        if (target == null) {
            showActionBar(player, "没有看向生物，无法释放睡眠喷雾");
            return;
        }
        tpValues.put(id, tp - cost);
        syncTpToClient(player);
        ServerLevel level = (ServerLevel) player.level();
        // 吟唱音效 snd_spell_pacify.ogg（世界声源，附近玩家都能听到）
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                GeneratedMod.PACIFY_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        activeSleepSpray.put(id, new SleepSprayEffect(level, target.getId()));
        // 进度「打瞌睡」：成功释放一次睡眠喷雾
        DeltamagicAdvancements.SPELL_CAST.cast(player,
                DeltamagicAdvancements.enchantmentId(GeneratedMod.SLEEP_SPRAY.get()));
    }

    /**
     * 睡眠喷雾时间轴：第 0 / 6 / 12 刻在目标身上依次放出三块雾（各自钉在其出生位置），
     * 每块雾是一整片 3×3×2 的雾团（3 列 × 3 行 × 2 层雾片，见 {@link #spawnSleepMistBlock}），
     * 每块存活 20 刻；期间凡处在任一块雾覆盖范围（宽 3 格 × 高 2 格 × 深 3 格的长方体）内的生物，每刻 Tiredness +3
     * （同刻被多块雾盖住也只 +3，不叠）。第 32 刻整个法术收尾。
     */
    private void tickSleepSpray(ServerPlayer caster) {
        UUID id = caster.getUUID();
        SleepSprayEffect effect = activeSleepSpray.get(id);
        if (effect == null) {
            return;
        }
        if (effect.ticksElapsed >= SLEEP_SPRAY_END_TICK || effect.level != caster.level()) {
            activeSleepSpray.remove(id);
            return;
        }
        int tick = effect.ticksElapsed;
        // 锚点：目标活着就跟着它走，死亡/消失后钉在最后一次已知位置把雾放完
        Entity target = effect.level.getEntity(effect.targetId);
        if (target instanceof LivingEntity living && living.isAlive()) {
            effect.anchor = living.position().add(0.0D, living.getBbHeight() * 0.5D + 0.2D, 0.0D);
        }
        if (effect.anchor == null) {
            activeSleepSpray.remove(id); // 理论上不会发生（起效瞬间目标必活着），保险兜底
            return;
        }
        if (tick % SLEEP_SPRAY_MIST_INTERVAL == 0) {
            int slot = tick / SLEEP_SPRAY_MIST_INTERVAL;
            if (slot < SLEEP_SPRAY_MIST_COUNT) {
                effect.mistPos[slot] = effect.anchor;
                spawnSleepMistBlock(effect.level, effect.anchor);
            }
        }
        // 覆盖判定：收集处在任一朵"还活着"的雾覆盖长方体（宽 3 × 高 2 × 深 3 格）内的生物，
        // 去重后每刻 +3 Tiredness（同刻被多朵雾盖住也只 +3，不叠）；
        // 判定按"生物身体中心 ↔ 雾心"的长方体范围计算，避免雾钉在身体高度时把贴地的小生物错判在范围外
        java.util.List<LivingEntity> covered = new java.util.ArrayList<>(4);
        for (int slot = 0; slot < SLEEP_SPRAY_MIST_COUNT; slot++) {
            Vec3 mist = effect.mistPos[slot];
            if (mist == null || tick - slot * SLEEP_SPRAY_MIST_INTERVAL >= SLEEP_SPRAY_MIST_LIFETIME) {
                continue;
            }
            AABB area = new AABB(mist, mist)
                    .inflate(SLEEP_SPRAY_MIST_HALF_WIDTH, SLEEP_SPRAY_MIST_HALF_HEIGHT, SLEEP_SPRAY_MIST_HALF_WIDTH);
            for (Entity entity : effect.level.getEntities((Entity) null, area,
                    candidate -> {
                        if (!(candidate instanceof net.minecraft.world.entity.Mob mob)
                                || !mob.isAlive() || candidate == caster) {
                            return false;
                        }
                        Vec3 center = mob.position().add(0.0D, mob.getBbHeight() * 0.5D, 0.0D);
                        return Math.abs(center.x - mist.x) <= SLEEP_SPRAY_MIST_HALF_WIDTH
                                && Math.abs(center.z - mist.z) <= SLEEP_SPRAY_MIST_HALF_WIDTH
                                && Math.abs(center.y - mist.y) <= SLEEP_SPRAY_MIST_HALF_HEIGHT;
                    })) {
                if (!covered.contains(entity)) {
                    covered.add((LivingEntity) entity);
                }
            }
        }
        for (LivingEntity mob : covered) {
            TirednessManager.add(mob, SLEEP_SPRAY_TIREDNESS_PER_TICK);
        }
        effect.ticksElapsed++;
    }

    /**
     * 在雾心周围铺一整块 3×3×2 的雾：水平 3 列 × 3 行、竖直 2 层，共 18 片雾片，
     * 每片带小幅随机抖动，让雾块边缘自然。粒子位置与覆盖判定共用同一个中心与尺寸，
     * 所以"看到的雾"和"真正起效的范围"完全对齐。
     */
    private static void spawnSleepMistBlock(ServerLevel level, Vec3 center) {
        net.minecraft.util.RandomSource random = level.random;
        for (double layer : SLEEP_SPRAY_MIST_LAYERS) {
            for (int gx : SLEEP_SPRAY_MIST_GRID_OFFSETS) {
                for (int gz : SLEEP_SPRAY_MIST_GRID_OFFSETS) {
                    double x = center.x + gx + (random.nextDouble() - 0.5D) * 2.0D * SLEEP_SPRAY_MIST_JITTER;
                    double y = center.y + layer + (random.nextDouble() - 0.5D) * SLEEP_SPRAY_MIST_JITTER;
                    double z = center.z + gz + (random.nextDouble() - 0.5D) * 2.0D * SLEEP_SPRAY_MIST_JITTER;
                    level.sendParticles(GeneratedMod.SLEEP_MIST.get(), x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    /**
     * 释放螺旋桃击：需主手持带该附魔的锄头；消耗 16 TP（戴荆棘指环减半）；
     * 必须看向 32 格内的生物（只认怪物类，玩家不吃这套）才起效（没目标不扣 TP）。
     * 起效后由 tickSpiralClubs 沿目标外圈顺时针逐刻铺 12 枚悬停桃弹，第 19 刻全体起飞。
     */
    private void handleSpiralClubs(ServerPlayer player) {
        ItemStack hoe = player.getMainHandItem();
        if (!(hoe.getItem() instanceof HoeItem) || !hasSpiralClubs(hoe)) {
            return;
        }
        UUID id = player.getUUID();
        if (activeSpiralClubs.containsKey(id)) {
            return; // 上一圈桃弹还没铺完，不允许叠着放
        }
        int tp = tpValues.getOrDefault(id, 0);
        int cost = thornCost(player, TP_JEVIL_SPELL_COST);
        if (tp < cost) {
            showActionBar(player, "TP 不足（" + tp + "/" + cost + "），无法释放螺旋桃击");
            return;
        }
        Mob target = findTargetOnView(player, JEVIL_SPELL_AIM_RANGE, Mob.class);
        if (target == null) {
            showActionBar(player, "没有看向生物，无法释放螺旋桃击");
            return;
        }
        tpValues.put(id, tp - cost);
        syncTpToClient(player);
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D + 0.2D, 0.0D);
        // 起环角度取"施法者所在的一侧"：第一枚桃弹先出现在面向玩家的那边
        double baseAngle = Math.atan2(player.getZ() - center.z, player.getX() - center.x);
        activeSpiralClubs.put(id, new SpiralClubsEffect(
                (ServerLevel) player.level(), target.getId(), player.getId(), center, baseAngle));
    }

    /**
     * 螺旋桃击时间轴：第 0~11 刻俯视顺时针每刻在目标外圈（半径 4.5）悬停生成 1 枚桃弹，
     * 生成期间圆环跟着活目标走；最后一枚生成于第 11 刻，再等 8 刻，第 19 刻全体朝目标飞去
     * （悬停刻数在出生时就写进各弹自身，起飞时刻天然对齐）。控制器铺满 12 枚即退场，
     * 之后的飞行、命中与消散由桃弹实体自己完成。
     */
    private void tickSpiralClubs(ServerPlayer caster) {
        UUID id = caster.getUUID();
        SpiralClubsEffect effect = activeSpiralClubs.get(id);
        if (effect == null) {
            return;
        }
        if (effect.ticksElapsed >= SPIRAL_CLUB_COUNT || effect.level != caster.level()) {
            activeSpiralClubs.remove(id);
            return;
        }
        Entity target = effect.level.getEntity(effect.targetId);
        if (target instanceof LivingEntity living && living.isAlive()) {
            effect.anchor = living.position().add(0.0D, living.getBbHeight() * 0.5D + 0.2D, 0.0D);
        }
        int tick = effect.ticksElapsed;
        // 俯视顺时针：x=cos、z=sin 时角度递增，在"上北下南"的俯视图上正好是顺时针
        double angle = effect.baseAngle + tick * (Math.PI * 2.0D / SPIRAL_CLUB_COUNT);
        double x = effect.anchor.x + SPIRAL_CLUB_RADIUS * Math.cos(angle);
        double z = effect.anchor.z + SPIRAL_CLUB_RADIUS * Math.sin(angle);
        // 兜底方向：起飞瞬间目标若已死亡，桃弹朝它出生时"圆心所在的那边"直飞
        double dx = effect.anchor.x - x;
        double dz = effect.anchor.z - z;
        double len = Math.sqrt(dx * dx + dz * dz);
        Vec3 fallback = len > 1.0E-4
                ? new Vec3(dx / len * SpellBullet.CLUB_SPEED, 0.0D, dz / len * SpellBullet.CLUB_SPEED)
                : Vec3.ZERO;
        SpellBullet.fire(effect.level, x, effect.anchor.y, z, SpellBullet.KIND_CLUB,
                SPIRAL_CLUB_DAMAGE, SPIRAL_CLUB_FLY_TICK - tick, effect.targetId, effect.ownerId, fallback);
        effect.ticksElapsed++;
    }

    /**
     * 释放方钻上冲：需主手持带该附魔的锄头；消耗 16 TP（戴荆棘指环减半）；
     * 必须看向 32 格内的生物才起效（没目标不扣 TP）。
     * 起效后由 tickDiamondUppercut 在目标脚下半径 5 格的圆面上持续 20 秒钻出方钻弹。
     */
    private void handleDiamondUppercut(ServerPlayer player) {
        ItemStack hoe = player.getMainHandItem();
        if (!(hoe.getItem() instanceof HoeItem) || !hasDiamondUppercut(hoe)) {
            return;
        }
        UUID id = player.getUUID();
        if (activeDiamondUppercut.containsKey(id)) {
            return; // 上一轮方钻雨还没停，不允许叠着放
        }
        int tp = tpValues.getOrDefault(id, 0);
        int cost = thornCost(player, TP_JEVIL_SPELL_COST);
        if (tp < cost) {
            showActionBar(player, "TP 不足（" + tp + "/" + cost + "），无法释放方钻上冲");
            return;
        }
        Mob target = findTargetOnView(player, JEVIL_SPELL_AIM_RANGE, Mob.class);
        if (target == null) {
            showActionBar(player, "没有看向生物，无法释放方钻上冲");
            return;
        }
        tpValues.put(id, tp - cost);
        syncTpToClient(player);
        // 圆心取释放瞬间目标脚下的位置，之后 20 秒钉死不动
        activeDiamondUppercut.put(id, new DiamondUppercutEffect(
                (ServerLevel) player.level(), player.getId(), target.position()));
    }

    /**
     * 方钻上冲时间轴：400 刻（20 秒）内每 3 刻在半径 5 格的圆面内均匀随机取一点，
     * 从该点的地面向上钻出 1 枚方钻弹（上升约 22 格后自行消散，撞到生物提前消散）。
     */
    private void tickDiamondUppercut(ServerPlayer caster) {
        UUID id = caster.getUUID();
        DiamondUppercutEffect effect = activeDiamondUppercut.get(id);
        if (effect == null) {
            return;
        }
        if (effect.ticksElapsed >= DIAMOND_UPPERCUT_DURATION || effect.level != caster.level()) {
            activeDiamondUppercut.remove(id);
            return;
        }
        if (effect.ticksElapsed % DIAMOND_UPPERCUT_INTERVAL == 0) {
            net.minecraft.util.RandomSource random = caster.getRandom();
            // 圆内均匀取点：r = R×√rand、θ 均匀，避免点全挤在圆心
            double r = DIAMOND_UPPERCUT_RADIUS * Math.sqrt(random.nextDouble());
            double theta = random.nextDouble() * Math.PI * 2.0D;
            double x = effect.anchor.x + r * Math.cos(theta);
            double z = effect.anchor.z + r * Math.sin(theta);
            double y = groundLevel(effect.level, x, z, effect.anchor.y);
            SpellBullet.fire(effect.level, x, y, z, SpellBullet.KIND_DIAMOND,
                    DIAMOND_UPPERCUT_DAMAGE, 0, -1, effect.ownerId, Vec3.ZERO);
        }
        effect.ticksElapsed++;
    }

    /**
     * 释放恶魔镰刀：需主手持带该附魔的锄头；消耗 32 TP（戴荆棘指环减半）；
     * 必须看向 32 格内的生物才起效（没目标不扣 TP）。
     * 起效瞬间在目标外圈（四象限各 1 柄）召唤 4 柄镰刀开始环绕，约 20 游戏刻后一齐朝中心俯冲、
     * 穿过中心继续飞，飞出外圈后恢复环绕，整场持续 15 秒；镰刀碰到的生物受 25 点伤害。
     * 15 秒内同一玩家不允许叠放第二场（镰刀弹自己活满寿命，这里只记结束时刻）。
     */
    private void handleJevilScythe(ServerPlayer player) {
        ItemStack hoe = player.getMainHandItem();
        if (!(hoe.getItem() instanceof HoeItem) || !hasJevilScythe(hoe)) {
            return;
        }
        UUID id = player.getUUID();
        ServerLevel level = (ServerLevel) player.level();
        Long until = jevilScytheUntil.get(id);
        if (until != null && level.getGameTime() < until) {
            return; // 上一场镰刀还在绕，不允许叠着放
        }
        int tp = tpValues.getOrDefault(id, 0);
        int cost = thornCost(player, TP_JEVIL_SCYTHE_COST);
        if (tp < cost) {
            showActionBar(player, "TP 不足（" + tp + "/" + cost + "），无法释放恶魔镰刀");
            return;
        }
        Mob target = findTargetOnView(player, JEVIL_SPELL_AIM_RANGE, Mob.class);
        if (target == null) {
            showActionBar(player, "没有看向生物，无法释放恶魔镰刀");
            return;
        }
        tpValues.put(id, tp - cost);
        syncTpToClient(player);
        jevilScytheUntil.put(id, level.getGameTime() + JEVIL_SCYTHE_DURATION);
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D + 0.2D, 0.0D);
        // 起环角度取"施法者所在的一侧"，四柄镰刀均匀分布在四象限
        double baseAngle = Math.atan2(player.getZ() - center.z, player.getX() - center.x);
        for (int i = 0; i < JEVIL_SCYTHE_COUNT; i++) {
            double angle = baseAngle + i * (Math.PI * 2.0D / JEVIL_SCYTHE_COUNT);
            SpellBullet.fireScythe(level, center, angle, JEVIL_SCYTHE_DAMAGE,
                    target.getId(), player.getId());
        }
    }

    /** 从目标脚面上方 1 格往下找第一个挡路方块，返回其顶面上方 0.3 格；12 格内找不到就用脚面。 */
    private static double groundLevel(ServerLevel level, double x, double z, double fromY) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        for (int y = Mth.floor(fromY) + 1; y >= Mth.floor(fromY) - 12; y--) {
            BlockPos pos = new BlockPos(bx, y, bz);
            if (level.getBlockState(pos).blocksMotion()) {
                return y + 1.3D;
            }
        }
        return fromY + 0.3D;
    }

    /** 冰震术时间轴：0~7 刻顺时针铺 8 片环上雪花，8 刻出中央大雪花，9 刻结算伤害。 */
    private void tickIceShock(ServerPlayer caster) {
        UUID id = caster.getUUID();
        IceShockEffect effect = activeIceShock.get(id);
        if (effect == null) {
            return;
        }
        if (effect.ticksElapsed >= ICE_SHOCK_END_TICK || effect.level != caster.level()) {
            activeIceShock.remove(id);
            return;
        }
        Entity target = effect.level.getEntity(effect.targetId);
        if (!(target instanceof LivingEntity living) || !living.isAlive()) {
            activeIceShock.remove(id); // 目标中途死亡/消失，吟唱直接结束（TP 已扣）
            return;
        }
        Vec3 center = living.position().add(0.0D, living.getBbHeight() * 0.5D + 0.2D, 0.0D);
        int tick = effect.ticksElapsed;
        if (tick < ICE_SHOCK_RING_COUNT) {
            // 俯视顺时针每刻点亮一片：角度从正东起、每刻 +45°
            float angle = (float) (tick * Math.PI * 2.0D / ICE_SHOCK_RING_COUNT);
            double x = center.x + ICE_SHOCK_RING_RADIUS * Math.cos(angle);
            double z = center.z + ICE_SHOCK_RING_RADIUS * Math.sin(angle);
            effect.level.sendParticles(
                    new IceShockSnowflakeOptions(GeneratedMod.ICE_SHOCK_SNOWFLAKE.get(), false, angle,
                            ICE_SHOCK_DAMAGE_TICK - tick, 0.28F),
                    x, center.y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        } else if (tick == ICE_SHOCK_CENTER_TICK) {
            // 8 片雪花凑齐，环中央凝出一片大雪花
            effect.level.sendParticles(
                    new IceShockSnowflakeOptions(GeneratedMod.ICE_SHOCK_SNOWFLAKE.get(), true, 0.0F,
                            ICE_SHOCK_DAMAGE_TICK - tick, 0.55F),
                    center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        } else if (tick == ICE_SHOCK_DAMAGE_TICK) {
            // 伤害结算：同一刻环上雪花散开、中央雪花膨胀渐隐（客户端按 hold 参数自动播放）
            applyIceShockDamage(effect.level, caster, living);
        }
        effect.ticksElapsed++;
    }

    /**
     * 冰震术伤害结算：伤害类型为 TrueIce（无视护甲）；先按 +40 冻结值判断是否触发强化伤害
     * （追加后 TicksFrozen ≥ 130 时伤害从 15 提升到 30，是替代不是叠加）。
     * 施法者带着「继续前进」buff 时，本次 TrueIce 伤害翻倍（15→30、30→60）。
     * 每命中一次还会给目标的 TicksSnowGrave（雪墓值）+50，攒满 140 即被冰封。
     */
    private static void applyIceShockDamage(ServerLevel level, ServerPlayer caster, LivingEntity target) {
        int frozen = Math.min(140, target.getTicksFrozen() + ICE_SHOCK_FROZEN_GAIN);
        float base = frozen >= ICE_SHOCK_FROZEN_THRESHOLD ? ICE_SHOCK_FROZEN_DAMAGE : ICE_SHOCK_DAMAGE;
        target.hurt(TrueIceDamage.source(level, caster), TrueIceDamage.damage(base, caster));
        target.setTicksFrozen(frozen);
        SnowGraveManager.add(target, SnowGraveManager.ICE_SHOCK_GRAVE_GAIN);
    }

    private void spawnSmash(ServerPlayer player, float damage, float scale, boolean crimson) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 start = eye.add(look.scale(0.6D));
        RoughSmashProjectile projectile = new RoughSmashProjectile(GeneratedMod.ROUGH_SMASH_ENTITY.get(), level);
        projectile.setPos(start.x, start.y, start.z);
        projectile.init(damage, scale, player, crimson);
        projectile.launch(look);
        level.addFreshEntity(projectile);
        // 发射瞬间播放 snd_rudebuster_hit.ogg
        level.playSound(null, start.x, start.y, start.z,
                GeneratedMod.RUDEBUSTER_HIT_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * 读取斧头的实际攻击力（即物品提示框里显示的"攻击力"：徒手基础 1 点 + 所有主手攻击伤害加成）。
     * 直接取物品属性而不是硬编码材质档位，所以其它模组自定的斧头（伤害再高/再低）也能正确适配。
     */
    private static double roughSmashAxeDamage(ItemStack axe) {
        double added = 1.0D;        // 玩家徒手基础攻击伤害
        double multiplyBase = 0.0D;
        double multiplyTotal = 1.0D;
        for (AttributeModifier modifier : axe.getAttributeModifiers(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
            switch (modifier.getOperation()) {
                case ADDITION:
                    added += modifier.getAmount();
                    break;
                case MULTIPLY_BASE:
                    multiplyBase += modifier.getAmount();
                    break;
                case MULTIPLY_TOTAL:
                    multiplyTotal *= 1.0D + modifier.getAmount();
                    break;
                default:
                    break;
            }
        }
        return Math.max(0.0D, added * (1.0D + multiplyBase) * multiplyTotal);
    }

    private void tickCharge(ServerPlayer player) {
        UUID id = player.getUUID();
        ChargeState charge = chargingPlayers.get(id);
        if (charge == null) {
            return;
        }
        Optional<ItemStack> ring = findEquippedRing(player).filter(ModEvents::hasSnowBurial);
        if (ring.isEmpty() || player.getCooldowns().isOnCooldown(ring.get().getItem())) {
            chargingPlayers.remove(id);
            return;
        }
        charge.ticksHeld++;
        if (charge.ticksHeld >= CHARGE_TICKS) {
            chargingPlayers.remove(id);
            castSnowBurial(player);
        }
    }

    private void castSnowBurial(ServerPlayer player) {
        UUID id = player.getUUID();
        int cost = thornCost(player, TP_SNOW_BURIAL_COST);
        tpValues.put(id, Math.max(0, tpValues.getOrDefault(id, 0) - cost));
        syncTpToClient(player);
        LivingEntity target = findTargetOnView(player, 32.0D, LivingEntity.class);
        if (target == null) {
            return;
        }
        activeEffects.put(player.getUUID(), new SnowBurialEffect(player.serverLevel(), target.getId(), target.position(), 0));
    }

    private void tickSnowBurial(ServerPlayer caster) {
        UUID id = caster.getUUID();
        SnowBurialEffect effect = activeEffects.get(id);
        if (effect == null) {
            return;
        }
        if (effect.ticksElapsed >= EFFECT_TICKS || effect.level != caster.level()) {
            activeEffects.remove(id);
            // 冷却写在真正佩戴的那枚指环上（雪花指环也能放雪葬了）
            Item cooldownRing = findEquippedRing(caster).filter(ModEvents::hasSnowBurial)
                    .map(ItemStack::getItem)
                    .orElse(GeneratedMod.THORN_RING.get());
            caster.getCooldowns().addCooldown(cooldownRing, COOLDOWN_TICKS);
            return;
        }

        Entity target = effect.level.getEntity(effect.targetId);
        if (target instanceof LivingEntity living && living.isAlive()) {
            effect.origin = living.position();
        }

        effect.level.sendParticles(GeneratedMod.SNOW_BURIAL_PARTICLE.get(),
                effect.origin.x, effect.origin.y + 0.05D, effect.origin.z,
                PARTICLES_PER_TICK, 1.45D, 0.10D, 1.45D, 0.04D);
        // 每 0.5 秒（10 刻）结算一次伤害，伤害类型按"虚空 → TrueIce"循环交替
        if (effect.ticksElapsed % SNOW_BURIAL_INTERVAL == 0) {
            damageEntitiesInRange(effect.level, effect.origin, caster, effect.ticksElapsed / SNOW_BURIAL_INTERVAL);
        }
        effect.ticksElapsed++;
    }

    /** 第 pulse 次结算使用的伤害类型：偶数次虚空、奇数次 TrueIce，循环往复。 */
    private static DamageSource snowBurialDamageSource(ServerLevel level, ServerPlayer caster, int pulse) {
        if (Math.floorMod(pulse, 2) == 0) {
            // 虚空：原版 out_of_world 伤害类型（同样无视护甲与无敌判定），
            // 手动带上施法者作来源实体，保证最后一击在虚空跳伤上时击杀归属仍是玩家
            Holder<DamageType> voidType = level.registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD);
            return new DamageSource(voidType, caster, caster);
        }
        return TrueIceDamage.source(level, caster);
    }

    private static void damageEntitiesInRange(ServerLevel level, Vec3 origin, ServerPlayer caster, int pulse) {
        AABB area = new AABB(origin, origin).inflate(EFFECT_RADIUS);
        for (Entity entity : level.getEntities((Entity) null, area,
                candidate -> candidate instanceof LivingEntity living
                        && living.isAlive()
                        && candidate != caster
                        && candidate.distanceToSqr(origin) <= EFFECT_RADIUS * EFFECT_RADIUS)) {
            LivingEntity target = (LivingEntity) entity;
            DamageSource source = snowBurialDamageSource(level, caster, pulse);
            // 基础 20 点；TrueIce 那一跳过一遍「继续前进」判定（带 buff 时 20→40），虚空那一跳不翻倍
            float amount = source.is(TrueIceDamage.KEY) ? TrueIceDamage.damage(SNOW_BURIAL_DAMAGE, caster)
                    : SNOW_BURIAL_DAMAGE;
            // 清掉受击无敌帧，保证每 0.5 秒的伤害都能按类型正常结算
            target.invulnerableTime = 0;
            target.hurt(source, amount);
            // 进度「冰冻鸡块」：这一跳伤害直接把目标打死才算雪葬击杀
            if (!target.isAlive()) {
                DeltamagicAdvancements.SNOW_GRAVE_KILL.kill(caster, target);
            }
            // 生物数据 TicksFrozen 设为 140（玩家目标会看到结霜屏幕效果，生物呈现全身结霜）
            target.setTicksFrozen(SNOW_BURIAL_TICKS_FROZEN);
            // 雪葬同时强制内部生物的 TicksSnowGrave 设为 140：进入 5 秒冰封（冰块包裹），
            // 吟唱期间每次跳伤都会刷新这 5 秒，暴雪一停、冰封再持续 5 秒后才解除并归零
            SnowGraveManager.force(target, SnowGraveManager.MAX_VALUE);
        }
    }

    private void tickGrindstone(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (!(menu instanceof GrindstoneMenu grindstone)) {
            grindstoneSnowBurialState.remove(player.getUUID());
            return;
        }

        boolean currentlyHasSnowBurial = hasSnowBurial(grindstone.getSlot(0).getItem())
                || hasSnowBurial(grindstone.getSlot(1).getItem());
        boolean previouslyHadSnowBurial = grindstoneSnowBurialState.getOrDefault(player.getUUID(), false);
        if (previouslyHadSnowBurial && !currentlyHasSnowBurial) {
            playSoundForPlayer(player, GeneratedMod.OMINOUS_CANCEL_SOUND.get(), 1.0F, 1.0F);
            // 进度「*Just joking」：雪葬附魔真的被磨掉了（只是把成品拿回背包的不算）
            if (!playerHoldsSnowBurial(player, grindstone)) {
                DeltamagicAdvancements.GRINDSTONE_DISENCHANT.disenchant(
                        player, GeneratedMod.SNOW_BURIAL.get());
            }
        }
        grindstoneSnowBurialState.put(player.getUUID(), currentlyHasSnowBurial);
    }

    /** 玩家身上（背包 / 副手 / 盔甲 / 光标 / 当前砂轮槽位）是否还留有任何带雪葬附魔的物品。 */
    private static boolean playerHoldsSnowBurial(ServerPlayer player, GrindstoneMenu menu) {
        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            if (hasSnowBurial(stack)) {
                return true;
            }
        }
        for (ItemStack stack : inventory.offhand) {
            if (hasSnowBurial(stack)) {
                return true;
            }
        }
        for (ItemStack stack : inventory.armor) {
            if (hasSnowBurial(stack)) {
                return true;
            }
        }
        if (hasSnowBurial(player.containerMenu.getCarried())) {
            return true;
        }
        for (int slot = 0; slot < menu.slots.size(); slot++) {
            if (hasSnowBurial(menu.getSlot(slot).getItem())) {
                return true;
            }
        }
        return false;
    }

    private void tickTp(ServerPlayer player) {
        UUID id = player.getUUID();
        int current = tpValues.getOrDefault(id, 0);
        if (current >= TP_MAX) {
            tpPassiveTimers.put(id, 0);
            tpShieldTimers.put(id, 0);
            return;
        }

        boolean blocking = player.isBlocking();
        if (blocking) {
            int timer = tpShieldTimers.getOrDefault(id, 0) + 1;
            if (timer >= TP_SHIELD_TICKS) {
                tpValues.put(id, Math.min(TP_MAX, current + TP_SHIELD_GAIN));
                timer = 0;
            }
            tpShieldTimers.put(id, timer);
            tpPassiveTimers.put(id, 0);
        } else {
            int timer = tpPassiveTimers.getOrDefault(id, 0) + 1;
            if (timer >= TP_PASSIVE_TICKS) {
                tpValues.put(id, Math.min(TP_MAX, current + TP_PASSIVE_GAIN));
                timer = 0;
            }
            tpPassiveTimers.put(id, timer);
            tpShieldTimers.put(id, 0);
        }
    }

    /**
     * 盾牌成功格挡伤害时触发（原版判定格挡成立才会发出该事件）。
     * 只有"有源伤害"——即伤害能追溯到某个来源实体（近战怪物、玩家、
     * 被实体射出的箭/火球等）——才回复 16 点 TP；坠落、岩浆这类
     * 无源伤害即使被盾减伤也不算。
     */
    @SubscribeEvent
    public void onShieldBlock(ShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getDamageSource().getEntity() == null) {
            return;
        }
        addTp(player, TP_SHIELD_BLOCK_GAIN);
        // 格挡有源伤害成功：播放 snd_graze.ogg
        playSoundForPlayer(player, GeneratedMod.GRAZE_SOUND.get(), 1.0F, 1.0F);
    }

    /** 给玩家增加 TP（封顶 TP_MAX），有变化时立刻同步给客户端刷新 TP 条。 */
    private void addTp(ServerPlayer player, int amount) {
        UUID id = player.getUUID();
        int current = tpValues.getOrDefault(id, 0);
        int next = Math.min(TP_MAX, current + amount);
        if (next != current) {
            tpValues.put(id, next);
            syncTpToClient(player);
        }
    }

    /**
     * 「紧迫」系列物品使用时的 TP 恢复入口。
     * TP 已经满时返回 false，调用方据此不消耗物品；否则恢复 amount 点并返回 true。
     */
    public static boolean tryRestoreTp(ServerPlayer player, int amount) {
        if (INSTANCE.tpValues.getOrDefault(player.getUUID(), 0) >= TP_MAX) {
            return false;
        }
        INSTANCE.addTp(player, amount);
        return true;
    }

    private void showActionBar(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    private static boolean hasSnowBurial(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.SNOW_BURIAL.get(), stack) > 0;
    }

    public static boolean hasToothbrush(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.TOOTHBRUSH.get(), stack) > 0;
    }

    public static boolean hasRoughSmash(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.ROUGH_SMASH.get(), stack) > 0;
    }

    public static boolean hasCrimsonSmash(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.CRIMSON_SMASH.get(), stack) > 0;
    }

    public static boolean hasIceShock(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.ICE_SHOCK.get(), stack) > 0;
    }

    public static boolean hasHealPrayer(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.HEAL_PRAYER.get(), stack) > 0;
    }

    public static boolean hasOkHeal(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.OK_HEAL.get(), stack) > 0;
    }

    public static boolean hasSleepSpray(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.SLEEP_SPRAY.get(), stack) > 0;
    }

    public static boolean hasSpiralClubs(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.SPIRAL_CLUBS.get(), stack) > 0;
    }

    public static boolean hasDiamondUppercut(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.DIAMOND_UPPERCUT.get(), stack) > 0;
    }

    public static boolean hasJevilScythe(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(GeneratedMod.JEVIL_SCYTHE.get(), stack) > 0;
    }

    /** 判断右栏是否是带指定附魔的原版附魔书（兼容标签里的 StoredEnchantments 与战利品写入的 Enchantments）。 */
    private static boolean bookGrants(ItemStack book, net.minecraft.world.item.enchantment.Enchantment enchantment) {
        if (!book.is(Items.ENCHANTED_BOOK)) {
            return false;
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(enchantment, book) > 0) {
            return true;
        }
        net.minecraft.nbt.ListTag stored = EnchantedBookItem.getEnchantments(book);
        for (int i = 0; i < stored.size(); i++) {
            net.minecraft.nbt.CompoundTag tag = stored.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
            if (id != null && net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getValue(id) == enchantment) {
                return true;
            }
        }
        return false;
    }

    /** 是否是带本模组任一附魔的原版附魔书（供 TP 条 HUD 判断手持模组物品）。 */
    public static boolean carriesModBookEnchantment(ItemStack book) {
        return bookGrants(book, GeneratedMod.SNOW_BURIAL.get())
                || bookGrants(book, GeneratedMod.TOOTHBRUSH.get())
                || bookGrants(book, GeneratedMod.ROUGH_SMASH.get())
                || bookGrants(book, GeneratedMod.CRIMSON_SMASH.get())
                || bookGrants(book, GeneratedMod.ICE_SHOCK.get())
                || bookGrants(book, GeneratedMod.HEAL_PRAYER.get())
                || bookGrants(book, GeneratedMod.OK_HEAL.get())
                || bookGrants(book, GeneratedMod.SLEEP_SPRAY.get())
                || bookGrants(book, GeneratedMod.SPIRAL_CLUBS.get())
                || bookGrants(book, GeneratedMod.DIAMOND_UPPERCUT.get())
                || bookGrants(book, GeneratedMod.JEVIL_SCYTHE.get());
    }

    private static void playSoundForPlayer(Player player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    /**
     * 返回戒指槽里佩戴的本模组指环（荆棘指环或雪花指环）；两种都戴着时优先返回荆棘指环，
     * 这样"消耗减半"与附魔判定都以荆棘指环为准。
     * Uses the stable Curios API reflectively so the source checker can run without external API stubs.
     */
    public static Optional<ItemStack> findEquippedRing(LivingEntity wearer) {
        try {
            Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Object lazyInventory = curiosApi.getMethod("getCuriosInventory", LivingEntity.class)
                    .invoke(null, wearer);
            Object resolved = lazyInventory.getClass().getMethod("resolve").invoke(lazyInventory);
            if (!(resolved instanceof Optional<?> optional) || optional.isEmpty()) {
                return Optional.empty();
            }
            Object handler = optional.get();
            Class<?> handlerApi = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
            Object curios = handlerApi.getMethod("getCurios").invoke(handler);
            if (!(curios instanceof Map<?, ?> map)) {
                return Optional.empty();
            }
            Object ringHandler = map.get("ring");
            if (ringHandler == null) {
                return Optional.empty();
            }
            Class<?> stacksApi = Class.forName("top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler");
            Object stacks = stacksApi.getMethod("getStacks").invoke(ringHandler);
            Class<?> dynamicStacksApi = Class.forName(
                    "top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler");
            int slots = (Integer) dynamicStacksApi.getMethod("getSlots").invoke(stacks);
            Method getStack = dynamicStacksApi.getMethod("getStackInSlot", int.class);
            Optional<ItemStack> snowflake = Optional.empty();
            for (int index = 0; index < slots; index++) {
                Object stack = getStack.invoke(stacks, index);
                if (stack instanceof ItemStack itemStack) {
                    if (itemStack.is(GeneratedMod.THORN_RING.get())) {
                        return Optional.of(itemStack); // 荆棘指环优先
                    }
                    if (itemStack.is(GeneratedMod.SNOWFLAKE_RING.get()) && snowflake.isEmpty()) {
                        snowflake = Optional.of(itemStack);
                    }
                }
            }
            return snowflake;
        } catch (ReflectiveOperationException ignored) {
            // Curios is a mandatory runtime dependency; an unavailable API disables the slot effect.
        }
        return Optional.empty();
    }

    /** 戒指槽里是否戴着荆棘指环——所有法术 TP 消耗减半的判定入口。 */
    public static boolean hasThornRingEquipped(LivingEntity wearer) {
        return findEquippedRing(wearer)
                .filter(ring -> ring.is(GeneratedMod.THORN_RING.get()))
                .isPresent();
    }

    /**
     * 法术实际 TP 消耗：佩戴荆棘指环时把基础消耗除以 2（整数除法向下取整，最低 1），
     * 否则按基础消耗。所有法术扣费与"TP 不足"提示都走这里，保证规则一致。
     */
    private static int thornCost(ServerPlayer player, int baseCost) {
        return hasThornRingEquipped(player) ? Math.max(1, baseCost / 2) : baseCost;
    }

    private static final class ChargeState {
        private int ticksHeld;
    }

    private static final class IceShockEffect {
        private final ServerLevel level;
        private final int targetId;
        private int ticksElapsed;

        private IceShockEffect(ServerLevel level, int targetId) {
            this.level = level;
            this.targetId = targetId;
        }
    }

    /** 睡眠喷雾的施法状态：目标、三朵雾的出生位置（null = 还没放出）、走刻数。 */
    private static final class SleepSprayEffect {
        private final ServerLevel level;
        private final int targetId;
        private final Vec3[] mistPos = new Vec3[SLEEP_SPRAY_MIST_COUNT];
        /** 最近一次的目标身体中心：目标死亡后雾继续在这个位置散完。 */
        private Vec3 anchor;
        private int ticksElapsed;

        private SleepSprayEffect(ServerLevel level, int targetId) {
            this.level = level;
            this.targetId = targetId;
        }
    }

    /** 螺旋桃击的铺环状态：目标、施法者、当前圆心（活目标带着走）、起环角度、走刻数。 */
    private static final class SpiralClubsEffect {
        private final ServerLevel level;
        private final int targetId;
        private final int ownerId;
        private Vec3 anchor;
        private final double baseAngle;
        private int ticksElapsed;

        private SpiralClubsEffect(ServerLevel level, int targetId, int ownerId, Vec3 anchor, double baseAngle) {
            this.level = level;
            this.targetId = targetId;
            this.ownerId = ownerId;
            this.anchor = anchor;
            this.baseAngle = baseAngle;
        }
    }

    /** 方钻上冲的生效状态：圆心（释放瞬间钉死）、施法者、走刻数。 */
    private static final class DiamondUppercutEffect {
        private final ServerLevel level;
        private final int ownerId;
        private final Vec3 anchor;
        private int ticksElapsed;

        private DiamondUppercutEffect(ServerLevel level, int ownerId, Vec3 anchor) {
            this.level = level;
            this.ownerId = ownerId;
            this.anchor = anchor;
        }
    }

    private static final class SnowBurialEffect {
        private final ServerLevel level;
        private final int targetId;
        private Vec3 origin;
        private int ticksElapsed;

        private SnowBurialEffect(ServerLevel level, int targetId, Vec3 origin, int ticksElapsed) {
            this.level = level;
            this.targetId = targetId;
            this.origin = origin;
            this.ticksElapsed = ticksElapsed;
        }
    }

}
