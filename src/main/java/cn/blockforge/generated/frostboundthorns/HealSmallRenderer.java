package cn.blockforge.generated.frostboundthorns;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 治疗光球渲染器：spr_cutscene_18_susie_heal_small 的三帧绿色十字光点，
 * 每 6 游戏刻换一帧循环播放。朝向分两段：
 * 环绕时贴片正面始终面向施法者（眼睛位置），射出后锁定在飞行方向上不再变化；
 * 找不到施法者（掉出加载范围等）时退回普通的面向相机贴片。
 * 贴图 13×7，贴片按同比例取 0.65 × 0.35 格。
 */
public final class HealSmallRenderer extends EntityRenderer<HealSmallEntity> {
    private static final ResourceLocation[] FRAMES = {
            entityTexture("heal_small_0"),
            entityTexture("heal_small_1"),
            entityTexture("heal_small_2"),
    };
    /** 贴片宽高（格），13:7 的原始比例。 */
    private static final float WIDTH = 0.65F;
    private static final float HEIGHT = 0.35F;

    public HealSmallRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    private static ResourceLocation entityTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                GeneratedMod.MOD_ID, "textures/entity/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(HealSmallEntity entity) {
        return FRAMES[entity.animationFrame(0.0F) % FRAMES.length];
    }

    @Override
    public void render(HealSmallEntity entity, float entityYaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        Vec3 facing = computeFacing(entity, partialTicks);
        if (facing != null) {
            // 把贴片的 +Z 法线转到目标方向：环绕=面向玩家，射出=面向飞行方向（固定）
            Vector3f dir = new Vector3f((float) facing.x, (float) facing.y, (float) facing.z).normalize();
            poseStack.mulPose(new Quaternionf().rotationTo(new Vector3f(0.0F, 0.0F, 1.0F), dir));
        } else {
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        }
        ResourceLocation texture = FRAMES[entity.animationFrame(partialTicks) % FRAMES.length];
        drawBillboard(poseStack, buffer.getBuffer(RenderType.entityTranslucent(texture)),
                WIDTH, HEIGHT);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * 贴片正面该朝向的单位向量；返回 null 表示退回面向相机。
     * 射出：实体自己的 yRot/xRot（服务端射出瞬间锁定，随位置包同步）推出的视线向量；
     * 环绕：光球插值位置指向施法者眼睛的向量。
     */
    private static Vec3 computeFacing(HealSmallEntity entity, float partialTicks) {
        if (entity.isLaunched()) {
            Vec3 look = entity.getViewVector(partialTicks);
            return look.lengthSqr() > 1.0E-6D ? look : null;
        }
        Entity owner = entity.ownerUuid().map(id -> findOwnerPlayer(entity, id)).orElse(null);
        if (owner == null) {
            return null;
        }
        Vec3 facing = owner.getEyePosition(partialTicks).subtract(entity.getPosition(partialTicks));
        return facing.lengthSqr() > 1.0E-6D ? facing : null;
    }

    /** 客户端按 UUID 找回施法者玩家实体（光球主人一定是玩家；找不到返回 null）。 */
    private static Entity findOwnerPlayer(HealSmallEntity orb, java.util.UUID ownerUuid) {
        for (net.minecraft.world.entity.player.Player player : orb.level().players()) {
            if (player.getUUID().equals(ownerUuid)) {
                return player;
            }
        }
        return null;
    }

    /** 在当前姿态下画一张 width×height 的全亮贴片（光球自发光，不受环境光影响）。 */
    private static void drawBillboard(PoseStack poseStack, VertexConsumer consumer,
            float width, float height) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float halfW = width / 2.0F;
        float halfH = height / 2.0F;
        int light = LightTexture.FULL_BRIGHT;
        consumer.vertex(matrix, -halfW, -halfH, 0.0F).color(255, 255, 255, 255)
                .uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, halfW, -halfH, 0.0F).color(255, 255, 255, 255)
                .uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, halfW, halfH, 0.0F).color(255, 255, 255, 255)
                .uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, -halfW, halfH, 0.0F).color(255, 255, 255, 255)
                .uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
    }
}
