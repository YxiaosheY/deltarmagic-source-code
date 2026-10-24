package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;

/**
 * 牢房栏：外观与连接方式和原版铁栅栏完全一致（方块状态、碰撞箱、连接判定全部继承），
 * 唯一区别是"无法挖掘"——挖掘进度恒为 0，生存模式怎么挖都不会碎；
 * 爆炸抗性也按基岩级设置，炸不坏。
 * 拆除只有一条路：手持「牢房钥匙」右键它（拆墙逻辑在 PrisonKeyItem#useOn 里），
 * 被钥匙拆掉时方块直接消失、不掉物品。
 */
public final class PrisonBarsBlock extends IronBarsBlock {
    public PrisonBarsBlock() {
        // 复制原版铁栅栏的全部属性（含连接形状），再把硬度改成"不可挖掘"（-1）、爆炸抗性改成基岩级
        super(BlockBehaviour.Properties.copy(Blocks.IRON_BARS)
                .strength(-1.0F, 3600000.0F)
                .noLootTable());
    }

    /** 挖掘进度恒 0：生存模式下永远挖不动（创造模式按原版规则可以直接点掉）。 */
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return 0.0F;
    }
}
