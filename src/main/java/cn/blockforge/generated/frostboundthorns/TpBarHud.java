package cn.blockforge.generated.frostboundthorns;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 屏幕左侧的 TP 条 HUD。
 * 佩戴（Curios 戒指槽）或手持模组物品时显示：
 * 图层自下而上：深红底槽（spr_tensionbar_ch1_1）→ 顶部高出一像素的白色亮边
 * （spr_tensionfilling_ch1_2-2）→ 从下往上涨的橙色填充（spr_tensionfilling_ch1_0），
 * 左侧是 TP 图标和百分比文字。填充不再按数值阈值换贴图。
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
public final class TpBarHud {
    /** 底槽：spr_tensionbar_ch1_1.png（深红 + 黑描边）。 */
    private static final ResourceLocation BAR_BG = guiTexture("tp_bar_empty");
    /** 填充：spr_tensionfilling_ch1_0.png（橙色），盖在最上层。 */
    private static final ResourceLocation FILL = guiTexture("tp_fill_normal");
    /** 亮边：spr_tensionfilling_ch1_2-2.png（白色），垫在填充下面、顶出 1 像素。 */
    private static final ResourceLocation FILL_CAP = guiTexture("tp_fill_full");
    private static final ResourceLocation LOGO = guiTexture("tp_logo");

    private static final int BAR_TEX_W = 25;
    private static final int BAR_TEX_H = 196;
    private static final int LOGO_TEX_W = 22;
    private static final int LOGO_TEX_H = 44;
    private static final int MARGIN = 6;

    private TpBarHud() {
    }

    private static ResourceLocation guiTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "textures/gui/" + name + ".png");
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator() || !shouldShow(minecraft)) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int tp = TpBarState.get();

        // 屏幕太矮时整体等比缩小，保证条子完整可见
        float scale = Math.min(1.0F, (screenH - 24) / (float) BAR_TEX_H);
        int barW = Math.max(1, Math.round(BAR_TEX_W * scale));
        int barH = Math.max(1, Math.round(BAR_TEX_H * scale));
        int logoW = Math.max(1, Math.round(LOGO_TEX_W * scale));
        int logoH = Math.max(1, Math.round(LOGO_TEX_H * scale));

        int logoX = MARGIN;
        int barX = logoX + logoW + 4;
        int barY = (screenH - barH) / 2;
        int logoY = barY + Math.round(barH * 0.22F);

        // 1) 底槽（spr_tensionbar_ch1_1，自带黑描边），永远垫在最下面
        graphics.blit(BAR_BG, barX, barY, barW, barH, 0.0F, 0.0F, BAR_TEX_W, BAR_TEX_H, BAR_TEX_W, BAR_TEX_H);

        // 2) 白色亮边（spr_tensionfilling_ch1_2-2）：和填充一样从底部涨，但多画 1 像素，
        //    露出顶部 1 像素的白线；3) 橙色填充（spr_tensionfilling_ch1_0）盖在它上面。
        //    不再按数值阈值更换贴图。
        if (tp > 0) {
            int clamped = Math.min(tp, TpBarState.MAX);
            int srcH = Math.round(BAR_TEX_H * clamped / (float) TpBarState.MAX);
            int dstH = Math.max(1, Math.round(barH * clamped / (float) TpBarState.MAX));
            int capSrcH = Math.min(BAR_TEX_H, srcH + 1);
            int capDstH = dstH + 1;
            graphics.blit(FILL_CAP, barX, barY + barH - capDstH, barW, capDstH,
                    0.0F, BAR_TEX_H - capSrcH, BAR_TEX_W, capSrcH, BAR_TEX_W, BAR_TEX_H);
            graphics.blit(FILL, barX, barY + barH - dstH, barW, dstH,
                    0.0F, BAR_TEX_H - srcH, BAR_TEX_W, srcH, BAR_TEX_W, BAR_TEX_H);
        }

        // 4) TP 图标 + 百分比文字
        graphics.blit(LOGO, logoX, logoY, logoW, logoH, 0.0F, 0.0F, LOGO_TEX_W, LOGO_TEX_H, LOGO_TEX_W, LOGO_TEX_H);
        Font font = minecraft.font;
        String text = Math.min(tp, TpBarState.MAX) + "%";
        int textX = logoX + (logoW - font.width(text)) / 2;
        graphics.drawString(font, text, textX, logoY + logoH + 2, 0xFFFFFF, true);
    }

    /** 佩戴指环（荆棘 / 雪花，Curios 戒指槽）或手持模组物品时显示。 */
    private static boolean shouldShow(Minecraft minecraft) {
        Player player = minecraft.player;
        if (holdsModItem(player.getMainHandItem()) || holdsModItem(player.getOffhandItem())) {
            return true;
        }
        return ModEvents.findEquippedRing(player).isPresent();
    }

    private static boolean holdsModItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item == GeneratedMod.THORN_RING.get()
                || item == GeneratedMod.SNOWFLAKE_RING.get()
                || item == GeneratedMod.SNOW_BURIAL_BOOK.get()
                || item instanceof TensionItem
                || ModEvents.carriesModBookEnchantment(stack)) {
            return true;
        }
        return item instanceof AxeItem
                && (ModEvents.hasToothbrush(stack) || ModEvents.hasRoughSmash(stack));
    }
}
