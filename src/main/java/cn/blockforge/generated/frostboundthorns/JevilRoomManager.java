package cn.blockforge.generated.frostboundthorns;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * 进房触发器：每 10 刻检查一次玩家是否站在某个 jevil_room 的包围盒内；
 * 是且该房间还没刷过 Boss，就在模板中心 (12,1,10) 处生成一只 Jevil 并播放变身音效。
 * （房间内部只有一格宽的铁栏窗，玩家正常只能从钥匙拆开的缺口进入——先拆栏，后开打。）
 */
public final class JevilRoomManager {
    public static final ResourceLocation JEVIL_ROOM_ID = ResourceLocation.fromNamespaceAndPath(
            GeneratedMod.MOD_ID, "jevil_room");
    public static final ResourceKey<Structure> JEVIL_ROOM_KEY =
            ResourceKey.create(Registries.STRUCTURE, JEVIL_ROOM_ID);

    private JevilRoomManager() {
    }

    public static void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 10 != 0 || !player.isAlive() || player.isSpectator()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(JEVIL_ROOM_KEY);
        if (structure == null) {
            return;
        }
        StructureStart start = level.structureManager().getStructureAt(player.blockPosition(), structure);
        if (start == null || !start.isValid()) {
            return;
        }
        BoundingBox box = start.getBoundingBox();
        BlockPos here = player.blockPosition();
        if (!box.isInside(here.getX(), here.getY(), here.getZ())) {
            return;
        }
        JevilRoomSpawns data = level.getDataStorage().computeIfAbsent(
                JevilRoomSpawns::load, JevilRoomSpawns::new, "deltamagic_jevil_rooms");
        String roomKey = level.dimension().location() + "|" + box.minX() + "," + box.minY() + "," + box.minZ();
        if (!data.markSpawned(roomKey)) {
            return;
        }
        data.setDirty();
        // 模板内坐标 (12,1,10)：房间地板上一层的中心点
        BlockPos spawnPos = new BlockPos(box.minX() + 12, box.minY() + 1, box.minZ() + 10);
        JevilEntity jevil = GeneratedMod.JEVIL.get().create(level);
        if (jevil == null) {
            return;
        }
        jevil.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
        jevil.setHealth(jevil.getMaxHealth());
        level.addFreshEntity(jevil);
        level.playSound(null, spawnPos, GeneratedMod.JEVIL_METAMORPHOSIS.get(), SoundSource.HOSTILE, 5.0F, 1.0F);
    }
}
