package cn.blockforge.generated.frostboundthorns;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 世界存档数据：记录哪些 jevil_room 已经放出过 Boss（按维度 + 结构包围盒左下角唯一标识）。
 * 保证"玩家进入房间内部 → 生成一只 Jevil"每间房只触发一次，退出重进不会重复刷。
 */
public final class JevilRoomSpawns extends SavedData {
    private final Set<String> spawnedRooms = new LinkedHashSet<>();

    public static JevilRoomSpawns load(CompoundTag tag) {
        JevilRoomSpawns data = new JevilRoomSpawns();
        ListTag list = tag.getList("SpawnedRooms", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            data.spawnedRooms.add(list.getString(i));
        }
        return data;
    }

    /** 首次记录返回 true（该房间还没刷过 Boss），重复返回 false。 */
    public boolean markSpawned(String roomKey) {
        return this.spawnedRooms.add(roomKey);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String key : this.spawnedRooms) {
            list.add(StringTag.valueOf(key));
        }
        tag.put("SpawnedRooms", list);
        return tag;
    }
}
