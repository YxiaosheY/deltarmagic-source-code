package cn.blockforge.generated.frostboundthorns;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * "粗暴碎击 / 朱红碎击" 共用投射物：先朝玩家视线方向直飞，进入 10 格内出现除发射者之外的活动实体时，
 * 就近锁定最近的目标并自动追踪，命中（或碰墙 / 超时）后消失。
 * 大小与伤害由所持斧头的实际攻击力决定：
 * 粗暴碎击 —— 伤害 = 攻击力 × 5，大小 = 攻击力 ÷ 5；
 * 朱红碎击 —— 伤害 = 攻击力 × 6.5，大小 = 攻击力 ÷ 4.5（crimson 标记为 true，换用红色贴图与拖尾）。
 */
public final class RoughSmashProjectile extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(RoughSmashProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE =
            SynchedEntityData.defineId(RoughSmashProjectile.class, EntityDataSerializers.FLOAT);
    /** 是否是"朱红碎击"弹幕（决定客户端用哪套贴图、服务端撒哪种拖尾粒子）。 */
    private static final EntityDataAccessor<Boolean> DATA_CRIMSON =
            SynchedEntityData.defineId(RoughSmashProjectile.class, EntityDataSerializers.BOOLEAN);

    /** 发现目标的范围半径（格）。 */
    private static final double ACQUIRE_RADIUS = 10.0D;
    /** 飞行速度（格 / 游戏刻）。 */
    private static final double SPEED = 1.2D;
    /** 最大存活时间（游戏刻），超过后自动消散，避免飞进虚空不消失。 */
    private static final int MAX_LIFE = 200;

    private Vec3 straightMotion = Vec3.ZERO;
    private int targetId = -1;
    private int life;

    public RoughSmashProjectile(EntityType<? extends RoughSmashProjectile> type, Level level) {
        super(type, level);
    }

    /** 设置伤害、大小、发射者；crimson 为 true 时是朱红碎击（红色贴图）。 */
    public void init(float damage, float scale, Entity owner, boolean crimson) {
        this.synchedSetDamage(damage);
        this.synchedSetScale(scale);
        this.setOwner(owner);
        this.setNoGravity(true);
        this.entityData.set(DATA_CRIMSON, crimson);
    }

    /** 是否使用朱红碎击的红色贴图 / 粒子。 */
    public boolean isCrimson() {
        return this.entityData.get(DATA_CRIMSON);
    }

    /** 给一个初始飞行方向（玩家的视线方向）。 */
    public void launch(Vec3 direction) {
        Vec3 motion = direction.normalize().scale(SPEED);
        this.straightMotion = motion;
        this.setDeltaMovement(motion);
    }

    public float getScale() {
        return this.entityData.get(DATA_SCALE);
    }

    private void synchedSetScale(float scale) {
        this.entityData.set(DATA_SCALE, scale);
    }

    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    private void synchedSetDamage(float damage) {
        this.entityData.set(DATA_DAMAGE, damage);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SCALE, 1.0F);
        this.entityData.define(DATA_DAMAGE, 10.0F);
        this.entityData.define(DATA_CRIMSON, false);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            if (!this.serverTick()) {
                return;
            }
        }
        super.tick();
    }

    /** 服务端逻辑。返回 false 表示本刻已消散，无需再走移动 / 碰撞。 */
    private boolean serverTick() {
        if (++this.life > MAX_LIFE) {
            this.discard();
            return false;
        }

        if (this.targetId == -1) {
            this.targetId = this.findNearestTarget();
        }

        if (this.targetId != -1) {
            Entity target = this.level().getEntity(this.targetId);
            if (target instanceof LivingEntity living && living.isAlive()) {
                Vec3 to = living.getEyePosition().subtract(this.position());
                if (to.lengthSqr() > 0.0001D) {
                    this.setDeltaMovement(to.normalize().scale(SPEED));
                }
            } else {
                // 目标已消失，重新进入寻找状态
                this.targetId = -1;
            }
        } else {
            // 尚未发现目标，保持直线飞行
            this.setDeltaMovement(this.straightMotion);
        }

        // 飞行轨迹用自身 beam 贴图点缀，方便看清弹道（朱红碎击用红色一套）
        if (this.level() instanceof ServerLevel serverLevel && this.life % 2 == 0) {
            serverLevel.sendParticles(this.isCrimson()
                            ? GeneratedMod.CRIMSON_SMASH_TRAIL_PARTICLE.get()
                            : GeneratedMod.ROUGH_SMASH_TRAIL_PARTICLE.get(),
                    this.getX(), this.getY(), this.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.0D);
        }
        return true;
    }

    private int findNearestTarget() {
        Entity owner = this.getOwner();
        LivingEntity best = null;
        double bestSqr = ACQUIRE_RADIUS * ACQUIRE_RADIUS;
        AABB area = this.getBoundingBox().inflate(ACQUIRE_RADIUS);
        for (Entity entity : this.level().getEntities(this, area,
                candidate -> candidate instanceof LivingEntity
                        && candidate != owner
                        && candidate.isAlive())) {
            double distanceSqr = entity.distanceToSqr(this);
            if (distanceSqr < bestSqr) {
                bestSqr = distanceSqr;
                best = (LivingEntity) entity;
            }
        }
        return best == null ? -1 : best.getId();
    }

    private void hitEntity(LivingEntity target) {
        Entity owner = this.getOwner();
        DamageSource source;
        if (owner instanceof Player player) {
            source = this.damageSources().playerAttack(player);
        } else if (owner instanceof LivingEntity livingOwner) {
            source = this.damageSources().mobProjectile(this, livingOwner);
        } else {
            source = this.damageSources().generic();
        }
        target.hurt(source, this.getDamage());
        // 命中瞬间播放 snd_rudebuster_swing.ogg（音量放大些，让远处落点也听得见）
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                GeneratedMod.RUDEBUSTER_SWING_SOUND.get(), net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 1.0F);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (result.getEntity() instanceof LivingEntity living && living.isAlive()) {
            this.hitEntity(living);
        } else {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity == this.getOwner() || entity.isSpectator() || !entity.isAlive()) {
            return false;
        }
        // 原版拾取判定之外的活体生物也要能被弹幕打中：末影龙这类
        // isPickable() 恒为 false 的生物以前会被弹体直接穿透（与法术选靶口径保持一致）。
        // 物品、经验球等非活体仍按 isPickable 排除，不会被弹幕意外撞掉。
        return entity.isPickable() || entity instanceof LivingEntity;
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("scale", this.getScale());
        tag.putFloat("damage", this.getDamage());
        tag.putInt("targetId", this.targetId);
        tag.putBoolean("crimson", this.isCrimson());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.synchedSetScale(tag.getFloat("scale"));
        this.synchedSetDamage(tag.getFloat("damage"));
        this.targetId = tag.getInt("targetId");
        this.entityData.set(DATA_CRIMSON, tag.getBoolean("crimson"));
    }
}
