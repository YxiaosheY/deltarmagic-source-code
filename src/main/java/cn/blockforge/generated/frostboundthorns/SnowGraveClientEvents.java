package cn.blockforge.generated.frostboundthorns;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 雪墓值（TicksSnowGrave）的客户端表现：
 * <ul>
 * <li>超过 40 后，屏幕上出现一张铺满视野的原版冰块贴图（textures/block/ice.png），
 *     不透明度随数值从 40→140 线性上升到 0.85（封顶 0.85，保证还能看见路）；</li>
 * <li>到 140（冰封中），生物会被一个放大的原版冰块模型包裹住，直到数值归零；</li>
 * <li>玩家自己被冰封（数值到 140）期间，所有按键直接失灵：移动、跳跃、潜行、攻击、
 *     放置、背包、快捷栏、本模组的施法键（X/Z/←/→）等全部无响应，
 *     鼠标左键 / 右键 / 滚轮同样禁用；只有 ESC、F3 这类系统功能键保持可用，
 *     保证玩家随时能暂停或退出。</li>
 * </ul>
 * 数据来源是服务端推送的 SnowGraveState，这里只读不写。
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
public final class SnowGraveClientEvents {
    /** 原版冰块贴图，直接引用 minecraft 命名空间，不打包副本。 */
    private static final ResourceLocation ICE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/ice.png");
    /** 屏幕贴图起效阈值：雪墓值超过 40 才开始显现。 */
    public static final int OVERLAY_START = 40;
    /** 冰贴图单块源尺寸（原版 ice.png 为 16×16，平铺保持像素密度不变）。 */
    private static final int ICE_TEX_SIZE = 16;
    /** 屏幕贴图最大不透明度（140 时也不完全糊死，留一点视野）。 */
    private static final float OVERLAY_MAX_ALPHA = 0.85F;

    private SnowGraveClientEvents() {
    }

    /** 断线 / 换世界：清空缓存，防止实体 ID 复用时把旧状态套到新生物身上。 */
    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SnowGraveState.clear();
        inputLocked = false;
    }

    // ------------------------------------------------------------------
    // 冰封时"所有按键直接失灵"
    // ------------------------------------------------------------------

    /** 上一刻是否处于按键锁定状态：只在"冻结 → 解冻"的瞬间做一次按键回读。 */
    private static boolean inputLocked;

    /**
     * 客户端 tick 开头（Forge 实测：onPreClientTick 在 Minecraft.tick() 偏移 17，
     * vanilla 的 handleKeybinds 在偏移 335，所以这里清零一定赶在原版消费按键之前）：
     * 玩家自己雪墓值到 140 时，把 options.keyMappings 里所有按键（含本模组注册的
     * X/Z/←/→）强制松开、排空积压的单击计数——移动、跳跃、攻击、放置、背包、
     * 快捷栏、施法全部失灵。ESC / F3 由 KeyboardHandler 直接处理，不走按键表，
     * 保持可用（暂停 / 退出的安全出口）。
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean locked = isLocalPlayerSealed(minecraft);
        if (locked) {
            for (KeyMapping key : minecraft.options.keyMappings) {
                key.setDown(false);            // 长按型按键（WASD / 跳跃 / 潜行 / 疾跑…）按"未按下"处理
                while (key.consumeClick()) {   // 单击触发型（背包 / 丢弃 / 数字键…）排空排队中的点击
                    // 循环体留空：只需要 consumeClick 的清零副作用
                }
            }
            KeyMapping.resetToggleKeys(); // 切换模式的潜行/疾跑不吃 setDown(false)，用官方复位
        } else if (inputLocked) {
            // 解冻瞬间把键盘物理按下状态读回来：还按着 W 就能立刻继续跑动
            KeyMapping.setAll();
        }
        inputLocked = locked;
    }

    /** 冰封期间禁用鼠标按下 / 重复（放行"松开"事件，避免原版按键状态卡住）。 */
    @SubscribeEvent
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        // MouseButton.Pre 可取消（@Cancelable）；取消后原版不再处理这次点击（按键表、界面点击都不走）
        if (minecraft.screen == null && event.getAction() != GLFW.GLFW_RELEASE
                && isLocalPlayerSealed(minecraft)) {
            event.setCanceled(true);
        }
    }

    /** 冰封期间禁用滚轮（防止切快捷栏 / 换手持物）。 */
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null && isLocalPlayerSealed(minecraft)) {
            event.setCanceled(true);
        }
    }

    /** 玩家自己是否处于冰封：服务端在冻结开始 / 期间 / 结束都会同步，客户端缓存到 140 即冻结中。 */
    private static boolean isLocalPlayerSealed(Minecraft minecraft) {
        return minecraft.player != null && minecraft.level != null
                && SnowGraveState.get(minecraft.player.getId()) >= SnowGraveManager.MAX_VALUE;
    }

    /** HUD 层：玩家自己的雪墓值 > 40 时，铺一层半透明原版冰块贴图。 */
    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.player.isSpectator()) {
            return;
        }
        int value = SnowGraveState.get(minecraft.player.getId());
        if (value <= OVERLAY_START) {
            return;
        }
        float alpha = Mth.clamp((value - OVERLAY_START) / 100.0F, 0.0F, OVERLAY_MAX_ALPHA);
        GuiGraphics graphics = event.getGuiGraphics();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blitRepeating(ICE_TEXTURE, 0, 0, width, height,
                0, 0, ICE_TEX_SIZE, ICE_TEX_SIZE, ICE_TEX_SIZE, ICE_TEX_SIZE);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F); // 颜色状态必须复位，避免影响后面的 HUD 绘制
    }

    /** 世界层：雪墓值达到 140（冰封中）的生物，外面罩一个放大到其碰撞箱的原版冰块模型。 */
    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<LivingEntity, ?> event) {
        LivingEntity entity = event.getEntity();
        if (SnowGraveState.get(entity.getId()) < SnowGraveManager.MAX_VALUE) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        // 事件传入的坐标系原点在生物脚底中心：把 1×1×1 的方块模型平移居中并缩放到略大于碰撞箱
        float width = entity.getBbWidth() + 0.5F;
        float height = entity.getBbHeight() + 0.5F;
        poseStack.translate(-width * 0.5F, -0.15F, -width * 0.5F);
        poseStack.scale(width, height, width);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.ICE.defaultBlockState(),
                poseStack, event.getMultiBufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
