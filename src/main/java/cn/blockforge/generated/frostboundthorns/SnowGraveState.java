package cn.blockforge.generated.frostboundthorns;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端缓存的「TicksSnowGrave（雪墓值）」表：实体网络 ID → 当前数值。
 * 纯数据容器（不引用任何客户端类），服务端线程也能安全加载；
 * 数值由 SnowGraveSyncPacket 从服务端推过来写入，渲染层只读取。
 */
public final class SnowGraveState {
    private static final Map<Integer, Integer> VALUES = new HashMap<>();

    private SnowGraveState() {
    }

    /** 写入某个实体的雪墓值；value ≤ 0 时直接删表项（保持表干净）。 */
    public static void set(int entityId, int value) {
        if (value > 0) {
            VALUES.put(entityId, value);
        } else {
            VALUES.remove(entityId);
        }
    }

    public static int get(int entityId) {
        return VALUES.getOrDefault(entityId, 0);
    }

    /** 离开世界 / 断线时整表清空，防止实体 ID 复用导致新生物带着旧状态。 */
    public static void clear() {
        VALUES.clear();
    }
}
