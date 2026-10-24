package cn.blockforge.generated.frostboundthorns;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端缓存的「翠绿闪现」时间表：实体网络 ID → 本波闪现的开始时刻（毫秒）。
 * 治疗类法术（治疗祷言、OK治疗）在服务端结算生效后，通过 GreenFlashPacket 推给
 * 在场客户端，这里只负责记时；渲染层（{@link GreenFlashLayer}）每帧读取不透明度。
 * 纯数据容器（不引用任何客户端类），与 SnowGraveState 同一口径。
 *
 * 节奏：总时长 0.5 秒（10 游戏刻）。前 0.12 秒保持纯绿（直接覆盖），
 * 之后线性渐隐，到 0.5 秒整完全恢复原样。重复治疗会重新计时（再闪一次）。
 */
public final class GreenFlashState {
    /** 一次闪现的总时长（毫秒）：10 游戏刻 = 0.5 秒。 */
    public static final long FLASH_MS = 500L;
    /** 起手的"纯绿不透明"保持时长（毫秒）：到点后才开始渐隐。 */
    public static final long SOLID_MS = 120L;

    private static final Map<Integer, Long> START_TIMES = new HashMap<>();

    private GreenFlashState() {
    }

    /** 让某个实体开始一波翠绿闪现（重复触发直接重置起点，等于"又绿了一下"）。 */
    public static void trigger(int entityId) {
        START_TIMES.put(entityId, System.currentTimeMillis());
    }

    /**
     * 该实体此刻的蒙皮不透明度：0 = 不在闪现（不画），1 = 纯绿覆盖。
     * 顺带把过期的表项删掉，保持表干净。
     */
    public static float alphaOf(int entityId) {
        Long start = START_TIMES.get(entityId);
        if (start == null) {
            return 0.0F;
        }
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed >= FLASH_MS) {
            START_TIMES.remove(entityId);
            return 0.0F;
        }
        if (elapsed <= SOLID_MS) {
            return 1.0F;
        }
        return (FLASH_MS - elapsed) / (float) (FLASH_MS - SOLID_MS);
    }

    /** 离开世界 / 断线时整表清空，防止实体 ID 复用把旧闪现套到新生物身上。 */
    public static void clear() {
        START_TIMES.clear();
    }
}
