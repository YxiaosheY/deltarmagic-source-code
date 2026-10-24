package cn.blockforge.generated.frostboundthorns;

import net.minecraft.server.level.ServerPlayer;

/**
 * 帕秋莉手册 (Patchouli) 可选适配的"门控"层——本类不 import 任何 patchouli 类，
 * 因此没装帕秋莉时也能安全加载；只有确认帕秋莉在场时才会去碰 {@link PatchouliCompat}
 * （JVM 对类是懒加载的：那行调用不被执行，PatchouliCompat 就永远不会被解析）。
 * <p>
 * 两件事分工：
 * <ul>
 * <li>「模组创造页里出现教程书」：纯数据驱动，书 JSON 的 creative_tab 字段
 *     指向 deltamagic:mod_items，帕秋莉自己会把书挂进那一页，不需要任何代码；</li>
 * <li>「玩家进游戏发一本」：由本类在 PlayerLoggedInEvent 里转发给 {@link PatchouliCompat}。</li>
 * </ul>
 */
public final class PatchouliGate {

    private PatchouliGate() {
    }

    /** 帕秋莉手册是否已安装（加载器层面的判断，不加载任何它的类）。 */
    public static boolean patchouliLoaded() {
        return net.minecraftforge.fml.ModList.get().isLoaded("patchouli");
    }

    /** 玩家进服时调用：装了帕秋莉就补发一本教程书，没装或出任何岔子都静默跳过。 */
    public static void onPlayerLoggedIn(ServerPlayer player) {
        if (!patchouliLoaded()) {
            return;
        }
        try {
            PatchouliCompat.giveGuideBook(player);
        } catch (Throwable t) {
            // 发书失败绝不能影响玩家正常进服（例如书籍 JSON 被数据包改坏了）。
            System.err.println("[deltamagic] 帕秋莉教程书发放跳过：" + t);
        }
    }
}
