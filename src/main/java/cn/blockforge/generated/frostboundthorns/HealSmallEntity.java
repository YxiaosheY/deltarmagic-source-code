package cn.blockforge.generated.frostboundthorns;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 「OK治疗」的治疗光球（heal_small）：服务端驱动，客户端只渲染三帧贴片动画。
 * 两种状态：
 * 环绕——以施法者身体为中心、半径 1.15 格的圆轨道转圈（角速度与 Jevil 旋转木马弹幕同档），
 * 环上的光球始终按 360°/当前数量 均分间距（召唤、射出、过期时都会重新排一次队形），
 * 附带上下小幅漂浮，贴片始终正面朝向施法者，存活 30 秒；
 * 射出——施法者对准空气右键时，由 {@link ModEvents} 挑一枚还在环绕的光球沿视线直线飞出，
 * 初速 1 格/刻、每刻再 +0.1（封顶 2.5 格/刻），贴片朝向锁定在射出方向上不再变化；
 * 撞到方块消散，撞到任意活体实体（含玩家）则为其恢复 [其最大生命值 × 0.65 + 治疗量]
 * 并附带 30 秒 1 级抗性提升，然后消散。
 * 治疗量在召唤瞬间从施法者的永久存档读好写进光球，之后施法者再成长也不影响已存在的光球。
 */
public final class HealSmallEntity extends Entity {
    /** 光球寿命：30 秒 = 600 游戏刻（环绕与射出共用同一寿命）。 */
    public static final int MAX_AGE = 600;
    /** 环绕半径（格）。 */
    public static final double ORBIT_RADIUS = 1.15D;
    /** 环绕角速度（弧度/游戏刻）：与 Jevil 旋转木马弹幕同一档（0.16），约 2 秒转满一圈。 */
    public static final double ORBIT_SPEED = 0.16D;
    /** 环绕轨道高度：施法者脚面上方 身体高度 × 该系数 处。 */
    private static final double ORBIT_HEIGHT_FACTOR = 0.6D;
    /** 射出初速（格/游戏刻），约 20 格/秒起步。 */
    public static final double FLY_SPEED = 1.0D;
    /** 射出后每刻追加的速度（格/游戏刻²），越飞越快。 */
    public static final double FLY_ACCELERATION = 0.1D;
    /** 射出速度上限（格/游戏刻），约 50 格/秒，防止一帧跨过太大距离。 */
    public static final double FLY_MAX_SPEED = 2.5D;
    /** 射出命中分段的单步长度（格）：速度快时按小步推进，避免整段跨过实体或方块。 */
    private static final double FLY_STEP = 0.5D;
    /** 命中后附带的抗性提升：30 秒 = 600 游戏刻、1 级（amplifier 0）。 */
    private static final int RESISTANCE_TICKS = 600;
    private static final int RESISTANCE_AMPLIFIER = 0;
    /** 命中恢复量 = 目标最大生命值 × 该系数 + 治疗量。 */
    private static final float TARGET_HEAL_MULT = 0.65F;

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(HealSmallEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_LAUNCHED =
            SynchedEntityData.defineId(HealSmallEntity.class, EntityDataSerializers.BOOLEAN);

    /** 环绕起始相位角（弧度）：由 {@link #rebalanceOrbits} 按"当前环上还有几枚"均分，永远等间距。 */
    private double phase;
    /** 召唤瞬间定格的"治疗量"（点），命中结算时加在恢复量上。 */
    private float healBonus;
    /** 服务端生命周期计数（自毁判定用）。 */
    private int age;

    public HealSmallEntity(EntityType<? extends HealSmallEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    /**
     * 服务端召唤入口：在施法者轨道上生成一枚环绕光球，并把环上所有光球重新均分间距。
     * slot 只决定出生瞬间的站位，真正的间距以重排结果为准。
     */
    public static HealSmallEntity summon(ServerLevel level, ServerPlayer owner, int slot, float healBonus) {
        HealSmallEntity orb = new HealSmallEntity(GeneratedMod.HEAL_SMALL.get(), level);
        double angle = slot * (Math.PI * 2.0D / ModEvents.OK_HEAL_MAX_ORBS);
        Vec3 anchor = orbitCenter(owner, angle);
        orb.setPos(anchor.x, anchor.y, anchor.z);
        orb.entityData.set(DATA_OWNER, Optional.of(owner.getUUID()));
        orb.phase = angle;
        orb.healBonus = healBonus;
        level.addFreshEntity(orb);
        rebalanceOrbits(level, owner, null);
        return orb;
    }

    /** 当前环绕相位角（弧度）= 起始相位 + 每刻累加的转角。 */
    private double currentAngle() {
        return this.phase + this.age * ORBIT_SPEED;
    }

    /** 把任意角度折回 [0, 2π)：环上排序用，避免"370°"和"10°"被当成两个不同方位。 */
    private static double normalizeAngle(double angle) {
        double twoPi = Math.PI * 2.0D;
        double wrapped = angle % twoPi;
        return wrapped < 0.0D ? wrapped + twoPi : wrapped;
    }

    /**
     * 队形重排：把 owner 环上仍在环绕的光球按当前方位从同一点起、依次间隔 360°/n 摆开。
     * 光球每刻转过同一角度，所以这次摆匀之后间距会一直保持，直到下一次数量变化
     * （新召唤 / 被射出 / 寿命到期）时再重排一次。exclude 是本次要离环的那一枚，可为 null。
     */
    private static void rebalanceOrbits(ServerLevel level, LivingEntity owner, HealSmallEntity exclude) {
        List<HealSmallEntity> orbs = new ArrayList<>();
        for (Entity entity : level.getEntities(owner, owner.getBoundingBox().inflate(8.0D),
                other -> other != exclude && other instanceof HealSmallEntity orb
                        && !orb.isLaunched() && orb.isOwnedBy(owner))) {
            orbs.add((HealSmallEntity) entity);
        }
        if (orbs.size() < 2) {
            return; // 只剩一枚（或没有）时无所谓间距，保持原位
        }
        orbs.sort(Comparator.comparingDouble(HealSmallEntity::currentAngle));
        double first = normalizeAngle(orbs.get(0).currentAngle());
        int count = orbs.size();
        for (int i = 1; i < count; i++) {
            HealSmallEntity orb = orbs.get(i);
            double target = first + i * (Math.PI * 2.0D / count);
            orb.phase = target - orb.age * ORBIT_SPEED;
        }
    }

    /** 指定相位角上、环绕轨道所在的那一点（出生定位用）。 */
    private static Vec3 orbitCenter(LivingEntity owner, double angle) {
        double cy = owner.getY() + owner.getBbHeight() * ORBIT_HEIGHT_FACTOR;
        return new Vec3(owner.getX() + ORBIT_RADIUS * Math.cos(angle), cy,
                owner.getZ() + ORBIT_RADIUS * Math.sin(angle));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_OWNER, Optional.empty());
        this.entityData.define(DATA_LAUNCHED, false);
    }

    /** 光球是否已被右键射出（true = 直线飞行中，不再参与环绕治疗计数）。 */
    public boolean isLaunched() {
        return Boolean.TRUE.equals(this.entityData.get(DATA_LAUNCHED));
    }

    /** 这枚光球是否属于指定玩家。 */
    public boolean isOwnedBy(LivingEntity candidate) {
        Optional<UUID> owner = this.entityData.get(DATA_OWNER);
        return owner.isPresent() && owner.get().equals(candidate.getUUID());
    }

    /** 施法者 UUID（环绕阶段渲染器靠它把光球"转正"面向玩家，两端都可读）。 */
    public java.util.Optional<UUID> ownerUuid() {
        return this.entityData.get(DATA_OWNER);
    }

    /**
     * 服务端射出入口：沿视线方向直线飞出（由 ModEvents 在玩家对空气右键时调用）。
     * 同时把实体朝向锁定为射出方向——原版会把 yRot/xRot 随位置包一起同步给客户端，
     * 渲染器据此让贴片保持固定朝向、不再跟随玩家转身。
     */
    public void launch(Vec3 look) {
        double len = look.length();
        if (len < 1.0E-4) {
            return;
        }
        Vec3 dir = look.scale(1.0D / len);
        this.entityData.set(DATA_LAUNCHED, true);
        double horizontal = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        this.setYRot((float) Math.toDegrees(Math.atan2(-dir.x, dir.z)));
        this.setXRot((float) Math.toDegrees(Math.atan2(-dir.y, horizontal)));
        this.setDeltaMovement(dir.scale(FLY_SPEED));
        // 这一枚离环了：剩下的光球重新均分间距
        if (this.level() instanceof ServerLevel serverLevel) {
            LivingEntity owner = resolveOwner();
            if (owner != null) {
                rebalanceOrbits(serverLevel, owner, this);
            }
        }
    }

    private LivingEntity resolveOwner() {
        Optional<UUID> owner = this.entityData.get(DATA_OWNER);
        if (owner.isEmpty() || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(owner.get());
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            // 客户端只做位置插值（原版实体同步已处理），逻辑全在服务端
            return;
        }
        this.age++;
        LivingEntity owner = resolveOwner();
        if (owner == null) {
            this.discard(); // 施法者死亡/离场/跨存档重载，光球直接消散
            return;
        }
        if (this.age > MAX_AGE) {
            // 这一枚到期离环：剩下的光球重新均分间距，然后自己消散
            rebalanceOrbits((ServerLevel) this.level(), owner, this);
            this.discard();
            return;
        }
        if (isLaunched()) {
            tickFlight();
        } else {
            tickOrbit(owner);
        }
    }

    /** 环绕阶段：轨道圆心跟着施法者走，相位角每刻累加 ORBIT_SPEED（队形由服务端重排保证均分）。 */
    private void tickOrbit(LivingEntity owner) {
        double angle = currentAngle();
        double cy = owner.getY() + owner.getBbHeight() * ORBIT_HEIGHT_FACTOR
                + Math.sin(this.age * 0.12D + this.phase) * 0.12D;
        this.setPos(owner.getX() + ORBIT_RADIUS * Math.cos(angle), cy,
                owner.getZ() + ORBIT_RADIUS * Math.sin(angle));
    }

    /**
     * 射出阶段：沿当前速度直线飞，每刻再加速 FLY_ACCELERATION（封顶 FLY_MAX_SPEED）。
     * 一个刻内按不超过 0.5 格的小步推进：先查方块再查实体，命中即结算治疗并消散，
     * 速度提起来也不会整段跨过目标。
     */
    private void tickFlight() {
        Vec3 vel = this.getDeltaMovement();
        double speed = vel.length();
        if (speed < 1.0E-4) {
            return;
        }
        Vec3 dir = vel.scale(1.0D / speed);
        int steps = (int) Math.ceil(speed / FLY_STEP);
        double stepLen = speed / steps;
        Vec3 pos = this.position();
        for (int i = 0; i < steps; i++) {
            Vec3 from = pos;
            Vec3 to = from.add(dir.x * stepLen, dir.y * stepLen, dir.z * stepLen);
            if (this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS) {
                spawnHealSparks(from);
                this.discard();
                return;
            }
            this.setPos(to.x, to.y, to.z);
            LivingEntity hit = findHealTarget();
            if (hit != null) {
                healEntity(hit);
                this.discard();
                return;
            }
            pos = to;
        }
        double newSpeed = Math.min(speed + FLY_ACCELERATION, FLY_MAX_SPEED);
        this.setDeltaMovement(dir.scale(newSpeed));
    }

    /** 当前包围盒（外扩 0.25 格）内可治疗的活体：排除施法者本人与旁观玩家。 */
    private LivingEntity findHealTarget() {
        LivingEntity owner = resolveOwner();
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(0.25D),
                target -> target.isAlive() && target != owner && !(target instanceof Player p && p.isSpectator()));
        return targets.isEmpty() ? null : targets.get(0);
    }

    /** 命中结算：恢复 [目标最大生命值 × 0.65 + 治疗量] 点生命，并给 30 秒 1 级抗性提升。 */
    private void healEntity(LivingEntity target) {
        target.heal(target.getMaxHealth() * TARGET_HEAL_MULT + this.healBonus);
        GreenFlash.trigger(target);
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                RESISTANCE_TICKS, RESISTANCE_AMPLIFIER, false, true));
        spawnHealSparks(target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D));
    }

    /** 结算点上方撒一把绿色烟花火花（与治疗祷言同款粒子）。 */
    private void spawnHealSparks(Vec3 at) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GeneratedMod.HEAL_PRAYER_SPARK.get(),
                    at.x, at.y + 0.7D, at.z, 8, 0.35D, 0.2D, 0.35D, 0.0D);
        }
    }

    /** 渲染动画帧号：三帧贴图各停 6 游戏刻循环。 */
    public int animationFrame(float partialTicks) {
        return (int) ((this.tickCount + partialTicks) / 6.0F) % 3;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // 光球是转瞬即逝的特效实体，落盘时不保留状态（与 SpellBullet 同一口径，重载后自动消散）
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
