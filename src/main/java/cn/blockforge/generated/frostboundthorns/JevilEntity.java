package cn.blockforge.generated.frostboundthorns;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Jevil（小丑）Boss，血量 350，掉落 350 经验。行为按设计大纲（jevil.txt）实现：
 * 1. 玩家靠近 5 格内：立刻开始围绕玩家高速旋转，并一次性播放 snd_joker_anything；
 *    同时以它为中心奏响战斗曲 joker.ogg（大音量、随距离衰减，约 101 秒一曲，
 *    战斗不打完就一直循环播下一遍；Jevil 死亡时立刻掐掉）。
 *    开场先围绕玩家转 8 秒（期间不放技能），随后瞬移到距离玩家约 8 格的位置，
 *    在那里绕着该点继续旋转；此后每隔 20~30 秒再随机换一个新的“约 8 格”落点。
 * 2. 战斗中循环使用四个技能（梅花弹雨 / 菱形弹砸落 / 镰刀隐身突袭 / 旋转木马包围圈）。
 * 3. 血量低于 100：播放 neochaos，进入 30 秒无敌的镰刀狂暴阶段。
 * 4. 受击随机大笑（ha0 / ha1）。
 * 5. 致命一击被拦截：先播放 laugh1、剧烈抖动、逐渐缩小到 0，最后才真正消失并掉经验。
 * 它没有近战、不吃坠落 / 卡墙 / 溺水伤害，也不会自然刷新或 despawn。
 */
public final class JevilEntity extends Mob {
    /** 死亡动画总时长（刻）：前 3 秒狂抖，后 2 秒缩到 0。 */
    private static final int DEATH_TICKS = 100;
    /** 开场围绕玩家旋转的时长（刻）：8 秒。 */
    private static final int INTRO_ORBIT_TICKS = 160;
    /** 定点旋转阶段：每隔 20~30 秒重新选一个距玩家约 8 格的落点。 */
    private static final int RELOCATE_MIN_TICKS = 400;
    private static final int RELOCATE_SPAN_TICKS = 200;
    /** 定点旋转时绕落点的小圈半径（格）。 */
    private static final double ANCHOR_ORBIT_RADIUS = 2.5D;
    /**
     * 战斗曲循环周期（刻）：joker.ogg 实测约 101.05 秒（44100Hz，last granule 4456421），
     * 即 2021.05 刻，取 2021 刻——一曲刚播完就紧接着起下一遍，听不出接缝。
     */
    private static final int MUSIC_PERIOD_TICKS = 2021;
    /** 四个常规技能 + 狂暴阶段的持续时间（刻）。 */
    private static final int[] SKILL_DURATION = {0, 200, 300, 100, 80, 600};
    /** 限伤：Jevil 每次受击最多只结算这么多点伤害（一刀 999 也只掉 40 血）。 */
    private static final float MAX_DAMAGE_PER_HIT = 40.0F;

    private static final EntityDataAccessor<Float> DATA_Y_SCALE =
            SynchedEntityData.defineId(JevilEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(JevilEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_DYING =
            SynchedEntityData.defineId(JevilEntity.class, EntityDataSerializers.BOOLEAN);

    /** 顶部紫色 Boss 血条（跟随原版“看见即显示”规则）。 */
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    /** 当前技能：0 待机轮换，1-4 大纲四技能，5 血量<100 的镰刀狂暴。 */
    private int skillId;
    private int skillTicks;
    private int skillDuration;
    private int cooldownTicks = 40;
    private int nextSkill = 1;
    /** 隐身无敌 / 狂暴无敌标记（服务端逻辑，不进伤害判定白名单）。 */
    private boolean invulnerable;
    private boolean introDone;
    private boolean phase2Started;
    private boolean dying;
    private int deathTicks;
    /** 公转相位角（弧度），每刻递增，同时驱动身体自转。 */
    private float orbitAngle;
    /** 开场 8 秒绕玩家公转的剩余刻数（进入定点阶段后置 0）。 */
    private int introOrbitTicksLeft;
    /** 是否已进入“绕锚点定点旋转”阶段。 */
    private boolean anchorMode;
    /** 下一次换位的倒计时（刻），仅在 anchorMode 下递减。 */
    private int relocateTicksLeft;
    /** 定点旋转的锚点：距离玩家约 8 格的随机落点。 */
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    /** 战斗曲下一次重播的倒计时（刻）；0 表示"该起下一遍了"。 */
    private int musicTicksLeft;
    /** 战斗中暂时没人靠近的标记：玩家一回来立刻重新奏响战斗曲。 */
    private boolean musicPausedForAbsence;
    /** 给出致命一击的玩家（掉落判定用；非玩家击杀为 null）。 */
    private java.util.UUID killerId;
    /** 暗影水晶"每人只掉一次"记在玩家持久数据里的键。 */
    public static final String SHADOW_CRYSTAL_DEFEATED_KEY = "DeltamagicJevilDefeated";

    public JevilEntity(EntityType<? extends JevilEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.setCanPickUpLoot(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 350.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_Y_SCALE, 1.0F);
        this.entityData.define(DATA_SCALE, 1.0F);
        this.entityData.define(DATA_DYING, false);
    }

    // ------------------------------------------------------------------
    // 客户端渲染读取的三个可视状态（y 压缩 / 整体缩放 / 死亡抖动）
    // ------------------------------------------------------------------
    public float getVisualYScale() {
        return this.entityData.get(DATA_Y_SCALE);
    }

    public float getVisualScale() {
        return this.entityData.get(DATA_SCALE);
    }

    public boolean isDyingAnimation() {
        return this.entityData.get(DATA_DYING);
    }

    private void setVisualScale(float scale) {
        this.entityData.set(DATA_SCALE, scale);
    }

    private void setVisualYScale(float yScale) {
        this.entityData.set(DATA_Y_SCALE, yScale);
    }

    // ------------------------------------------------------------------
    // 基础规则
    // ------------------------------------------------------------------
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.dying || this.invulnerable
                || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.FALL)) {
            return false;
        }
        // 限伤：单次伤害封顶 40 点，后面的致命判定也用封顶后的数值
        if (amount > MAX_DAMAGE_PER_HIT) {
            amount = MAX_DAMAGE_PER_HIT;
        }
        if (amount >= this.getHealth()) {
            // 大纲第 5 条：致命一击先转成"大笑 + 抖动 + 缩小"的死亡动画
            this.captureKiller(source);
            this.startDeath();
            return false;
        }
        boolean flag = super.hurt(source, amount);
        if (flag && !this.level().isClientSide) {
            // 大纲第 4 条：受击随机 ha0 / ha1
            this.playSound(this.level().getRandom().nextBoolean()
                    ? GeneratedMod.JEVIL_HA0.get() : GeneratedMod.JEVIL_HA1.get(), 3.0F, 1.0F);
        }
        return flag;
    }

    @Override
    public void die(DamageSource source) {
        this.captureKiller(source);
        if (!this.dying) {
            this.startDeath();
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
            net.minecraft.world.entity.MobSpawnType reason) {
        // 只在 jevil_room 结构里由脚本生成，绝不自然刷新
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public int getExperienceReward() {
        // 经验在死亡动画结束时手动掉落（350 点）
        return 0;
    }

    @Override
    protected void dropFromLootTable(DamageSource source, boolean recentlyHit) {
        // 大纲第 6 条：掉落物暂不设计（连默认战利品表也不掉）
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("JevilDying", this.dying);
        if (this.killerId != null) {
            tag.putUUID("JevilKiller", this.killerId);
        }
        tag.putInt("JevilDeathTicks", this.deathTicks);
        tag.putBoolean("JevilIntroDone", this.introDone);
        tag.putBoolean("JevilPhase2", this.phase2Started);
        tag.putInt("JevilIntroOrbitTicks", this.introOrbitTicksLeft);
        tag.putBoolean("JevilAnchorMode", this.anchorMode);
        tag.putInt("JevilRelocateTicks", this.relocateTicksLeft);
        tag.putDouble("JevilAnchorX", this.anchorX);
        tag.putDouble("JevilAnchorY", this.anchorY);
        tag.putDouble("JevilAnchorZ", this.anchorZ);
        tag.putInt("JevilMusicTicks", this.musicTicksLeft);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.dying = tag.getBoolean("JevilDying");
        if (tag.hasUUID("JevilKiller")) {
            this.killerId = tag.getUUID("JevilKiller");
        }
        this.deathTicks = tag.getInt("JevilDeathTicks");
        this.introDone = tag.getBoolean("JevilIntroDone");
        this.phase2Started = tag.getBoolean("JevilPhase2");
        this.introOrbitTicksLeft = tag.getInt("JevilIntroOrbitTicks");
        this.anchorMode = tag.getBoolean("JevilAnchorMode");
        this.relocateTicksLeft = tag.getInt("JevilRelocateTicks");
        this.anchorX = tag.getDouble("JevilAnchorX");
        this.anchorY = tag.getDouble("JevilAnchorY");
        this.anchorZ = tag.getDouble("JevilAnchorZ");
        this.musicTicksLeft = tag.getInt("JevilMusicTicks");
        if (this.introDone && !this.dying) {
            // 区块重载 / 退出重进后，客户端上那遍曲子早就没了：
            // 战斗还在打，2 刻后立刻把战斗曲重新奏响
            this.musicTicksLeft = 2;
        }
        if (this.introDone && !this.anchorMode && this.introOrbitTicksLeft <= 0) {
            // 旧存档没有这套新字段：直接当作开场旋转已结束
            this.introOrbitTicksLeft = 1;
        }
        if (this.dying) {
            this.entityData.set(DATA_DYING, true);
        }
    }

    // ------------------------------------------------------------------
    // 主循环
    // ------------------------------------------------------------------
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        ServerBossEvent event = this.bossEvent;
        event.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.dying) {
            this.tickDeathAnimation();
            return;
        }
        Player target = this.level().getNearestPlayer(this, 48.0D);
        if (target == null || !target.isAlive()) {
            if (this.skillId != 0) {
                this.endSkill();
            }
            this.setDeltaMovement(Vec3.ZERO);
            if (this.introDone) {
                this.musicPausedForAbsence = true;
            }
            return;
        }
        if (this.introDone) {
            // 战斗曲修复：一曲（约 101 秒）播完自动接下一遍；玩家离开又回来时立刻重新奏响
            if (this.musicPausedForAbsence) {
                this.musicPausedForAbsence = false;
                this.startBattleMusic();
            } else if (--this.musicTicksLeft <= 0) {
                this.startBattleMusic();
            }
        }
        if (!this.introDone) {
            if (this.distanceToSqr(target) <= 25.0D) {
                // 大纲第 1 条：靠近 5 格内立刻开始围绕玩家旋转 8 秒 + 一次性 anything + 战斗曲
                this.introDone = true;
                this.anchorMode = false;
                this.introOrbitTicksLeft = INTRO_ORBIT_TICKS;
                this.musicPausedForAbsence = false;
                this.playSound(GeneratedMod.JEVIL_ANYTHING.get(), 4.0F, 1.0F);
                this.startBattleMusic();
            } else {
                return;
            }
        }
        this.tickCombat(target);
    }

    /** 围绕玩家公转 + 自转，并按冷却轮换技能。 */
    private void tickCombat(Player target) {
        // ---- 移动阶段：开场 8 秒绕玩家转，之后绕随机锚点转并每 20~30 秒换位 ----
        double cx;
        double cy;
        double cz;
        double radius;
        if (!this.anchorMode) {
            cx = target.getX();
            cy = target.getY() + 1.3D;
            cz = target.getZ();
            radius = this.skillId == 1 ? 10.0D : 6.0D;
            if (this.introOrbitTicksLeft > 0 && --this.introOrbitTicksLeft == 0) {
                // 8 秒绕场结束：选定第一个锚点，进入定点旋转阶段
                this.anchorMode = true;
                this.pickAnchor(target);
            }
        } else {
            if (--this.relocateTicksLeft <= 0) {
                // 到点：在玩家半径约 8 格外重选一个新落点，滑过去继续转
                this.pickAnchor(target);
                this.playSound(GeneratedMod.JEVIL_BYEBYE.get(), 2.0F, 1.4F);
            }
            cx = this.anchorX;
            cy = this.anchorY;
            cz = this.anchorZ;
            radius = ANCHOR_ORBIT_RADIUS;
        }

        this.orbitAngle += this.skillId == 1 ? 0.30F : 0.22F;
        double wx = cx + Math.cos(this.orbitAngle) * radius;
        double wz = cz + Math.sin(this.orbitAngle) * radius;
        double wy = cy + Math.sin(this.orbitAngle * 2.0F) * 0.4D;
        this.setDeltaMovement(
                (wx - this.getX()) * 0.10D,
                Mth.clamp((wy - this.getY()) * 0.10D, -0.35D, 0.35D),
                (wz - this.getZ()) * 0.10D);
        this.hasImpulse = true;
        // 身体高速自转（公转的 3 倍速）
        float spin = (float) Math.toDegrees(this.orbitAngle) * 3.0F;
        this.setYRot(spin % 360.0F);
        this.yBodyRot = this.getYRot();
        this.setXRot(0.0F);

        if (this.skillId == 0) {
            if (!this.phase2Started && this.getHealth() < 100.0F) {
                this.startPhase2();
                return;
            }
            if (!this.anchorMode) {
                // 开场 8 秒纯粹绕玩家旋转，不放技能
                return;
            }
            if (this.cooldownTicks > 0) {
                this.cooldownTicks--;
                return;
            }
            this.startSkill(this.nextSkill, target);
            this.nextSkill = this.nextSkill % 4 + 1;
            return;
        }

        this.skillTicks--;
        int elapsed = this.skillDuration - this.skillTicks;
        switch (this.skillId) {
            case 1 -> {
                // 技能①：拉开距离旋转，持续 10 秒吐梅花弹（3.5 伤害）
                if (elapsed % 12 == 0) {
                    this.fireClub(target);
                }
                if (elapsed > 0 && elapsed % 40 == 0) {
                    this.playSound(GeneratedMod.JEVIL_OH.get(), 3.0F, 1.0F);
                }
            }
            case 2 -> {
                // 技能②：玩家上方 5 格、5x5 范围内随机砸菱形弹（5 伤害），持续 15 秒
                if (elapsed % 8 == 0) {
                    this.spawnDiamond(target);
                }
            }
            case 3 -> {
                // 技能③：y 压缩 1 秒 → 隐身无敌 → 四方镰刀（10 伤害）→ 恢复
                if (elapsed <= 20) {
                    this.setVisualYScale(Math.max(0.0F, 1.0F - elapsed / 20.0F));
                }
                if (elapsed == 20) {
                    this.setInvisible(true);
                    this.invulnerable = true;
                    this.spawnScytheRing(target, 10.0F);
                }
                if (elapsed >= 80) {
                    this.setInvisible(false);
                    this.invulnerable = false;
                    this.setVisualYScale(Math.min(1.0F, (elapsed - 80) / 20.0F));
                }
            }
            case 4 -> {
                // 技能④：一圈旋转木马绕住玩家（8 伤害，持续 3 秒）——弹体在 startSkill 里已放出
            }
            case 5 -> {
                // 狂暴：30 秒无敌 + 镰刀连续砸向玩家（15 伤害）
                if (elapsed % 15 == 0) {
                    this.spawnScytheRandom(target, 15.0F);
                }
            }
            default -> {
            }
        }
        if (this.skillTicks <= 0) {
            this.endSkill();
        }
    }

    /** 随机选一个距离玩家半径约 8 格（7~9 格浮动）的落点，并排好下一次换位的倒计时。 */
    private void pickAnchor(Player target) {
        net.minecraft.util.RandomSource random = this.level().getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = 7.0D + random.nextDouble() * 2.0D;
        this.anchorX = target.getX() + Math.cos(angle) * distance;
        this.anchorZ = target.getZ() + Math.sin(angle) * distance;
        this.anchorY = target.getY() + 1.3D;
        // 20~30 秒后再换下一个位置
        this.relocateTicksLeft = RELOCATE_MIN_TICKS + random.nextInt(RELOCATE_SPAN_TICKS);
    }

    private void startSkill(int skill, Player target) {
        this.skillId = skill;
        this.skillDuration = SKILL_DURATION[skill];
        this.skillTicks = this.skillDuration;
        switch (skill) {
            case 1 -> this.playSound(GeneratedMod.JEVIL_OH.get(), 3.0F, 1.0F);
            case 2 -> this.playSound(GeneratedMod.JEVIL_CHAOS.get(), 3.0F, 1.0F);
            case 3 -> this.playSound(GeneratedMod.JEVIL_BYEBYE.get(), 3.0F, 1.0F);
            case 4 -> {
                this.playSound(GeneratedMod.JEVIL_ANYTHING.get(), 3.0F, 1.0F);
                this.spawnCarouselRing(target);
            }
            default -> {
            }
        }
    }

    private void endSkill() {
        this.skillId = 0;
        this.skillTicks = 0;
        this.cooldownTicks = 40;
        this.invulnerable = false;
        this.setInvisible(false);
        this.setVisualYScale(1.0F);
    }

    private void startPhase2() {
        this.phase2Started = true;
        this.skillId = 5;
        this.skillDuration = SKILL_DURATION[5];
        this.skillTicks = this.skillDuration;
        this.invulnerable = true;
        this.playSound(GeneratedMod.JEVIL_NEOCHAOS.get(), 4.0F, 1.0F);
    }

    /**
     * 重新奏响战斗曲：先掐掉可能还挂在客户端上的旧半截（防止两遍重叠），
     * 再播一遍 joker.ogg，并把循环计时器归位（约 101 秒后再起下一遍）。
     */
    private void startBattleMusic() {
        this.stopBattleMusic();
        this.musicTicksLeft = MUSIC_PERIOD_TICKS;
        this.level().playSound(null, this.blockPosition(), GeneratedMod.JEVIL_MUSIC.get(),
                net.minecraft.sounds.SoundSource.RECORDS, 12.0F, 1.0F);
    }

    /** 给本维度所有玩家发“停掉这一个音效”的包（等价 /stopsound），只影响 joker_music。 */
    private void stopBattleMusic() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        net.minecraft.network.protocol.game.ClientboundStopSoundPacket packet =
                new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                        GeneratedMod.JEVIL_MUSIC.getId(), net.minecraft.sounds.SoundSource.RECORDS);
        for (ServerPlayer player : serverLevel.getPlayers(p -> true)) {
            player.connection.send(packet);
        }
    }

    // ------------------------------------------------------------------
    // 弹幕发射
    // ------------------------------------------------------------------
    private void fireClub(Player target) {
        Vec3 eye = this.getEyePosition();
        Vec3 dir = target.getEyePosition().subtract(eye);
        double len = dir.length();
        if (len < 1.0E-4) {
            return;
        }
        JevilBullet.fire(this.level(), eye.x, eye.y, eye.z,
                JevilBullet.KIND_CLUB, 3.5F, dir.scale(0.4D / len), target, this);
    }

    private void spawnDiamond(Player target) {
        net.minecraft.util.RandomSource random = this.level().getRandom();
        double x = target.getX() + (random.nextDouble() * 5.0D - 2.5D);
        double z = target.getZ() + (random.nextDouble() * 5.0D - 2.5D);
        double y = target.getY() + 5.0D + random.nextDouble();
        JevilBullet.fire(this.level(), x, y, z, JevilBullet.KIND_DIAMOND, 5.0F, Vec3.ZERO, target, this);
    }

    /** 技能③：从东南西北四个方向各放一把追踪镰刀。 */
    private void spawnScytheRing(Player target, float damage) {
        double[][] offsets = {{8.0D, 0.0D}, {-8.0D, 0.0D}, {0.0D, 8.0D}, {0.0D, -8.0D}};
        for (double[] offset : offsets) {
            Vec3 pos = target.position().add(offset[0], 2.5D, offset[1]);
            Vec3 dir = target.getEyePosition().subtract(pos);
            double len = dir.length();
            JevilBullet.fire(this.level(), pos.x, pos.y, pos.z, JevilBullet.KIND_SCYTHE, damage,
                    len < 1.0E-4 ? Vec3.ZERO : dir.scale(0.45D / len), target, this);
        }
    }

    /** 狂暴阶段：绕 Boss 随机角度放一把追踪镰刀（15 伤害）。 */
    private void spawnScytheRandom(Player target, float damage) {
        double angle = this.level().getRandom().nextDouble() * Math.PI * 2.0D;
        Vec3 pos = this.position().add(Math.cos(angle) * 6.0D, 1.5D, Math.sin(angle) * 6.0D);
        Vec3 dir = target.getEyePosition().subtract(pos);
        double len = dir.length();
        JevilBullet.fire(this.level(), pos.x, pos.y, pos.z, JevilBullet.KIND_SCYTHE, damage,
                len < 1.0E-4 ? Vec3.ZERO : dir.scale(0.45D / len), target, this);
    }

    /** 技能④：8 颗旋转木马绕住玩家（相位错开，弹体自己维持圆周）。 */
    private void spawnCarouselRing(Player target) {
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0D;
            Vec3 pos = target.position().add(Math.cos(angle) * 2.5D, 0.6D, Math.sin(angle) * 2.5D);
            JevilBullet bullet = JevilBullet.fire(this.level(), pos.x, pos.y, pos.z,
                    JevilBullet.KIND_CAROUSEL, 8.0F, Vec3.ZERO, target, this);
            bullet.setCarouselPhase((float) angle);
        }
    }

    // ------------------------------------------------------------------
    // 死亡动画（大纲第 5 条）
    // ------------------------------------------------------------------
    private void startDeath() {
        this.dying = true;
        this.deathTicks = DEATH_TICKS;
        this.musicTicksLeft = 0;
        this.musicPausedForAbsence = false;
        this.entityData.set(DATA_DYING, true);
        this.setDeltaMovement(Vec3.ZERO);
        this.endSkill();
        // 战斗结束：把还在放的那半截战斗曲掐掉，别让它盖过胜利后的安静
        this.stopBattleMusic();
        this.playSound(GeneratedMod.JEVIL_LAUGH1.get(), 4.0F, 1.0F);
    }

    private void tickDeathAnimation() {
        this.deathTicks--;
        if (this.deathTicks <= 0) {
            this.dropJevilLoot();
            this.level().addFreshEntity(new ExperienceOrb(
                    this.level(), this.getX(), this.getY(), this.getZ(), 350));
            this.discard();
            return;
        }
        // 后 40 刻逐渐缩到 0；全程剧烈抖动（抖动由渲染器按 isDyingAnimation 实现）
        this.setVisualScale(Math.max(0.0F, this.deathTicks / 40.0F));
        this.setDeltaMovement(Vec3.ZERO);
    }

    /** 记下给出致命一击的玩家（法术弹幕的伤害源也能追溯到施法者本人）。 */
    private void captureKiller(DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            this.killerId = player.getUUID();
        }
    }

    /**
     * Jevil 掉落（死亡动画结束时结算）：
     * 三本专属附魔书——螺旋桃击 / 方钻上冲 / 恶魔镰刀，每次击杀各掉 1 本；
     * 丑魔锭——每次击杀 80% 概率掉 1 个；
     * 暗影水晶——只有"第一次击败 Jevil"的那个玩家能拿到（记在该玩家的持久数据里，
     * 同一玩家再打倒 Jevil 就不掉了，换个没打过的玩家来补刀则算他的首杀）。
     */
    private void dropJevilLoot() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        spawnBook(serverLevel, GeneratedMod.SPIRAL_CLUBS.get());
        spawnBook(serverLevel, GeneratedMod.DIAMOND_UPPERCUT.get());
        spawnBook(serverLevel, GeneratedMod.JEVIL_SCYTHE.get());
        // 丑魔锭：80% 掉一个（掉在尸体位置，谁都能捡，不绑定击杀者）
        if (this.random.nextFloat() < 0.8F) {
            net.minecraft.world.entity.item.ItemEntity ingot = new net.minecraft.world.entity.item.ItemEntity(
                    this.level(), this.getX(), this.getY() + 0.5D, this.getZ(),
                    new net.minecraft.world.item.ItemStack(GeneratedMod.JEVIL_INGOT.get()));
            ingot.setPickUpDelay(40);
            this.level().addFreshEntity(ingot);
        }
        ServerPlayer killer = null;
        if (this.killerId != null
                && serverLevel.getPlayerByUUID(this.killerId) instanceof ServerPlayer player) {
            killer = player;
        }
        if (killer == null) {
            return;
        }
        CompoundTag persistent = killer.getPersistentData();
        if (!persistent.getBoolean(SHADOW_CRYSTAL_DEFEATED_KEY)) {
            persistent.putBoolean(SHADOW_CRYSTAL_DEFEATED_KEY, true);
            net.minecraft.world.entity.item.ItemEntity crystal = new net.minecraft.world.entity.item.ItemEntity(
                    this.level(), this.getX(), this.getY() + 0.5D, this.getZ(),
                    new net.minecraft.world.item.ItemStack(GeneratedMod.SHADOW_CRYSTAL.get()));
            crystal.setPickUpDelay(40); // 晚 2 秒才能捡，避免和死亡动画挤在一起
            this.level().addFreshEntity(crystal);
        }
    }

    /** 在尸体位置掉一本带指定附魔的附魔书。 */
    private void spawnBook(ServerLevel serverLevel,
            net.minecraft.world.item.enchantment.Enchantment enchantment) {
        net.minecraft.world.entity.item.ItemEntity book = new net.minecraft.world.entity.item.ItemEntity(
                this.level(), this.getX(), this.getY() + 0.5D, this.getZ(),
                net.minecraft.world.item.EnchantedBookItem.createForEnchantment(
                        new net.minecraft.world.item.enchantment.EnchantmentInstance(enchantment, 1)));
        book.setPickUpDelay(40);
        this.level().addFreshEntity(book);
    }
}
