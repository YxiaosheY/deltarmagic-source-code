package cn.blockforge.generated.frostboundthorns;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 生物数据「Tiredness（疲劳值）」的服务端管理器，由法术"睡眠喷雾"的雾气累加。规则：
 * <ol>
 * <li>生物处在雾气覆盖范围内时，每游戏刻 +3（由 ModEvents 的喷雾时间轴调用 {@link #add}）；</li>
 * <li>当 Tiredness &gt; 该生物最大生命值 × 3 时，强制杀死该生物：
 *     不生成任何掉落物（{@link LivingDropsEvent} 里清空），
 *     但该生物原本会掉的经验掉落两倍（{@link net.minecraftforge.event.entity.living.LivingExperienceDropEvent} 翻倍）；</li>
 * <li>数值写在实体的 ForgeData（{@value #NBT_VALUE}）里，随实体正常存盘 / 跨区块加载保留，
 *     没有自然衰减——被喷过的生物会一直"记仇"，直到睡死或世界数据被清。</li>
 * </ol>
 * 只被 {@link #forceSleepDeath} 杀死的生物才会带上强杀标记（{@value #NBT_SLEEP_KILL}），
 * 正常死亡 / 其它伤害死亡不受掉落与经验规则影响。
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class TirednessManager {
    /** ForgeData 里的疲劳值键（公开：{@link JadeCompat} 往 Jade 同步包里也用它作键）。 */
    public static final String NBT_VALUE = "deltamagic_Tiredness";
    /** ForgeData 里的"睡死强杀"标记键：只有带标记的这次死亡才清空掉落、翻倍经验。 */
    private static final String NBT_SLEEP_KILL = "deltamagic_SleepKill";

    private TirednessManager() {
    }

    /** 读取某生物当前的 Tiredness（0 = 没被喷过）。 */
    public static int get(LivingEntity living) {
        return living.getPersistentData().getInt(NBT_VALUE);
    }

    /** 睡眠喷雾入口：给生物累加 amount 点疲劳值，超过"最大生命值 × 3"即睡死。 */
    public static void add(LivingEntity living, int amount) {
        if (living.level().isClientSide || amount <= 0 || !living.isAlive()) {
            return;
        }
        int value = get(living) + amount;
        living.getPersistentData().putInt(NBT_VALUE, value);
        if (value > living.getMaxHealth() * 3.0F) {
            forceSleepDeath(living);
        }
    }

    /**
     * 强制杀死：先清掉疲劳值并打上强杀标记（掉落 / 经验事件在死亡流程里读这个标记），
     * 再调用 kill()（genericKill 伤害类型，无视护甲、无敌帧与 Boss 免疫，保证"强制"）。
     * 经验要在这里手动结算：原版 dropExperience() 要求"最后被玩家打过"才掉经验，
     * 强杀没有伤害来源，走原版路径会掉 0 点。规则按用户设定执行——
     * 睡死的生物掉落 [该生物经验值 × 2] 的经验（仍尊重 doMobLoot 游戏规则），
     * 同时把原版经验事件置 0（见 onXpDrop），避免"先打后喷"时双份掉落。
     */
    private static void forceSleepDeath(LivingEntity living) {
        CompoundTag tag = living.getPersistentData();
        tag.remove(NBT_VALUE);
        tag.putBoolean(NBT_SLEEP_KILL, true);
        int doubledExperience = 0;
        if (living.level() instanceof ServerLevel serverLevel
                && serverLevel.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)
                && living.shouldDropExperience()
                && !living.wasExperienceConsumed()) {
            doubledExperience = living.getExperienceReward() * 2;
        }
        living.kill();
        if (doubledExperience > 0 && living.level() instanceof ServerLevel serverLevel) {
            ExperienceOrb.award(serverLevel, living.position(), doubledExperience);
        }
    }

    /** 睡死的生物：不生成任何掉落物（含装备与战利品表产物）。 */
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (isSleepKill(event.getEntity())) {
            event.getDrops().clear();
        }
    }

    /** 睡死的生物：经验已由 forceSleepDeath 手动按两倍掉落，这里把原版路径置 0 防双份。 */
    @SubscribeEvent
    public static void onXpDrop(LivingExperienceDropEvent event) {
        if (isSleepKill(event.getEntity())) {
            event.setDroppedExperience(0);
        }
    }

    /** 本次死亡是否由 Tiredness 强杀触发。 */
    private static boolean isSleepKill(LivingEntity living) {
        return living.getPersistentData().getBoolean(NBT_SLEEP_KILL);
    }
}
