package cn.blockforge.generated.frostboundthorns;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Jevil 的弹幕实体。一个实体类型按 kind 分四种弹幕（渲染与伤害都走同一套逻辑）：
 * 0 梅花弹（直线飞行，3.5 伤害）、1 菱形弹（从天而降砸地，5 伤害）、
 * 2 镰刀（追踪玩家，技能里 10 / 狂暴阶段 15 伤害）、3 旋转木马（绕玩家转圈 3 秒，8 伤害）。
 * 全部无重力、穿方块、碰到玩家即结算伤害（原版受击无敌帧自动节流）。
 */
public final class JevilBullet extends Entity {
    public static final int KIND_CLUB = 0;
    public static final int KIND_DIAMOND = 1;
    public static final int KIND_SCYTHE = 2;
    public static final int KIND_CAROUSEL = 3;

    private static final EntityDataAccessor<Integer> DATA_KIND =
            SynchedEntityData.defineId(JevilBullet.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE =
            SynchedEntityData.defineId(JevilBullet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_TARGET =
            SynchedEntityData.defineId(JevilBullet.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(JevilBullet.class, EntityDataSerializers.OPTIONAL_UUID);

    /** 服务端生命周期计数（自毁 / 旋转木马的圈数动画都用它）。 */
    private int age;
    /** 旋转木马的相位角（每颗错开，均匀围成一圈）。 */
    private float phase;

    public JevilBullet(EntityType<? extends JevilBullet> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    /** 服务端发射入口：kind 决定外观与行为，damage 为命中伤害，target 为追踪/环绕的玩家。 */
    public static JevilBullet fire(Level level, double x, double y, double z, int kind,
            float damage, Vec3 motion, Player target, Entity owner) {
        JevilBullet bullet = new JevilBullet(GeneratedMod.JEVIL_BULLET.get(), level);
        bullet.setPos(x, y, z);
        bullet.entityData.set(DATA_KIND, kind);
        bullet.entityData.set(DATA_DAMAGE, damage);
        bullet.entityData.set(DATA_TARGET, target == null ? Optional.empty() : Optional.of(target.getUUID()));
        bullet.entityData.set(DATA_OWNER, owner == null ? Optional.empty() : Optional.of(owner.getUUID()));
        bullet.setDeltaMovement(motion);
        level.addFreshEntity(bullet);
        return bullet;
    }

    /** 旋转木马：设置这一颗在圈上的初始相位（服务端调用，位置每刻重算后同步给客户端）。 */
    public void setCarouselPhase(float phase) {
        this.phase = phase;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_KIND, KIND_CLUB);
        this.entityData.define(DATA_DAMAGE, 3.5F);
        this.entityData.define(DATA_TARGET, Optional.empty());
        this.entityData.define(DATA_OWNER, Optional.empty());
    }

    public int getKind() {
        return this.entityData.get(DATA_KIND);
    }

    public float getBulletDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    private Player resolveTarget() {
        UUID id = this.entityData.get(DATA_TARGET).orElse(null);
        if (id == null || !(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(id);
        return entity instanceof Player player && player.isAlive() ? player : null;
    }

    private LivingEntity resolveOwner() {
        UUID id = this.entityData.get(DATA_OWNER).orElse(null);
        if (id == null || !(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(id);
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
        int kind = this.getKind();
        switch (kind) {
            case KIND_CLUB -> {
                this.setPos(this.getX() + this.getDeltaMovement().x,
                        this.getY() + this.getDeltaMovement().y,
                        this.getZ() + this.getDeltaMovement().z);
                if (this.age > 160 || this.insideSolid()) {
                    this.discard();
                    return;
                }
            }
            case KIND_DIAMOND -> {
                Vec3 motion = this.getDeltaMovement();
                double vy = motion.y - 0.045F;
                this.setPos(this.getX(), this.getY() + vy, this.getZ());
                this.setDeltaMovement(motion.x, vy, motion.z);
                if (this.onSolidBelow() || this.age > 160) {
                    // 砸地：以落点为中心 1.7 格内的玩家都吃一发
                    hitPlayers(1.7D);
                    this.discard();
                    return;
                }
            }
            case KIND_SCYTHE -> {
                Player target = this.resolveTarget();
                if (target != null) {
                    Vec3 dir = target.getEyePosition().subtract(this.position());
                    double len = dir.length();
                    if (len > 1.0E-4) {
                        this.setDeltaMovement(dir.scale(0.45 / len));
                    }
                }
                this.setPos(this.getX() + this.getDeltaMovement().x,
                        this.getY() + this.getDeltaMovement().y,
                        this.getZ() + this.getDeltaMovement().z);
                if (this.age > 140) {
                    this.discard();
                    return;
                }
            }
            case KIND_CAROUSEL -> {
                Player target = this.resolveTarget();
                if (target == null || this.age > 60) {
                    this.discard();
                    return;
                }
                // 绕玩家转圈：半径 2.5，3 秒整（60 刻）
                double angle = this.phase + this.age * 0.16D;
                this.setPos(target.getX() + Math.cos(angle) * 2.5D,
                        target.getY() + 0.6D + Math.sin(this.age * 0.35D) * 0.15D,
                        target.getZ() + Math.sin(angle) * 2.5D);
                hitPlayers(0.0D);
                return;
            }
            default -> {
            }
        }
        hitPlayers(0.0D);
    }

    /** 与玩家 AABB 相交即结算伤害（菱形弹砸地时传 1.7 做范围判定）。 */
    private void hitPlayers(double extraRadius) {
        AABB box;
        if (extraRadius > 0.0D) {
            box = new AABB(this.getX() - extraRadius, this.getY() - extraRadius, this.getZ() - extraRadius,
                    this.getX() + extraRadius, this.getY() + extraRadius, this.getZ() + extraRadius);
        } else {
            box = this.getBoundingBox();
        }
        List<Player> players = this.level().getEntitiesOfClass(Player.class, box.inflate(0.1D));
        for (Player player : players) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            DamageSource source = this.level().damageSources().mobAttack(this.resolveOwner());
            player.hurt(source, this.getBulletDamage());
            // 梅花弹 / 镰刀命中即碎；菱形弹砸地后本来就要消失；木马命中不消失（持续骚扰）
            if (getKind() == KIND_CLUB || getKind() == KIND_SCYTHE) {
                this.discard();
                return;
            }
        }
    }

    private boolean insideSolid() {
        BlockState state = this.level().getBlockState(this.blockPosition());
        return state.isSolidRender(this.level(), this.blockPosition());
    }

    private boolean onSolidBelow() {
        BlockState state = this.level().getBlockState(this.blockPosition().below());
        return state.isSolidRender(this.level(), this.blockPosition().below());
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
        // 弹幕是转瞬即逝的特效实体，落盘时不保留状态
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
