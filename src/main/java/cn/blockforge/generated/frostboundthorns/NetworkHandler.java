package cn.blockforge.generated.frostboundthorns;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int nextMessageId;

    static {
        CHANNEL.messageBuilder(SnowBurialInputPacket.class, nextMessageId++)
                .encoder(SnowBurialInputPacket::encode)
                .decoder(SnowBurialInputPacket::decode)
                .consumerMainThread(SnowBurialInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(RoughSmashInputPacket.class, nextMessageId++)
                .encoder(RoughSmashInputPacket::encode)
                .decoder(RoughSmashInputPacket::decode)
                .consumerMainThread(RoughSmashInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(CrimsonSmashInputPacket.class, nextMessageId++)
                .encoder(CrimsonSmashInputPacket::encode)
                .decoder(CrimsonSmashInputPacket::decode)
                .consumerMainThread(CrimsonSmashInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(IceShockInputPacket.class, nextMessageId++)
                .encoder(IceShockInputPacket::encode)
                .decoder(IceShockInputPacket::decode)
                .consumerMainThread(IceShockInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(HealPrayerInputPacket.class, nextMessageId++)
                .encoder(HealPrayerInputPacket::encode)
                .decoder(HealPrayerInputPacket::decode)
                .consumerMainThread(HealPrayerInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(SleepSprayInputPacket.class, nextMessageId++)
                .encoder(SleepSprayInputPacket::encode)
                .decoder(SleepSprayInputPacket::decode)
                .consumerMainThread(SleepSprayInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(SpiralClubsInputPacket.class, nextMessageId++)
                .encoder(SpiralClubsInputPacket::encode)
                .decoder(SpiralClubsInputPacket::decode)
                .consumerMainThread(SpiralClubsInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(DiamondUppercutInputPacket.class, nextMessageId++)
                .encoder(DiamondUppercutInputPacket::encode)
                .decoder(DiamondUppercutInputPacket::decode)
                .consumerMainThread(DiamondUppercutInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(JevilScytheInputPacket.class, nextMessageId++)
                .encoder(JevilScytheInputPacket::encode)
                .decoder(JevilScytheInputPacket::decode)
                .consumerMainThread(JevilScytheInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(OkHealInputPacket.class, nextMessageId++)
                .encoder(OkHealInputPacket::encode)
                .decoder(OkHealInputPacket::decode)
                .consumerMainThread(OkHealInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(OkHealFireInputPacket.class, nextMessageId++)
                .encoder(OkHealFireInputPacket::encode)
                .decoder(OkHealFireInputPacket::decode)
                .consumerMainThread(OkHealFireInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncTpPacket.class, nextMessageId++)
                .encoder(SyncTpPacket::encode)
                .decoder(SyncTpPacket::decode)
                .consumerMainThread(SyncTpPacket::handle)
                .add();
        CHANNEL.messageBuilder(SnowGraveSyncPacket.class, nextMessageId++)
                .encoder(SnowGraveSyncPacket::encode)
                .decoder(SnowGraveSyncPacket::decode)
                .consumerMainThread(SnowGraveSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(GreenFlashSyncPacket.class, nextMessageId++)
                .encoder(GreenFlashSyncPacket::encode)
                .decoder(GreenFlashSyncPacket::decode)
                .consumerMainThread(GreenFlashSyncPacket::handle)
                .add();
    }

    private NetworkHandler() {
    }

    public static void initialize() {
        // Loading this class registers the packet before either side can send it.
    }

    public static void sendSnowBurialInput(boolean pressed) {
        CHANNEL.sendToServer(new SnowBurialInputPacket(pressed));
    }

    public static void sendRoughSmashInput(boolean pressed) {
        CHANNEL.sendToServer(new RoughSmashInputPacket(pressed));
    }

    public static void sendCrimsonSmashInput(boolean pressed) {
        CHANNEL.sendToServer(new CrimsonSmashInputPacket(pressed));
    }

    public static void sendIceShockInput(boolean pressed) {
        CHANNEL.sendToServer(new IceShockInputPacket(pressed));
    }

    public static void sendHealPrayerInput(boolean pressed) {
        CHANNEL.sendToServer(new HealPrayerInputPacket(pressed));
    }

    public static void sendSleepSprayInput(boolean pressed) {
        CHANNEL.sendToServer(new SleepSprayInputPacket(pressed));
    }

    public static void sendSpiralClubsInput(boolean pressed) {
        CHANNEL.sendToServer(new SpiralClubsInputPacket(pressed));
    }

    public static void sendDiamondUppercutInput(boolean pressed) {
        CHANNEL.sendToServer(new DiamondUppercutInputPacket(pressed));
    }

    public static void sendJevilScytheInput(boolean pressed) {
        CHANNEL.sendToServer(new JevilScytheInputPacket(pressed));
    }

    public static void sendOkHealInput(boolean pressed) {
        CHANNEL.sendToServer(new OkHealInputPacket(pressed));
    }

    /** 对准空气右键：请求服务端把一枚环绕中的治疗光球沿视线射出（OK治疗）。 */
    public static void sendOkHealFireInput(boolean pressed) {
        CHANNEL.sendToServer(new OkHealFireInputPacket(pressed));
    }

    /** 服务端把玩家当前的 TP 值和可用法术位图推给对应的客户端（左侧 TP 条 + 顶部法术菜单）。 */
    public static void syncTp(net.minecraft.server.level.ServerPlayer player, int tp, int spellBits) {
        CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new SyncTpPacket(tp, spellBits));
    }

    /**
     * 服务端把某个实体的 TicksSnowGrave（雪墓值）推给"所有追踪该实体的客户端 +（该实体是玩家时）本人"：
     * >40 的屏幕冰块贴图与 =140 的包裹冰块模型都靠这条同步驱动。
     */
    public static void syncSnowGrave(net.minecraft.world.entity.Entity entity, int value) {
        CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new SnowGraveSyncPacket(entity.getId(), value));
    }

    /** 服务端把治疗命中的生物推给所有追踪者和生物本人的客户端，播放 0.5 秒翠绿闪现。 */
    public static void sendGreenFlash(net.minecraft.world.entity.Entity entity) {
        CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new GreenFlashSyncPacket(entity.getId()));
    }

    public static final class SnowBurialInputPacket {
        private final boolean pressed;

        private SnowBurialInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(SnowBurialInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static SnowBurialInputPacket decode(FriendlyByteBuf buffer) {
            return new SnowBurialInputPacket(buffer.readBoolean());
        }

        private static void handle(SnowBurialInputPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleSnowBurialInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class RoughSmashInputPacket {
        private final boolean pressed;

        private RoughSmashInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(RoughSmashInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static RoughSmashInputPacket decode(FriendlyByteBuf buffer) {
            return new RoughSmashInputPacket(buffer.readBoolean());
        }

        private static void handle(RoughSmashInputPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleRoughSmashInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class CrimsonSmashInputPacket {
        private final boolean pressed;

        private CrimsonSmashInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(CrimsonSmashInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static CrimsonSmashInputPacket decode(FriendlyByteBuf buffer) {
            return new CrimsonSmashInputPacket(buffer.readBoolean());
        }

        private static void handle(CrimsonSmashInputPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleCrimsonSmashInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class IceShockInputPacket {
        private final boolean pressed;

        private IceShockInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(IceShockInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static IceShockInputPacket decode(FriendlyByteBuf buffer) {
            return new IceShockInputPacket(buffer.readBoolean());
        }

        private static void handle(IceShockInputPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleIceShockInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class HealPrayerInputPacket {
        private final boolean pressed;

        private HealPrayerInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(HealPrayerInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static HealPrayerInputPacket decode(FriendlyByteBuf buffer) {
            return new HealPrayerInputPacket(buffer.readBoolean());
        }

        private static void handle(HealPrayerInputPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleHealPrayerInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class SleepSprayInputPacket {
        private final boolean pressed;

        private SleepSprayInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(SleepSprayInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static SleepSprayInputPacket decode(FriendlyByteBuf buffer) {
            return new SleepSprayInputPacket(buffer.readBoolean());
        }

        private static void handle(SleepSprayInputPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleSleepSprayInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class SpiralClubsInputPacket {
        private final boolean pressed;

        private SpiralClubsInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(SpiralClubsInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static SpiralClubsInputPacket decode(FriendlyByteBuf buffer) {
            return new SpiralClubsInputPacket(buffer.readBoolean());
        }

        private static void handle(SpiralClubsInputPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleSpiralClubsInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class DiamondUppercutInputPacket {
        private final boolean pressed;

        private DiamondUppercutInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(DiamondUppercutInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static DiamondUppercutInputPacket decode(FriendlyByteBuf buffer) {
            return new DiamondUppercutInputPacket(buffer.readBoolean());
        }

        private static void handle(DiamondUppercutInputPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleDiamondUppercutInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class JevilScytheInputPacket {
        private final boolean pressed;

        private JevilScytheInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(JevilScytheInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static JevilScytheInputPacket decode(FriendlyByteBuf buffer) {
            return new JevilScytheInputPacket(buffer.readBoolean());
        }

        private static void handle(JevilScytheInputPacket message,
                Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleJevilScytheInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class OkHealInputPacket {
        private final boolean pressed;

        private OkHealInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(OkHealInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static OkHealInputPacket decode(FriendlyByteBuf buffer) {
            return new OkHealInputPacket(buffer.readBoolean());
        }

        private static void handle(OkHealInputPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleOkHealInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    /** C2S：OK治疗"对准空气右键射出治疗光球"输入。 */
    public static final class OkHealFireInputPacket {
        private final boolean pressed;

        private OkHealFireInputPacket(boolean pressed) {
            this.pressed = pressed;
        }

        private static void encode(OkHealFireInputPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.pressed);
        }

        private static OkHealFireInputPacket decode(FriendlyByteBuf buffer) {
            return new OkHealFireInputPacket(buffer.readBoolean());
        }

        private static void handle(OkHealFireInputPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    ModEvents.handleOkHealFireInput(context.getSender(), message.pressed);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class SyncTpPacket {
        private final int tp;
        private final int spellBits;

        SyncTpPacket(int tp, int spellBits) {
            this.tp = tp;
            this.spellBits = spellBits;
        }

        private static void encode(SyncTpPacket message, FriendlyByteBuf buffer) {
            buffer.writeInt(message.tp);
            buffer.writeInt(message.spellBits);
        }

        private static SyncTpPacket decode(FriendlyByteBuf buffer) {
            return new SyncTpPacket(buffer.readInt(), buffer.readInt());
        }

        private static void handle(SyncTpPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                TpBarState.set(message.tp);
                SpellMenuState.setSpellBits(message.spellBits);
            });
            context.setPacketHandled(true);
        }
    }

    /** S2C：治疗类法术命中/治疗了某个生物，客户端开始 0.5 秒翠绿闪现。 */
    public static final class GreenFlashSyncPacket {
        private final int entityId;

        GreenFlashSyncPacket(int entityId) {
            this.entityId = entityId;
        }

        private static void encode(GreenFlashSyncPacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.entityId);
        }

        private static GreenFlashSyncPacket decode(FriendlyByteBuf buffer) {
            return new GreenFlashSyncPacket(buffer.readVarInt());
        }

        private static void handle(GreenFlashSyncPacket message,
                Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> GreenFlashState.trigger(message.entityId));
            context.setPacketHandled(true);
        }
    }

    /** S2C：某个实体的 TicksSnowGrave（雪墓值）发生变化（≤0 表示清除）。 */
    public static final class SnowGraveSyncPacket {
        private final int entityId;
        private final int value;

        SnowGraveSyncPacket(int entityId, int value) {
            this.entityId = entityId;
            this.value = value;
        }

        private static void encode(SnowGraveSyncPacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.entityId);
            buffer.writeVarInt(message.value);
        }

        private static SnowGraveSyncPacket decode(FriendlyByteBuf buffer) {
            return new SnowGraveSyncPacket(buffer.readVarInt(), buffer.readVarInt());
        }

        private static void handle(SnowGraveSyncPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> SnowGraveState.set(message.entityId, message.value));
            context.setPacketHandled(true);
        }
    }
}
