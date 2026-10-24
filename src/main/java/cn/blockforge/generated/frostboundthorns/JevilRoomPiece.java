package cn.blockforge.generated.frostboundthorns;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * jevil_room 结构的单方块件：把 data/deltamagic/structures/jevil_room.nbt
 * 原样放进世界（内部铁栅栏已在转换时替换成 deltamagic:prison_bars）。
 */
public final class JevilRoomPiece extends TemplateStructurePiece {
    private static final ResourceLocation TEMPLATE_LOCATION = ResourceLocation.fromNamespaceAndPath(
            GeneratedMod.MOD_ID, "jevil_room");
    private static final StructurePlaceSettings PLACE_SETTINGS = new StructurePlaceSettings();

    public JevilRoomPiece(StructureTemplateManager manager, BlockPos pos) {
        super(GeneratedMod.JEVIL_ROOM_PIECE.get(), 0, manager, TEMPLATE_LOCATION,
                TEMPLATE_LOCATION.toString(), PLACE_SETTINGS, pos);
    }

    /** 存档重载用的反序列化构造（由 STRUCTURE_PIECE 注册表回调）。 */
    public JevilRoomPiece(StructureTemplateManager manager, CompoundTag tag) {
        super(GeneratedMod.JEVIL_ROOM_PIECE.get(), tag, manager,
                location -> PLACE_SETTINGS);
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos pos, ServerLevelAccessor level,
            RandomSource random, BoundingBox box) {
        // 模板里没有放置实体，也没有 data marker
    }
}
