package cn.blockforge.generated.frostboundthorns;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.ITooltip;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * 与「玉 (Jade)」模组的信息显示框兼容层：把生物的服务端疲劳值 Tiredness
 * 同步给客户端，并显示在 Jade 悬停提示框里（例：疲劳：30）。
 * <p>
 * 工作机制（Jade 1.20.1 Forge 的插件发现）：Jade 在模组加载完成后扫描所有 jar 里
 * 带 {@code @WailaPlugin} 注解的类，用无参构造实例化，再分别调用
 * {@link #register}（服务端侧，登记数据搬运工）与 {@link #registerClient}（客户端侧，
 * 登记提示框绘制者）。因此：
 * <ul>
 * <li>没装 Jade 时，这个类永远不会被加载，模组照常运行（Jade 只在编译期依赖，不打进产物）；</li>
 * <li>装了 Jade 时，玩家准星对准生物的瞬间，Jade 会向服务器要一份该生物的数据，
 *     {@link TirednessServerProvider} 把 Tiredness 写进返回的 CompoundTag，
 *     {@link TirednessClientProvider} 读到后在提示框追加一行「疲劳：N」。</li>
 * </ul>
 * 按要求：疲劳值为 0（没被喷雾喷过 / 被喷满睡死后清零）时整行不显示——服务端
 * 干脆不写这个键，客户端"没这个键就不画"。
 */
@WailaPlugin
public final class JadeCompat implements IWailaPlugin {

    /** 服务端 → 客户端同步疲劳值用的 NBT 键（与 TirednessManager 存档键同名，一份字符串两用）。 */
    static final String SYNC_KEY = TirednessManager.NBT_VALUE;

    @Override
    public void register(IWailaCommonRegistration registration) {
        // 服务端：为"所有实体"注册数据搬运（Provider 内部再筛：只搬活着的、疲劳非 0 的生物）
        registration.registerEntityDataProvider(TirednessServerProvider.INSTANCE, Entity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // 客户端：为"所有实体"注册提示框的一行绘制逻辑
        registration.registerEntityComponent(TirednessClientProvider.INSTANCE, Entity.class);
    }

    /** 提供者身份 id：Jade 用它做配置开关 / 排序，服务端与客户端两半必须用同一个。 */
    private static ResourceLocation uid() {
        return new ResourceLocation(GeneratedMod.MOD_ID, "tiredness");
    }

    /** 服务端搬运工：把 Jade 请求的那个生物的 Tiredness 写进回包（为 0 就不写，客户端自然不显示）。 */
    private static final class TirednessServerProvider
            implements snownee.jade.api.IServerDataProvider<EntityAccessor> {
        static final TirednessServerProvider INSTANCE = new TirednessServerProvider();

        @Override
        public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
            if (accessor.getEntity() instanceof LivingEntity living) {
                int value = TirednessManager.get(living);
                if (value != 0) {
                    tag.putInt(SYNC_KEY, value);
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return uid();
        }
    }

    /** 客户端绘制者：从 Jade 回包里读到疲劳值就补一行「疲劳：N」，读不到（为 0）什么都不画。 */
    private static final class TirednessClientProvider implements IEntityComponentProvider {
        static final TirednessClientProvider INSTANCE = new TirednessClientProvider();

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (data.contains(SYNC_KEY)) {
                int value = data.getInt(SYNC_KEY);
                if (value != 0) {
                    tooltip.add(Component.translatable("deltamagic.jade.tiredness", value));
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return uid();
        }
    }
}
