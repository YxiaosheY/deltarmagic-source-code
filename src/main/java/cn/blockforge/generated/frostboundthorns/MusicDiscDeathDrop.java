package cn.blockforge.generated.frostboundthorns;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 唱片的生存获取途径：玩家死亡瞬间，「唱片 4 · Ohhhhohohoho」有 5% 的概率
 * 直接掉在死亡位置附近。
 *
 * 说明两点设计决定：
 * <ul>
 * <li>这张唱片是"死亡的奖励"，不是背包物品的归还——所以 keepInventory
 * （保留物品栏）规则开着也照样掉；</li>
 * <li>「唱片 30 · Lost Girl」按当前规划暂不加入生存获取途径，
 * 只能从创造物品栏拿到，本类不处理它。</li>
 * </ul>
 *
 * 判定挂在 {@link LivingDeathEvent} 上（服务端死亡瞬间触发一次），
 * 不挂 PlayerDropsEvent：那个事件在 keepInventory 开启时根本不触发，
 * 而且玩家物品还没落地，往里塞东西时机不合适。
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class MusicDiscDeathDrop {
    /** 玩家死亡时掉落唱片的概率：5%。 */
    private static final float CHANCE = 0.05F;

    private MusicDiscDeathDrop() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 只管服务端、只管玩家；生物死亡不掉唱片
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level) || level.isClientSide) {
            return;
        }
        if (level.random.nextFloat() >= CHANCE) {
            return;
        }
        ItemStack disc = new ItemStack(GeneratedMod.MUSIC_DISC_OHHHH_OHOHOHO.get());
        ItemEntity drop = new ItemEntity(level,
                player.getX(), player.getY() + 0.5D, player.getZ(), disc);
        // 给一点随机散开速度：死在同一坐标的多张唱片不会叠成一坨捡不起来
        drop.setDeltaMovement((level.random.nextDouble() - 0.5D) * 0.3D,
                0.2D, (level.random.nextDouble() - 0.5D) * 0.3D);
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
    }
}
