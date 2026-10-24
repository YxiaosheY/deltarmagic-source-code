package cn.blockforge.generated.frostboundthorns;

/** 客户端一侧缓存的 TP 值（由服务端 SyncTpPacket 同步，0~100）。 */
public final class TpBarState {
    /** TP 上限（与服务端 ModEvents.TP_MAX 一致：固定 100 点）。 */
    public static final int MAX = 100;

    private static int tp;

    private TpBarState() {
    }

    public static void set(int value) {
        tp = Math.max(0, Math.min(MAX, value));
    }

    public static int get() {
        return tp;
    }

    public static void reset() {
        tp = 0;
    }
}
