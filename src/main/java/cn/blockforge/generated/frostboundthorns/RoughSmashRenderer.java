package cn.blockforge.generated.frostboundthorns;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/** 粗暴碎击 / 朱红碎击投射物渲染器：把 7 张 beam 贴图按 0-6 顺序循环播放，画成一个始终面向相机的方块。 */
public final class RoughSmashRenderer extends EntityRenderer<RoughSmashProjectile> {
    /** 帧贴图数量。 */
    private static final int FRAME_COUNT = 7;
    /** 每帧显示的刻数（2 刻 ≈ 每 0.1 秒换一帧，一整轮约 0.7 秒）。 */
    private static final int FRAME_DURATION = 2;
    /** 贴图宽高比（宽 43 × 高 62），按此比例拉伸四边形避免贴图变形。 */
    private static final float TEX_ASPECT = 43.0F / 62.0F;

    /** 粗暴碎击：紫色一套帧贴图。 */
    private static final ResourceLocation[] FRAMES = new ResourceLocation[FRAME_COUNT];
    /** 朱红碎击：红色一套帧贴图。 */
    private static final ResourceLocation[] CRIMSON_FRAMES = new ResourceLocation[FRAME_COUNT];

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            FRAMES[i] = ResourceLocation.fromNamespaceAndPath(
                    GeneratedMod.MOD_ID, "textures/entity/rough_smash_beam_" + i + ".png");
            CRIMSON_FRAMES[i] = ResourceLocation.fromNamespaceAndPath(
                    GeneratedMod.MOD_ID, "textures/entity/crimson_smash_beam_" + i + ".png");
        }
    }

    public RoughSmashRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    /** 当前应显示的帧贴图（朱红碎击用红色一套）。 */
    private static ResourceLocation frameTexture(RoughSmashProjectile entity) {
        int frame = (entity.tickCount / FRAME_DURATION) % FRAME_COUNT;
        return entity.isCrimson() ? CRIMSON_FRAMES[frame] : FRAMES[frame];
    }

    @Override
    public ResourceLocation getTextureLocation(RoughSmashProjectile entity) {
        return frameTexture(entity);
    }

    @Override
    public void render(RoughSmashProjectile entity, float entityYaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        Quaternionf cameraOrientation = this.entityRenderDispatcher.cameraOrientation();
        poseStack.mulPose(cameraOrientation);

        float scale = entity.getScale();
        poseStack.scale(scale, scale, scale);

        // 按贴图宽高比设置四边形：高度固定为 1，宽度按比例收缩，避免把 43x62 的竖长贴图压成正方形。
        float halfH = 0.5F;
        float halfW = halfH * TEX_ASPECT;
        float minX = -halfW;
        float maxX = halfW;
        float minY = -halfH;
        float maxY = halfH;

        ResourceLocation texture = frameTexture(entity);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        vertexConsumer.vertex(matrix, minX, minY, 0.0F)
                .color(255, 255, 255, 255)
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
        vertexConsumer.vertex(matrix, maxX, minY, 0.0F)
                .color(255, 255, 255, 255)
                .uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
        vertexConsumer.vertex(matrix, maxX, maxY, 0.0F)
                .color(255, 255, 255, 255)
                .uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
        vertexConsumer.vertex(matrix, minX, maxY, 0.0F)
                .color(255, 255, 255, 255)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
