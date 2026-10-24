package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 牢房钥匙：16 点耐久，每右键拆掉一格牢房栏消耗 1 点。
 * 右键牢房栏时该格方块"立刻消失"（不掉物品），同时钥匙扣 1 点耐久；
 * 耐久耗尽时按原版工具损坏规则播放断裂动画。
 */
public final class PrisonKeyItem extends Item {
    public PrisonKeyItem() {
        super(new Item.Properties().stacksTo(1).durability(16));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(GeneratedMod.PRISON_BARS.get())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            // dropItems=false：方块直接消失，不掉任何东西（钥匙开锁，牢门自解）
            level.destroyBlock(pos, false, context.getPlayer());
            ItemStack stack = context.getItemInHand();
            Player player = context.getPlayer();
            if (player != null) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            } else {
                // 发射器等无主场景：静默扣耐久
                stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + 1));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 近战攻击不消耗耐久：它只是开牢门的工具。 */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false;
    }
}
