package cn.blockforge.generated.frostboundthorns;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端一侧的法术菜单状态：是否打开、当前选中的第几格、玩家拥有哪些法术。
 * 法术可用性由服务端算好（手持斧的粗暴碎击附魔、戒指的雪葬附魔），
 * 随 TP 同步包一起推过来，存在 spellBits 里。
 * 不引用任何客户端类，保证服务端加载网络包时不会崩。
 */
public final class SpellMenuState {
    /** 粗暴碎击：手持带该附魔的斧头时可用（bit 0）。 */
    public static final int SPELL_ROUGH_SMASH = 1;
    /** 雪葬：佩戴带该附魔的荆棘指环或雪花指环时可用（bit 1）。 */
    public static final int SPELL_SNOW_BURIAL = 2;
    /** 朱红碎击：手持带该附魔的斧头时可用（bit 2）。 */
    public static final int SPELL_CRIMSON_SMASH = 4;
    /** 冰震术：佩戴带该附魔的雪花指环或荆棘指环时可用（bit 3）。 */
    public static final int SPELL_ICE_SHOCK = 8;
    /** 治疗祷言：佩戴带该附魔的头盔时可用（bit 4）。 */
    public static final int SPELL_HEAL_PRAYER = 16;
    /** 睡眠喷雾：佩戴带该附魔的雪花指环或荆棘指环时可用（bit 5）。 */
    public static final int SPELL_SLEEP_SPRAY = 32;
    /** 螺旋桃击：手持带该附魔的锄头时可用（bit 6）。 */
    public static final int SPELL_SPIRAL_CLUBS = 64;
    /** 方钻上冲：手持带该附魔的锄头时可用（bit 7）。 */
    public static final int SPELL_DIAMOND_UPPERCUT = 128;
    /** 恶魔镰刀：手持带该附魔的锄头时可用（bit 8）。 */
    public static final int SPELL_JEVIL_SCYTHE = 256;
    /** OK治疗：佩戴带该附魔的头盔时可用（bit 9）。 */
    public static final int SPELL_OK_HEAL = 512;

    /** 一页最多摆 3 格法术：超过 3 个时按页显示，↑/↓ 翻页。 */
    public static final int PAGE_SIZE = 3;

    private static boolean open;
    private static int spellBits;
    private static int selection;

    private SpellMenuState() {
    }

    public static boolean isOpen() {
        return open;
    }

    public static void toggle() {
        open = !open;
        clampSelection();
    }

    public static void close() {
        open = false;
    }

    /** 服务端同步过来的可用法术位图。 */
    public static void setSpellBits(int bits) {
        spellBits = bits;
        clampSelection();
    }

    /**
     * 按类型分组的可用法术列表：碎击类在前（粗暴碎击 → 朱红碎击），
     * 冰系在中（冰震术 → 睡眠喷雾 → 雪葬），治疗类最后（治疗祷言）。
     */
    public static List<Integer> availableSpells() {
        List<Integer> spells = new ArrayList<>(4);
        if ((spellBits & SPELL_ROUGH_SMASH) != 0) {
            spells.add(SPELL_ROUGH_SMASH);
        }
        if ((spellBits & SPELL_CRIMSON_SMASH) != 0) {
            spells.add(SPELL_CRIMSON_SMASH);
        }
        if ((spellBits & SPELL_ICE_SHOCK) != 0) {
            spells.add(SPELL_ICE_SHOCK);
        }
        if ((spellBits & SPELL_SLEEP_SPRAY) != 0) {
            spells.add(SPELL_SLEEP_SPRAY);
        }
        if ((spellBits & SPELL_SPIRAL_CLUBS) != 0) {
            spells.add(SPELL_SPIRAL_CLUBS);
        }
        if ((spellBits & SPELL_DIAMOND_UPPERCUT) != 0) {
            spells.add(SPELL_DIAMOND_UPPERCUT);
        }
        if ((spellBits & SPELL_JEVIL_SCYTHE) != 0) {
            spells.add(SPELL_JEVIL_SCYTHE);
        }
        if ((spellBits & SPELL_SNOW_BURIAL) != 0) {
            spells.add(SPELL_SNOW_BURIAL);
        }
        if ((spellBits & SPELL_HEAL_PRAYER) != 0) {
            spells.add(SPELL_HEAL_PRAYER);
        }
        if ((spellBits & SPELL_OK_HEAL) != 0) {
            spells.add(SPELL_OK_HEAL);
        }
        return spells;
    }

    /** 当前选中的法术；没有可用法术时返回 0。 */
    public static int selectedSpell() {
        List<Integer> spells = availableSpells();
        if (spells.isEmpty()) {
            return 0;
        }
        clampSelection();
        return spells.get(selection);
    }

    /** 选中法术在列表里的下标，供 HUD 决定绘制顺序。 */
    public static int selectedIndex() {
        List<Integer> spells = availableSpells();
        return spells.isEmpty() ? -1 : Math.min(selection, spells.size() - 1);
    }

    /**
     * ←/→：在<b>当前页内</b>移动选中格（页内首尾循环）。
     * 法术不超过一页时退化为整个列表首尾循环，和旧行为一致。
     */
    public static void moveSelection(int delta) {
        List<Integer> spells = availableSpells();
        int size = spells.size();
        if (size == 0) {
            selection = 0;
            return;
        }
        clampSelection();
        if (size <= PAGE_SIZE) {
            selection = (selection + delta % size + size) % size;
            return;
        }
        int pageStart = (selection / PAGE_SIZE) * PAGE_SIZE;
        int pageLen = Math.min(PAGE_SIZE, size - pageStart);
        int local = selection - pageStart;
        local = (local + delta % pageLen + pageLen) % pageLen;
        selection = pageStart + local;
    }

    /** 法术总数算出的总页数（至少 1 页）。 */
    public static int pageCount() {
        int size = availableSpells().size();
        return Math.max(1, (size + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    /** 当前页页码（0 起）。页由选中下标推导，保证选中的那一格一定画在页内。 */
    public static int currentPage() {
        int idx = selectedIndex();
        if (idx < 0) {
            return 0;
        }
        return Math.min(idx / PAGE_SIZE, pageCount() - 1);
    }

    /**
     * ↑/↓：切换上/下一页（首尾循环）。只有一页时什么都不做。
     * 翻页后尽量保持在新页的同一格；新页不满一格时落在最后一格。
     */
    public static void movePage(int delta) {
        int size = availableSpells().size();
        if (size <= PAGE_SIZE) {
            return;
        }
        clampSelection();
        int pages = pageCount();
        int page = selection / PAGE_SIZE;
        int local = selection % PAGE_SIZE;
        page = ((page + delta) % pages + pages) % pages;
        selection = Math.min(page * PAGE_SIZE + local, size - 1);
    }

    private static void clampSelection() {
        int size = availableSpells().size();
        if (size == 0) {
            selection = 0;
        } else if (selection >= size) {
            selection = 0;
        }
    }

    public static void reset() {
        open = false;
        spellBits = 0;
        selection = 0;
    }
}
