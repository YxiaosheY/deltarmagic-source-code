package cn.blockforge.generated.frostboundthorns;

import net.minecraft.world.entity.LivingEntity;

/** 治疗类法术的服务端触发入口：通知客户端给被治疗生物播放翠绿闪现。 */
public final class GreenFlash {
    private GreenFlash() {
    }

    /** 治疗结算成功后调用；玩家也属于 LivingEntity，因此自疗同样会触发。 */
    public static void trigger(LivingEntity healed) {
        if (!healed.level().isClientSide) {
            NetworkHandler.sendGreenFlash(healed);
        }
    }
}
