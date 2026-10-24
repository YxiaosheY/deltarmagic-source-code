package cn.blockforge.generated.frostboundthorns;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 屏幕正上方的法术菜单 HUD（按 X 开关）。
 * 菜单现在只是"选法术"的界面，释放不再依赖它开着：菜单关闭时，
 * 屏幕正右侧（垂直居中）会显示一枚小徽章，提示当前选中的（也就是按 Z 会释放的）法术。
 * 背景是 64×64 的白边黑底方块（spell_menu_bg），用九宫格方式横向拉伸到
 * 和左侧 TP 条的长边一样宽（196 像素，随屏幕高度同比缩放）：
 * 四个角原样保留，上下左右四条边只单向拉伸，中间铺满，边框始终 2 像素不走样。
 * 菜单内按类型分组显示当前可用的法术图标（碎击类在前、冰系在后；选中的一格用 _use 贴图），
 * 法术超过 3 个时一页只画 3 格、右下角显示页码（↑/↓ 翻页）；
 * 菜单外右侧贴 spr_bttech（MAGIC 按钮）。
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
public final class SpellMenuHud {
    /** 菜单背景：spell_menu_bg.png（64×64，2 像素白边）。 */
    private static final ResourceLocation MENU_BG = guiTexture("spell_menu_bg");
    /** 菜单外右侧按钮：spell_menu_button.png（31×32）。 */
    private static final ResourceLocation MENU_BUTTON = guiTexture("spell_menu_button");
    private static final ResourceLocation ICON_RUDE = guiTexture("spell_rude");
    private static final ResourceLocation ICON_RUDE_USE = guiTexture("spell_rude_use");
    private static final ResourceLocation ICON_RED_RUDE = guiTexture("spell_redrude");
    private static final ResourceLocation ICON_RED_RUDE_USE = guiTexture("spell_redrude_use");
    private static final ResourceLocation ICON_GASTER = guiTexture("spell_gaster");
    private static final ResourceLocation ICON_GASTER_USE = guiTexture("spell_gaster_use");
    private static final ResourceLocation ICON_ICE = guiTexture("spell_ice");
    private static final ResourceLocation ICON_ICE_USE = guiTexture("spell_ice_use");
    private static final ResourceLocation ICON_HEAL = guiTexture("spell_heal");
    private static final ResourceLocation ICON_HEAL_USE = guiTexture("spell_heal_use");
    private static final ResourceLocation ICON_SLEEP = guiTexture("spell_sleep");
    private static final ResourceLocation ICON_SLEEP_USE = guiTexture("spell_sleep_use");
    /** 螺旋桃击：spell_jevilse（Jevil 与环绕的桃弹）。 */
    private static final ResourceLocation ICON_SPIRAL = guiTexture("spell_jevilse");
    private static final ResourceLocation ICON_SPIRAL_USE = guiTexture("spell_jevilse_use");
    /** 方钻上冲：spell_jevilfr（Jevil 与环绕的方钻弹）。 */
    private static final ResourceLocation ICON_DIAMOND = guiTexture("spell_jevilfr");
    private static final ResourceLocation ICON_DIAMOND_USE = guiTexture("spell_jevilfr_use");
    /** 恶魔镰刀：spell_jevilthr（Jevil 与环绕的镰刀）。 */
    private static final ResourceLocation ICON_SCYTHE = guiTexture("spell_jevilthr");
    private static final ResourceLocation ICON_SCYTHE_USE = guiTexture("spell_jevilthr_use");
    /** OK治疗：spell_okheal（用户素材 OKheal / OKheal_use）。 */
    private static final ResourceLocation ICON_OK_HEAL = guiTexture("spell_okheal");
    private static final ResourceLocation ICON_OK_HEAL_USE = guiTexture("spell_okheal_use");

    private static final int BG_TEX = 64;
    /** 背景边框厚度（源图像素）。 */
    private static final int BORDER = 2;
    /** 菜单拉到的目标宽度 = TP 条长边（196），保持和 TP 条一样长。 */
    private static final int MENU_TEX_W = 196;
    private static final int MENU_TEX_H = 64;
    private static final int BUTTON_TEX_W = 31;
    private static final int BUTTON_TEX_H = 32;
    /** 图标在源图里占 64×64，画到菜单里缩到 48 高。 */
    private static final int ICON_TEX_H = 48;
    private static final int TOP_MARGIN = 4;
    private static final int ICON_GAP = 12;

    private SpellMenuHud() {
    }

    private static ResourceLocation guiTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "textures/gui/" + name + ".png");
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.player.isSpectator()) {
            return;
        }
        if (!SpellMenuState.isOpen()) {
            // 菜单关着时也要让玩家知道 Z 会放出什么：屏幕正右侧画一枚当前选中的法术徽章
            drawSelectedBadge(event.getGuiGraphics(), minecraft);
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();

        // 缩放规则和 TP 条一致，保证菜单长度和 TP 条长边完全相同
        float scale = Math.min(1.0F, (screenH - 24) / (float) MENU_TEX_W);
        int menuW = Math.max(1, Math.round(MENU_TEX_W * scale));
        int menuH = Math.max(1, Math.round(MENU_TEX_H * scale));
        int menuX = (screenW - menuW) / 2;
        int menuY = TOP_MARGIN;

        drawNineSlice(graphics, MENU_BG, menuX, menuY, menuW, menuH, scale);

        // 法术图标：一页最多画 3 格（超过 3 个法术按页显示，↑/↓ 翻页），选中的用 _use 贴图
        List<Integer> spells = SpellMenuState.availableSpells();
        int selected = SpellMenuState.selectedIndex();
        if (!spells.isEmpty()) {
            int pageStart = SpellMenuState.currentPage() * SpellMenuState.PAGE_SIZE;
            int pageEnd = Math.min(spells.size(), pageStart + SpellMenuState.PAGE_SIZE);
            int shown = pageEnd - pageStart;
            int iconSize = Math.max(1, Math.round(ICON_TEX_H * scale));
            int gap = Math.max(1, Math.round(ICON_GAP * scale));
            int totalW = shown * iconSize + (shown - 1) * gap;
            int startX = menuX + (menuW - totalW) / 2;
            int iconY = menuY + (menuH - iconSize) / 2;
            for (int i = 0; i < shown; i++) {
                ResourceLocation icon = iconFor(spells.get(pageStart + i), pageStart + i == selected);
                graphics.blit(icon, startX + i * (iconSize + gap), iconY,
                        iconSize, iconSize, 0.0F, 0.0F, BG_TEX, BG_TEX, BG_TEX, BG_TEX);
            }

            // 页码指示：法术多到分页时，在菜单右下角画 "当前页/总页数"
            int pages = SpellMenuState.pageCount();
            if (pages > 1) {
                String pageText = (pageStart / SpellMenuState.PAGE_SIZE + 1) + "/" + pages;
                var font = minecraft.font;
                int textW = font.width(pageText);
                graphics.drawString(font, pageText,
                        menuX + menuW - textW - 4, menuY + menuH - font.lineHeight - 3,
                        0xFFFFFF, true);
            }
        }

        // 菜单外右侧的 MAGIC 按钮，垂直居中对齐菜单
        int buttonW = Math.max(1, Math.round(BUTTON_TEX_W * scale));
        int buttonH = Math.max(1, Math.round(BUTTON_TEX_H * scale));
        graphics.blit(MENU_BUTTON, menuX + menuW + 2, menuY + (menuH - buttonH) / 2,
                buttonW, buttonH, 0.0F, 0.0F, BUTTON_TEX_W, BUTTON_TEX_H, BUTTON_TEX_W, BUTTON_TEX_H);
    }

    /**
     * 菜单关闭时画的"当前选中法术"小徽章：贴屏幕右边缘、垂直居中，尺寸约一格物品大小，
     * 背景复用菜单的九宫格贴图（缩小版），中间是选中法术的高亮图标（_use 贴图）。
     * 现在释放法术不再要求打开菜单，这枚徽章保证菜单合上也能看见 Z 会放出什么。
     * 没有任何可用法术时不画。
     */
    private static void drawSelectedBadge(GuiGraphics graphics, Minecraft minecraft) {
        int spell = SpellMenuState.selectedSpell();
        if (spell == 0) {
            return;
        }
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        float scale = Math.min(1.0F, (screenH - 24) / (float) MENU_TEX_W);
        int badge = Math.max(12, Math.round(30 * scale));
        int pad = Math.max(2, Math.round(4 * scale));
        int icon = Math.max(1, badge - 2 * pad);
        int x = screenW - badge - TOP_MARGIN;
        int y = (screenH - badge) / 2;
        drawNineSlice(graphics, MENU_BG, x, y, badge, badge, scale);
        ResourceLocation tex = iconFor(spell, true);
        graphics.blit(tex, x + pad, y + pad, icon, icon,
                0.0F, 0.0F, BG_TEX, BG_TEX, BG_TEX, BG_TEX);
    }

    private static ResourceLocation iconFor(int spell, boolean isSelected) {
        if (spell == SpellMenuState.SPELL_ROUGH_SMASH) {
            return isSelected ? ICON_RUDE_USE : ICON_RUDE;
        }
        if (spell == SpellMenuState.SPELL_CRIMSON_SMASH) {
            return isSelected ? ICON_RED_RUDE_USE : ICON_RED_RUDE;
        }
        if (spell == SpellMenuState.SPELL_ICE_SHOCK) {
            return isSelected ? ICON_ICE_USE : ICON_ICE;
        }
        if (spell == SpellMenuState.SPELL_HEAL_PRAYER) {
            return isSelected ? ICON_HEAL_USE : ICON_HEAL;
        }
        if (spell == SpellMenuState.SPELL_SLEEP_SPRAY) {
            return isSelected ? ICON_SLEEP_USE : ICON_SLEEP;
        }
        if (spell == SpellMenuState.SPELL_SPIRAL_CLUBS) {
            return isSelected ? ICON_SPIRAL_USE : ICON_SPIRAL;
        }
        if (spell == SpellMenuState.SPELL_DIAMOND_UPPERCUT) {
            return isSelected ? ICON_DIAMOND_USE : ICON_DIAMOND;
        }
        if (spell == SpellMenuState.SPELL_JEVIL_SCYTHE) {
            return isSelected ? ICON_SCYTHE_USE : ICON_SCYTHE;
        }
        if (spell == SpellMenuState.SPELL_OK_HEAL) {
            return isSelected ? ICON_OK_HEAL_USE : ICON_OK_HEAL;
        }
        return isSelected ? ICON_GASTER_USE : ICON_GASTER;
    }

    /** 九宫格拉伸：4 个角原样、4 条边单向拉伸、中间双向拉伸，边框厚度不随尺寸变形。 */
    private static void drawNineSlice(GuiGraphics graphics, ResourceLocation tex,
                                      int x, int y, int w, int h, float scale) {
        int b = Math.max(1, Math.round(BORDER * scale));
        int innerW = Math.max(0, w - 2 * b);
        int innerH = Math.max(0, h - 2 * b);
        int srcInner = BG_TEX - 2 * BORDER;
        // 上排：左上角 / 上边 / 右上角
        graphics.blit(tex, x, y, b, b, 0.0F, 0.0F, BORDER, BORDER, BG_TEX, BG_TEX);
        graphics.blit(tex, x + b, y, innerW, b, BORDER, 0.0F, srcInner, BORDER, BG_TEX, BG_TEX);
        graphics.blit(tex, x + w - b, y, b, b, BG_TEX - BORDER, 0.0F, BORDER, BORDER, BG_TEX, BG_TEX);
        // 中排：左边 / 中心 / 右边
        graphics.blit(tex, x, y + b, b, innerH, 0.0F, BORDER, BORDER, srcInner, BG_TEX, BG_TEX);
        graphics.blit(tex, x + b, y + b, innerW, innerH, BORDER, BORDER, srcInner, srcInner, BG_TEX, BG_TEX);
        graphics.blit(tex, x + w - b, y + b, b, innerH, BG_TEX - BORDER, BORDER, BORDER, srcInner, BG_TEX, BG_TEX);
        // 下排：左下角 / 下边 / 右下角
        graphics.blit(tex, x, y + h - b, b, b, 0.0F, BG_TEX - BORDER, BORDER, BORDER, BG_TEX, BG_TEX);
        graphics.blit(tex, x + b, y + h - b, innerW, b, BORDER, BG_TEX - BORDER, srcInner, BORDER, BG_TEX, BG_TEX);
        graphics.blit(tex, x + w - b, y + h - b, b, b, BG_TEX - BORDER, BG_TEX - BORDER, BORDER, BORDER, BG_TEX, BG_TEX);
    }
}
