package cn.blockforge.generated.frostboundthorns;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 生物数据「TicksSnowGrave（雪墓值）」的服务端管理器。规则：
 * <ol>
 * <li>与原版 TicksFrozen 类似、可被法术累加，封顶 {@value #MAX_VALUE}；到 140 会强制冻结生物
 *     5 秒（拉满原版冻结值 + 移速压到 0 + 钉住水平位移），冻结结束后雪墓值归零；</li>
 * <li>3 秒内没有继续上涨后，每秒自动减 1；</li>
 * <li>冰封期间生物被冰块包裹（渲染在客户端完成），直到数值回到 0；</li>
 * <li>雪葬每次跳伤把范围内生物强制设为 140；冰震术每命中一次 +{@value #ICE_SHOCK_GRAVE_GAIN}。</li>
 * </ol>
 * 数值每有变化就通过 SnowGraveSyncPacket 推给"追踪该实体的客户端 +（目标为玩家时）本人"，
 * 客户端据此绘制全屏冰块贴图与包裹冰块模型。状态写入 ForgeData，随实体正常存盘。
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class SnowGraveManager {
    /** 雪墓值封顶（= 触发强制冻结的阈值，与原版完全冻结阈值 140 对齐）。 */
    public static final int MAX_VALUE = 140;
    /** 冰震术每次命中追加的雪墓值。 */
    public static final int ICE_SHOCK_GRAVE_GAIN = 50;

    /** 到 140 后的强制冻结时长：5 秒。 */
    private static final int GRAVE_FREEZE_TICKS = 100;
    /** 连续 3 秒不再上涨后开始衰减。 */
    private static final int DECAY_GRACE_TICKS = 60;
    /** 衰减节奏：每秒（20 游戏刻）减 1。 */
    private static final int DECAY_INTERVAL_TICKS = 20;

    private static final String NBT_VALUE = "deltamagic_SnowGrave";
    private static final String NBT_IDLE = "deltamagic_SnowGraveIdle";
    private static final String NBT_FREEZE = "deltamagic_SnowGraveFreeze";

    /** 冻结期间挂到移动速度上的修饰符：MULTIPLY_TOTAL -100%，把速度整体压成 0（不会变负、不会倒着走）。 */
    private static final UUID SPEED_LOCK_ID = UUID.fromString("3a5e9c21-64d8-4f0b-9a72-2c1d5e8f4b67");
    private static final AttributeModifier SPEED_LOCK = new AttributeModifier(SPEED_LOCK_ID,
            "deltamagic snow grave freeze", -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private static final Map<UUID, State> STATES = new HashMap<>();

    private SnowGraveManager() {
    }

    /** 冰震术入口：每命中一次上涨 amount（封顶 140；冰封期间数值已满，不再累加）。 */
    public static void add(LivingEntity living, int amount) {
        if (living.level().isClientSide || amount <= 0) {
            return;
        }
        State state = getOrCreate(living);
        if (state.freezeTicks > 0) {
            return;
        }
        int before = state.value;
        state.value = Math.min(MAX_VALUE, state.value + amount);
        state.idleTicks = 0;
        state.decayTicks = 0;
        if (state.value != before) {
            afterChange(living, state);
        }
    }

    /** 雪葬入口：强制把雪墓值设成给定值（140 即立刻进入冰封；冰封期间重复调用会刷新 5 秒时长）。 */
    public static void force(LivingEntity living, int value) {
        if (living.level().isClientSide) {
            return;
        }
        State state = getOrCreate(living);
        state.value = Math.max(0, Math.min(MAX_VALUE, value));
        state.idleTicks = 0;
        state.decayTicks = 0;
        afterChange(living, state);
    }

    /** 数值变化后的统一收尾：判断是否进入冰封、写 NBT、同步客户端、清理空状态。 */
    private static void afterChange(LivingEntity living, State state) {
        if (state.value >= MAX_VALUE) {
            // 到 140：开始（或被雪葬再次命中时刷新）5 秒强制冻结
            state.freezeTicks = GRAVE_FREEZE_TICKS;
        }
        saveToNbt(living, state);
        NetworkHandler.syncSnowGrave(living, state.value);
        cleanupIfDone(living, state);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide) {
            return;
        }
        State state = STATES.get(living.getUUID());
        if (state == null) {
            return;
        }
        if (state.freezeTicks > 0) {
            tickFrozen(living, state);
        } else if (state.value > 0) {
            tickDecay(living, state);
        }
        saveToNbt(living, state);
        cleanupIfDone(living, state);
    }

    /** 冰封中：每刻维持原版冻结值拉满、移速压 0、水平位移钉死；5 秒到点后雪墓值归零。 */
    private static void tickFrozen(LivingEntity living, State state) {
        if (living.getTicksFrozen() < MAX_VALUE) {
            living.setTicksFrozen(MAX_VALUE);
        }
        AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(SPEED_LOCK)) {
            speed.addTransientModifier(SPEED_LOCK); // transient：不写进存盘，实体一删就没了
        }
        Vec3 motion = living.getDeltaMovement();
        living.setDeltaMovement(0.0D, motion.y, 0.0D);
        state.freezeTicks--;
        state.idleTicks = 0;
        state.decayTicks = 0;
        if (state.freezeTicks == 0) {
            state.value = 0; // 冻结结束，TicksSnowGrave 归零（冰块包裹也随之消失）
            if (speed != null) {
                speed.removeModifier(SPEED_LOCK_ID);
            }
            NetworkHandler.syncSnowGrave(living, 0);
        } else if (state.freezeTicks % 10 == 0) {
            // 冰封期间每 0.5 秒重发一次，兜住"冰封开始后才进入视野的玩家"看不到包裹冰块的情况
            NetworkHandler.syncSnowGrave(living, state.value);
        }
    }

    /** 未冰封：距上次上涨满 3 秒后，每秒 -1。 */
    private static void tickDecay(LivingEntity living, State state) {
        state.idleTicks++;
        if (state.idleTicks < DECAY_GRACE_TICKS) {
            return;
        }
        state.decayTicks++;
        if (state.decayTicks >= DECAY_INTERVAL_TICKS) {
            state.decayTicks = 0;
            state.value = Math.max(0, state.value - 1);
            NetworkHandler.syncSnowGrave(living, state.value);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide) {
            return;
        }
        State state = STATES.remove(living.getUUID());
        if (state != null) {
            // 冰封中被打死：立刻解除移速锁并通知客户端拆掉冰块
            AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && state.freezeTicks > 0) {
                speed.removeModifier(SPEED_LOCK_ID);
            }
            NetworkHandler.syncSnowGrave(living, 0);
        }
        clearNbt(living);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() == null || event.getLevel().isClientSide
                || !(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        // 区块重新加载 / 玩家重连：从 ForgeData 恢复上次的雪墓状态
        CompoundTag tag = living.getPersistentData();
        int value = tag.getInt(NBT_VALUE);
        int freeze = tag.getInt(NBT_FREEZE);
        if (value <= 0 && freeze <= 0) {
            return;
        }
        State state = new State();
        state.value = Math.min(MAX_VALUE, value);
        state.idleTicks = tag.getInt(NBT_IDLE);
        state.freezeTicks = freeze;
        STATES.put(living.getUUID(), state);
        NetworkHandler.syncSnowGrave(living, state.value);
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel() != null && event.getLevel().isClientSide) {
            return;
        }
        if (event.getEntity() instanceof LivingEntity living) {
            // 实体离开世界（卸载区块 / 玩家退出）：内存表清掉，状态已在 ForgeData 里随实体存盘
            STATES.remove(living.getUUID());
        }
    }

    private static void cleanupIfDone(LivingEntity living, State state) {
        if (state.value <= 0 && state.freezeTicks <= 0) {
            STATES.remove(living.getUUID());
            clearNbt(living);
        }
    }

    private static void clearNbt(LivingEntity living) {
        CompoundTag tag = living.getPersistentData();
        tag.remove(NBT_VALUE);
        tag.remove(NBT_IDLE);
        tag.remove(NBT_FREEZE);
    }

    private static State getOrCreate(LivingEntity living) {
        return STATES.computeIfAbsent(living.getUUID(), id -> new State());
    }

    private static void saveToNbt(LivingEntity living, State state) {
        CompoundTag tag = living.getPersistentData();
        tag.putInt(NBT_VALUE, state.value);
        tag.putInt(NBT_IDLE, state.idleTicks);
        tag.putInt(NBT_FREEZE, state.freezeTicks);
    }

    /** 每个实体的雪墓值状态（仅服务端持有）。 */
    private static final class State {
        private int value;        // TicksSnowGrave，0..140
        private int idleTicks;    // 距上次上涨过的刻数
        private int decayTicks;   // 衰减节奏计数（每 20 刻 -1）
        private int freezeTicks;  // >0 表示冰封中，剩余刻数
    }
}
