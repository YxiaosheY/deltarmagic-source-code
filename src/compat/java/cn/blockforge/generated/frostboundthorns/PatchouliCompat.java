package cn.blockforge.generated.frostboundthorns;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.item.ItemModBook;

/**
 * 帕秋莉手册兼容层：只在 {@link PatchouliGate#patchouliLoaded()} 为 true 时才会被类加载器碰到。
 * <p>
 * 职责只有一件事——玩家进服时检查背包里有没有 deltamagic 教程书，没有就发一本
 * （放进主背包失败则丢在脚下）。书本身的注册、创造页挂载、内容加载全部由
 * 帕秋莉读取 data/deltamagic/patchouli_books/ 下我们提供的 JSON 自动完成。
 */
public final class PatchouliCompat {

    /** 书籍 ID：对应 data/deltamagic/patchouli_books/deltamagic_book/book.json（1.20 起书籍定义改为文件夹内的 book.json）。 */
    public static final ResourceLocation BOOK_ID =
            ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "deltamagic_book");

    private PatchouliCompat() {
    }

    /** 服务端：给玩家补发一本 deltamagic 教程书（已有同款则不发，背包满丢脚下）。 */
    public static void giveGuideBook(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            Book held = ItemModBook.getBook(stack);
            if (held != null && BOOK_ID.equals(held.id)) {
                return; // 背包里已经有一本，不重复发
            }
        }
        ItemStack book = PatchouliAPI.get().getBookStack(BOOK_ID);
        if (book.isEmpty() || ItemModBook.getBook(book) == null) {
            // 书籍 JSON 没注册成功（比如被数据包误删），不发一本"点开是坏书"的空书
            return;
        }
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
    }
}
