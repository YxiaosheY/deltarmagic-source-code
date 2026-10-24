package cn.blockforge.generated.frostboundthorns;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * "睡眠喷雾"附魔书的村民来源：每个图书管理员有 15% 的概率出售这本书。
 * Forge 的 VillagerTradesEvent 只在服务器启动时按职业触发一次（改的是全局交易池），
 * 而村民每升一级才从池子里随机抽 2 条报价——挂进池子做不到"逐村民 15%"。
 * 所以这里直接盯着村民：每当遇到一个还没掷过骰的图书管理员，就掷一次 15%，
 * 通过则往它的交易列表里追加一条"8 绿宝石 → 睡眠喷雾附魔书"的报价。
 * 掷骰结果写进村民自己的 ForgeData（转职清空交易后也认账、跨存档不重掷），
 * 报价本身随村民 NBT 正常存盘；若交易列表被原版重建（转职），已通过的村民会自动补回报价。
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class SleepSprayTradeManager {
    /** 每个图书管理员出售该书的概率：15%。 */
    private static final float CHANCE = 0.15F;
    /** 售价（绿宝石）与交易参数，参考原版图书管理员附魔书档位。 */
    private static final int PRICE_EMERALDS = 8;
    private static final int MAX_USES = 8;
    private static final int VILLAGER_XP = 2;
    /** 检查节奏：每 10 游戏刻扫一遍即可，报价晚出现零点几秒玩家看不出来。 */
    private static final int CHECK_INTERVAL = 10;
    /** ForgeData 键：0 = 没掷过，1 = 掷输，2 = 掷赢。 */
    private static final String NBT_ROLLED = "deltamagic_SleepSprayTrade";

    private SleepSprayTradeManager() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Villager villager)
                || villager.level().isClientSide
                || villager.level().getGameTime() % CHECK_INTERVAL != 0) {
            return;
        }
        if (villager.getVillagerData().getProfession() != VillagerProfession.LIBRARIAN) {
            return;
        }
        if (villager.isTrading()) {
            return; // 玩家正开着它的交易界面：等关掉再动列表，避免与界面遍历冲突
        }
        CompoundTag data = villager.getPersistentData();
        int roll = data.getInt(NBT_ROLLED);
        if (roll == 1) {
            return; // 掷过且没通过：这个村民永远不卖这本书
        }
        // getOffers() 在原版里是惰性生成的（转职后 offers 被置空，这里第一次调用会触发重建），
        // 所以必须在拿到列表之后再做"已有报价"判断
        if (carriesBook(villager)) {
            if (roll == 0) {
                data.putInt(NBT_ROLLED, 2); // 兜底：报价已在（旧存档等），认账不再重复加
            }
            return;
        }
        if (roll == 0) {
            roll = villager.getRandom().nextFloat() < CHANCE ? 2 : 1;
            data.putInt(NBT_ROLLED, roll);
        }
        if (roll == 2) {
            villager.getOffers().add(makeOffer()); // 掷赢：交易被原版清空重建后也会走到这里补回
        }
    }

    /** 交易列表里是否已经有一条"睡眠喷雾附魔书"报价。 */
    private static boolean carriesBook(Villager villager) {
        for (MerchantOffer offer : villager.getOffers()) {
            ItemStack result = offer.getResult();
            if (!result.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(
                    GeneratedMod.SLEEP_SPRAY.get(), result) > 0) {
                return true;
            }
        }
        return false;
    }

    /** 生成一条"8 绿宝石 → 睡眠喷雾附魔书"的报价。 */
    private static MerchantOffer makeOffer() {
        return new MerchantOffer(new ItemStack(Items.EMERALD, PRICE_EMERALDS),
                EnchantedBookItem.createForEnchantment(
                        new EnchantmentInstance(GeneratedMod.SLEEP_SPRAY.get(), 1)),
                MAX_USES, VILLAGER_XP, 0.05F);
    }
}
