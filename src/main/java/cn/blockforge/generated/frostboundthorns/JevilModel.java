package cn.blockforge.generated.frostboundthorns;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Jevil 实体模型：顶点数据来自用户上传的 小丑角色-3.bbmodel（离线烘焙进
 * {@link JevilModelData#QUADS}，骨骼旋转全部已应用），这里只负责提交顶点，
 * 并叠加三个运行时可视状态：整体缩放（死亡缩小）、y 轴压缩（byebye 技能压扁）、
 * 死亡时的剧烈抖动。
 */
public final class JevilModel extends EntityModel<JevilEntity> {
    /** 模型脚底在像素 y=24（=1.5 格），实体坐标原点位于"头顶上方一格的脚底"渲染惯例处。 */
    private static final float FEET_OFFSET_BLOCKS = 1.5F;

    private JevilEntity entity;

    public JevilModel() {
        // entityCutoutNoCull：透明镂空、双面不剔除，和原版生物皮肤一致
        super(RenderType::entityCutoutNoCull);
    }

    @Override
    public void setupAnim(JevilEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        this.entity = entity;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
            int packedOverlay, float red, float green, float blue, float alpha) {
        float scale = 1.0F;
        float yScale = 1.0F;
        boolean dying = false;
        int shakeTick = 0;
        if (this.entity != null) {
            scale = this.entity.getVisualScale();
            yScale = this.entity.getVisualYScale();
            dying = this.entity.isDyingAnimation();
            shakeTick = this.entity.tickCount;
        }
        if (scale <= 0.001F || yScale <= 0.001F) {
            return;
        }
        poseStack.pushPose();
        // 以脚底为锚点做缩放：先平移到脚底、缩放、再平移回去（单位：格）
        poseStack.translate(0.0F, FEET_OFFSET_BLOCKS, 0.0F);
        poseStack.scale(scale, scale * yScale, scale);
        poseStack.translate(0.0F, -FEET_OFFSET_BLOCKS, 0.0F);
        if (dying) {
            // 大纲第 5 条：死亡动画期间剧烈抖动（每刻换向的确定性伪随机）
            double sx = Math.sin(shakeTick * 2.7D) * 0.07D * scale;
            double sz = Math.cos(shakeTick * 3.1D) * 0.07D * scale;
            poseStack.translate((float) sx, 0.0F, (float) sz);
        }
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        int r = (int) (red * 255.0F);
        int g = (int) (green * 255.0F);
        int b = (int) (blue * 255.0F);
        int a = (int) (alpha * 255.0F);
        double[] quads = JevilModelData.QUADS;
        int stride = JevilModelData.FLOATS_PER_QUAD;
        for (int q = 0; q + stride <= quads.length; q += stride) {
            float nx = (float) quads[q + 20];
            float ny = (float) quads[q + 21];
            float nz = (float) quads[q + 22];
            for (int v = 0; v < 4; v++) {
                // 顶点像素坐标除以 16 换算成格（与原版 ModelPart 提交方式一致）
                float x = (float) (quads[q + v * 3] / 16.0D);
                float y = (float) (quads[q + v * 3 + 1] / 16.0D);
                float z = (float) (quads[q + v * 3 + 2] / 16.0D);
                float u = (float) quads[q + 12 + v * 2];
                float w = (float) quads[q + 13 + v * 2];
                vertexConsumer.vertex(matrix, x, y, z)
                        .color(r, g, b, a)
                        .uv(u, w)
                        .overlayCoords(packedOverlay)
                        .uv2(packedLight)
                        .normal(normal, nx, ny, nz)
                        .endVertex();
            }
        }
        poseStack.popPose();
    }
}
