package cn.blockforge.generated.frostboundthorns;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Jevil Boss 渲染器：bbmodel 烘焙模型 + 64x64 小丑贴图。 */
public final class JevilRenderer extends MobRenderer<JevilEntity, JevilModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            GeneratedMod.MOD_ID, "textures/entity/jevil.png");

    public JevilRenderer(EntityRendererProvider.Context context) {
        super(context, new JevilModel(), 0.5F);
        // 悬浮 Boss 不打阴影，避免一团黑影暴露位置
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(JevilEntity entity) {
        return TEXTURE;
    }
}
