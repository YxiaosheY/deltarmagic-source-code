package cn.blockforge.generated.frostboundthorns;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * 治疗命中的翠绿蒙皮层：沿用生物当前已经摆好的模型姿势，再盖一层纯绿色贴图。
 * 蒙皮略微放大，避免与原模型表面重合产生闪烁；不在闪现期间时只做一次状态表查询。
 */
public final class GreenFlashLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,
                    "textures/misc/green_flash_white.png");
    private static final RenderType GREEN_SHELL = RenderType.entityTranslucent(WHITE_TEXTURE);

    /** 荧光草绿色，保持在暗处也能看清治疗反馈。 */
    private static final float GREEN_R = 0.15F;
    private static final float GREEN_G = 0.90F;
    private static final float GREEN_B = 0.25F;
    /** 让蒙皮浮到原模型表面之外，避免默认深度测试拒绝同深度片元。 */
    private static final float SHELL_SCALE = 1.02F;

    public GreenFlashLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        float alpha = GreenFlashState.alphaOf(entity.getId());
        if (alpha <= 0.0F) {
            return;
        }
        VertexConsumer consumer = bufferSource.getBuffer(GREEN_SHELL);
        poseStack.pushPose();
        poseStack.scale(SHELL_SCALE, SHELL_SCALE, SHELL_SCALE);
        this.getParentModel().renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, GREEN_R, GREEN_G, GREEN_B, alpha);
        poseStack.popPose();
    }
}
