package cn.blockforge.generated.frostboundthorns;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * jevil_room（小丑牢房）：雪地生物群系地表上的独立结构。
 * 1.20.1 没有数据驱动的 template 结构类型，这里用 Java Structure 子类 +
 * data/deltamagic/worldgen/structure/jevil_room.json 绑定生物群系与生成频率。
 */
public final class JevilRoomStructure extends Structure {
    public static final com.mojang.serialization.Codec<JevilRoomStructure> CODEC =
            simpleCodec(JevilRoomStructure::new);

    public JevilRoomStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return GeneratedMod.JEVIL_ROOM_STRUCTURE_TYPE.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        // 房间 25x21：让模板中心大致落在区块中心
        int x = chunkPos.getMiddleBlockX() - 12;
        int z = chunkPos.getMiddleBlockZ() - 10;
        int surfaceY = context.chunkGenerator().getBaseHeight(
                x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        // 地板沉到地表下 1 格，房间主体露出雪原
        BlockPos pos = new BlockPos(x, surfaceY - 1, z);
        return Optional.of(new GenerationStub(pos, builder -> builder.addPiece(
                new JevilRoomPiece(context.structureTemplateManager(), pos))));
    }
}
