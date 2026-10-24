package cn.blockforge.generated.frostboundthorns;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** 注册治疗类法术的全身翠绿闪现渲染层，并在断线时清理客户端计时表。 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GreenFlashClientEvents {
    private GreenFlashClientEvents() {
    }

    /** 给所有已注册的生物渲染器加上蒙皮层；玩家的不同外观渲染器单独处理。 */
    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            if (type == EntityType.PLAYER) {
                continue;
            }
            attachByType(event, type);
        }
        for (String skin : event.getSkins()) {
            attach(event.getSkin(skin));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void attachByType(EntityRenderersEvent.AddLayers event, EntityType<?> type) {
        EntityRenderer renderer = event.getRenderer((EntityType) type);
        attach(renderer);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void attach(EntityRenderer renderer) {
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            RenderLayer layer = new GreenFlashLayer(livingRenderer);
            livingRenderer.addLayer(layer);
        }
    }

    /** 玩家渲染器可能在切换世界后复用实体 ID，离开世界时清空旧闪现。 */
    @Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
    public static final class ForgeEvents {
        private ForgeEvents() {
        }

        @SubscribeEvent
        public static void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
            GreenFlashState.clear();
        }
    }
}
