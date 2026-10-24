package cn.blockforge.generated.frostboundthorns;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 玩家法术「螺旋桃击 / 方钻上冲 / 恶魔镰刀」的弹幕实体（服务端驱动，客户端只负责渲染贴片）。
 * 一个实体类型按 kind 分三种弹幕，贴图复用 Jevil 的 Undertale 风白底素材：
 * 0 桃弹（spr_clubsbullet）：出生时钉在目标外圈悬停，悬停刻数走完后朝目标生物飞去；
 * 1 方钻弹（spr_diamondbullet_vert）：从地面笔直向上冲起。
 * 2 镰刀弹（spr_joker_scythebody_ch1）：绕目标转 20 刻 → 朝中心俯冲并穿过继续飞 →
 *   飞到外圈半径后恢复环绕，约 20 刻后再次俯冲，如此循环直到 15 秒寿命结束；命中不消散，只进 20 刻受击冷却。
 * 全部无重力、穿方块，碰到生物即结算伤害（桃弹 / 方钻弹命中后消失；
 * 原版受击无敌帧被主动清掉，保证 12 枚桃弹围杀同一目标时每发都吃满伤害）。
 */
public final class SpellBullet extends Entity {
    public static final int KIND_CLUB = 0;
    public static final int KIND_DIAMOND = 1;
    public static final int KIND_SCYTHE = 2;

    /** 桃弹飞行速度（格/游戏刻），约 10 格/秒。 */
    public static final double CLUB_SPEED = 0.45D;
    /** 方钻弹上升速度（格/游戏刻），约 8 格/秒。 */
    public static final double DIAMOND_RISE_SPEED = 0.4D;
    /** 镰刀弹俯冲速度（格/游戏刻），约 11 格/秒。 */
    public static final double SCYTHE_SPEED = 0.55D;
    /** 镰刀弹环绕半径（格）：环绕段钉在这个半径上，俯冲穿出这个半径后恢复环绕。 */
    public static final double SCYTHE_ORBIT_RADIUS = 3.2D;
    /** 镰刀弹环绕角速度（弧度/游戏刻），约 28.6 刻转满一圈。 */
    public static final double SCYTHE_ORBIT_SPEED = 0.22D;
    /** 镰刀弹每一段环绕的刻数（约 20 游戏刻后再次俯冲）。 */
    public static final int SCYTHE_ORBIT_TICKS = 20;

    /** 方钻弹最长存活刻数（约 2.75 秒，冲起约 22 格后自行消散）。 */
    private static final int DIAMOND_MAX_AGE = 55;
    /** 桃弹最长存活刻数：悬停最多 19 刻 + 飞行若干刻，兜底消散。 */
    private static final int CLUB_MAX_AGE = 90;
    /** 镰刀弹总寿命：15 秒 = 300 游戏刻。 */
    private static final int SCYTHE_MAX_AGE = 300;
    /** 镰刀弹两次命中之间的最短间隔（刻），防止绕着同一只生物每刻扣 25 血。 */
    private static final int SCYTHE_HIT_COOLDOWN = 20;

    private static final EntityDataAccessor<Integer> DATA_KIND =
            SynchedEntityData.defineId(SpellBullet.class, EntityDataSerializers.INT);

    /** 命中伤害（服务端字段，随 fire() 传入，不落盘）。 */
    private float damage = 8.0F;
    /** 桃弹起飞前还要悬停的游戏刻数（方钻弹恒为 0）。 */
    private int hoverRemaining;
    /** 追踪目标（桃弹用；-1 = 无目标，起飞后沿出生朝向直线飞）。 */
    private int targetId = -1;
    /** 施法者（伤害归属；-1 = 无主）。 */
    private int ownerId = -1;
    /** 服务端生命周期计数（自毁判定用）。 */
    private int age;
    /** 镰刀弹阶段：0/2 环绕，1 俯冲（穿过中心继续飞）；环绕和俯冲交替循环。 */
    private int scythePhase;
    /** 镰刀弹当前环绕段剩余刻数。 */
    private int scytheOrbitLeft;
    /** 镰刀弹当前环绕相位角（弧度，俯视 x=cos、z=sin）。 */
    private double scytheAngle;
    /** 镰刀弹环绕圆心（跟随活目标更新；目标死亡后钉死在最后已知位置）。 */
    private Vec3 scytheAnchor = Vec3.ZERO;
    /** 镰刀弹受击冷却剩余刻数（>0 时不再结算伤害、也不消散）。 */
    private int scytheHitCooldown;

    public SpellBullet(net.minecraft.world.entity.EntityType<? extends SpellBullet> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    /**
     * 服务端发射入口。
     * hoverTicks：桃弹起飞前的悬停刻数（方钻弹传 0）；
     * initialMotion：悬停结束、目标已不在场时的兜底飞行方向（桃弹用，方钻弹传 ZERO）。
     */
    public static SpellBullet fire(Level level, double x, double y, double z, int kind,
            float damage, int hoverTicks, int targetId, int ownerId, Vec3 initialMotion) {
        SpellBullet bullet = new SpellBullet(GeneratedMod.SPELL_BULLET.get(), level);
        bullet.setPos(x, y, z);
        bullet.entityData.set(DATA_KIND, kind);
        bullet.damage = damage;
        bullet.hoverRemaining = hoverTicks;
        bullet.targetId = targetId;
        bullet.ownerId = ownerId;
        bullet.setDeltaMovement(initialMotion);
        level.addFreshEntity(bullet);
        return bullet;
    }

    /**
     * 服务端发射镰刀弹（恶魔镰刀专用）：出生在外圈 angle 方位上，
     * 先绕 center 转 {@link #SCYTHE_ORBIT_TICKS} 刻，再朝中心俯冲、穿过继续飞，
     * 飞出外圈后恢复环绕，之后循环俯冲，直到 15 秒寿命结束。
     */
    public static SpellBullet fireScythe(Level level, Vec3 center, double angle,
            float damage, int targetId, int ownerId) {
        SpellBullet bullet = new SpellBullet(GeneratedMod.SPELL_BULLET.get(), level);
        double x = center.x + SCYTHE_ORBIT_RADIUS * Math.cos(angle);
        double z = center.z + SCYTHE_ORBIT_RADIUS * Math.sin(angle);
        bullet.setPos(x, center.y, z);
        bullet.entityData.set(DATA_KIND, KIND_SCYTHE);
        bullet.damage = damage;
        bullet.targetId = targetId;
        bullet.ownerId = ownerId;
        bullet.scythePhase = 0;
        bullet.scytheOrbitLeft = SCYTHE_ORBIT_TICKS;
        bullet.scytheAngle = angle;
        bullet.scytheAnchor = center;
        level.addFreshEntity(bullet);
        return bullet;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_KIND, KIND_CLUB);
    }

    public int getKind() {
        return this.entityData.get(DATA_KIND);
    }

    private LivingEntity resolveTarget() {
        if (targetId < 0 || !(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(targetId);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    private LivingEntity resolveOwner() {
        if (ownerId < 0 || !(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(ownerId);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            // 客户端只做位置插值（原版实体同步已处理），逻辑全在服务端
            return;
        }
        this.age++;
        if (getKind() == KIND_SCYTHE) {
            tickScythe();
            return;
        }
        if (getKind() == KIND_CLUB) {
            if (this.hoverRemaining > 0) {
                // 悬停阶段：钉在出生点不动，等整圈桃弹凑齐
                this.hoverRemaining--;
                return;
            }
            LivingEntity target = resolveTarget();
            Vec3 motion = this.getDeltaMovement();
            if (target != null) {
                Vec3 dir = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                        .subtract(this.position());
                double len = dir.length();
                if (len > 1.0E-4) {
                    motion = dir.scale(CLUB_SPEED / len);
                    this.setDeltaMovement(motion);
                }
            }
            this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
            if (this.age > CLUB_MAX_AGE) {
                this.discard();
                return;
            }
        } else {
            this.setPos(this.getX(), this.getY() + DIAMOND_RISE_SPEED, this.getZ());
            if (this.age > DIAMOND_MAX_AGE) {
                this.discard();
                return;
            }
        }
        hitMobs();
    }

    /**
     * 镰刀弹时间轴（恶魔镰刀）：
     * 阶段 0/2：绕目标外圈（半径 3.2）转 20 刻，圆心跟随活目标；
     * 阶段 1：朝圆心俯冲，穿过中心后沿原方向继续直线飞，飞出外圈半径即恢复环绕；
     * 阶段 0/2 与阶段 1 交替循环，直到 15 秒寿命耗尽。
     * 命中生物扣 20 刻受击冷却但不消散——整场环绕俯冲可以反复撞不同目标。
     */
    private void tickScythe() {
        if (this.age > SCYTHE_MAX_AGE) {
            this.discard();
            return;
        }
        if (this.scytheHitCooldown > 0) {
            this.scytheHitCooldown--;
        }
        LivingEntity target = resolveTarget();
        if (target != null) {
            this.scytheAnchor = target.position()
                    .add(0.0D, target.getBbHeight() * 0.5D + 0.2D, 0.0D);
        }
        Vec3 center = this.scytheAnchor;
        if (this.scythePhase == 1) {
            Vec3 motion = this.getDeltaMovement();
            this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
            double dx = this.getX() - center.x;
            double dz = this.getZ() - center.z;
            if (dx * dx + dz * dz >= SCYTHE_ORBIT_RADIUS * SCYTHE_ORBIT_RADIUS) {
                // 穿出外圈：以当前方位角接回环绕，并重新开始这一段环绕计时
                this.scythePhase = 2;
                this.scytheOrbitLeft = SCYTHE_ORBIT_TICKS;
                this.scytheAngle = Math.atan2(dz, dx);
            }
        } else {
            this.scytheAngle += SCYTHE_ORBIT_SPEED;
            this.setPos(center.x + SCYTHE_ORBIT_RADIUS * Math.cos(this.scytheAngle),
                    center.y,
                    center.z + SCYTHE_ORBIT_RADIUS * Math.sin(this.scytheAngle));
            this.scytheOrbitLeft--;
            if (this.scytheOrbitLeft <= 0) {
                // 环绕一段时间后再次朝圆心俯冲，阶段 0 和阶段 2 都走这里
                this.scythePhase = 1;
                Vec3 dir = center.subtract(this.position());
                double len = dir.length();
                this.setDeltaMovement(len > 1.0E-4
                        ? dir.scale(SCYTHE_SPEED / len)
                        : new Vec3(SCYTHE_SPEED, 0.0D, 0.0D));
            }
        }
        if (this.scytheHitCooldown <= 0) {
            hitScythe();
        }
    }

    /** 镰刀命中：对碰到的生物扣 25 血，镰刀本身继续存在（受击冷却防每刻连扣）。 */
    private void hitScythe() {
        AABB box = this.getBoundingBox().inflate(0.15D);
        List<Mob> mobs = this.level().getEntitiesOfClass(Mob.class, box, Mob::isAlive);
        if (mobs.isEmpty()) {
            return;
        }
        Mob victim = mobs.get(0);
        DamageSource source = this.level().damageSources().indirectMagic(this, resolveOwner());
        victim.hurt(source, this.damage);
        this.scytheHitCooldown = SCYTHE_HIT_COOLDOWN;
    }

    /** 与生物 AABB 相交即结算伤害并消失（只认 Mob，玩家本就不是 Mob，天然不吃自家法术）。 */
    private void hitMobs() {
        AABB box = this.getBoundingBox().inflate(0.12D);
        List<Mob> mobs = this.level().getEntitiesOfClass(Mob.class, box, Mob::isAlive);
        if (mobs.isEmpty()) {
            return;
        }
        Mob victim = mobs.get(0);
        DamageSource source = this.level().damageSources().indirectMagic(this, resolveOwner());
        // 清掉受击无敌帧：12 枚桃弹同刻围殴同一目标时每发都吃满
        victim.invulnerableTime = 0;
        victim.hurt(source, this.damage);
        this.discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
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
        // 弹幕是转瞬即逝的特效实体，落盘时不保留状态（与 JevilBullet 同一口径）
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
