package dev.lazurite.fpvracing.client.render.entity;

import com.jme3.math.Quaternion;
import dev.lazurite.fpvracing.client.render.model.VoxelRacerOneModel;
import dev.lazurite.fpvracing.FPVRacing;
import dev.lazurite.fpvracing.common.entity.quads.VoxelRacerOneEntity;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class VoxelRacerOneEntityRenderer extends EntityRenderer<VoxelRacerOneEntity> {
    public static final Identifier texture = new Identifier(FPVRacing.MODID, "textures/drone.png");
    private final VoxelRacerOneModel model = new VoxelRacerOneModel();

    public VoxelRacerOneEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.2F;
    }

    public void render(VoxelRacerOneEntity voxelRacer, float yaw, float delta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.multiply(QuaternionHelper.bulletToMinecraft(voxelRacer.getPhysicsRotation(new Quaternion(), delta)));
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(model.getLayer(this.getTexture(voxelRacer)));
        model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();

        super.render(voxelRacer, yaw, delta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public boolean shouldRender(VoxelRacerOneEntity voxelRacer, Frustum frustum, double x, double y, double z) {
        return voxelRacer.shouldRender(x, y, z);
    }

    @Override
    public Identifier getTexture(VoxelRacerOneEntity voxelRacer) {
        return texture;
    }
}