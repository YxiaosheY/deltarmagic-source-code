package cn.blockforge.generated.frostboundthorns;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Jevil 弹幕渲染器：四种 kind 各自对应一张 Undertale 风白底贴图，
 * 一律画成面向相机的发光贴片（billboard），镰刀与旋转木马额外自转。
 */
public final class JevilBulletRenderer extends EntityRenderer<JevilBullet> {
    private static final ResourceLocation CLUB_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            GeneratedMod.MOD_ID, "textures/entity/spr_clubsbullet.png");
    private static final ResourceLocation DIAMOND_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            GeneratedMod.MOD_ID, "textures/entity/spr_diamondbullet_vert.png");
    private static final ResourceLocation SCYTHE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            GeneratedMod.MOD_ID, "textures/entity/spr_joker_scythebody_ch1.png");
    private static final ResourceLocation CAROUSEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            GeneratedMod.MOD_ID, "textures/entity/spr_carousel_ch1_0.png");

    public JevilBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(JevilBullet entity) {
        switch (entity.getKind()) {
            case JevilBullet.KIND_DIAMOND:
                return DIAMOND_TEXTURE;
            case JevilBullet.KIND_SCYTHE:
                return SCYTHE_TEXTURE;
            case JevilBullet.KIND_CAROUSEL:
                return CAROUSEL_TEXTURE;
            default:
                return CLUB_TEXTURE;
        }
    }

    @Override
    public void render(JevilBullet entity, float entityYaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        int kind = entity.getKind();
        float height;
        float width;
        // 自转：镰刀翻滚、木马旋转（绕视线轴）
        float spin = 0.0F;
        switch (kind) {
            case JevilBullet.KIND_DIAMOND:
                height = 0.85F;
                width = height;
                break;
            case JevilBullet.KIND_SCYTHE:
                height = 1.8F;
                width = height * 48.0F / 45.0F;
                spin = (entity.tickCount + partialTicks) * 0.35F;
                break;
            case JevilBullet.KIND_CAROUSEL:
                height = 1.5F;
                width = height * 36.0F / 29.0F;
                spin = (entity.tickCount + partialTicks) * 0.25F;
                break;
            default:
                height = 0.75F;
                width = height;
                spin = (entity.tickCount + partialTicks) * 0.2F;
                break;
        }
        if (spin != 0.0F) {
            poseStack.mulPose(new Quaternionf().rotationZ(spin));
        }
        drawBillboard(poseStack, buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity))),
                width, height, packedLight);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /** 在当前（已面向相机的）姿态下画一张 width×height 的贴图贴片。 */
    private static void drawBillboard(PoseStack poseStack, VertexConsumer consumer,
            float width, float height, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float halfW = width / 2.0F;
        float halfH = height / 2.0F;
        consumer.vertex(matrix, -halfW, -halfH, 0.0F).color(255, 255, 255, 255)
                .uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, halfW, -halfH, 0.0F).color(255, 255, 255, 255)
                .uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, halfW, halfH, 0.0F).color(255, 255, 255, 255)
                .uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, -halfW, halfH, 0.0F).color(255, 255, 255, 255)
                .uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
    }
}
