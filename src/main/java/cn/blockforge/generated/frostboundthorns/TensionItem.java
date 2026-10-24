package cn.blockforge.generated.frostboundthorns;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 「紧迫」系列消耗品：右键使用后立刻恢复固定数量的 TP，并播放 snd_tensionhorn.ogg。
 * 三个物品只有恢复量不同（紧迫一点 32、紧迫宝石 50、紧迫满点 100），
 * TP 已经满时不消耗物品，避免白扔。
 */
public final class TensionItem extends Item {
    private final int tpAmount;
    /** 紧迫满点带原版附魔光泽（一闪一闪的附魔纹路），另外两个没有。 */
    private final boolean shiny;

    public TensionItem(int tpAmount) {
        this(tpAmount, false);
    }

    public TensionItem(int tpAmount, boolean shiny) {
        super(new Item.Properties().stacksTo(64));
        this.tpAmount = tpAmount;
        this.shiny = shiny;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            // 客户端只负责抬手动画，真正的 TP 变化由服务端结算后同步回 TP 条
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!ModEvents.tryRestoreTp(serverPlayer, tpAmount)) {
            serverPlayer.displayClientMessage(Component.literal("TP 已满，无需使用"), true);
            return InteractionResultHolder.fail(stack);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                GeneratedMod.TENSION_HORN_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return shiny;
    }
}
