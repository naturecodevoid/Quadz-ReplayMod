package dev.lazurite.fpvracing.mixin.client;

import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin changes the code so it uses the position of the camera rather
 * than the position of the player if the player using a {@link QuadcopterEntity}.
 * The main effect it has is it allows particles, name tags, etc. to render when
 * not near the actual player entity.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow public abstract void doRandomBlockDisplayTicks(int x, int y, int z);

    @Inject(at = @At("HEAD"), method = "doRandomBlockDisplayTicks", cancellable = true)
    public void blockDisplayTicks(int xCenter, int yCenter, int zCenter, CallbackInfo info) {
        Camera cam = client.gameRenderer.getCamera();
        int camX = (int) cam.getPos().getX();
        int camY = (int) cam.getPos().getY();
        int camZ = (int) cam.getPos().getZ();

        if (camX != xCenter && camY != yCenter && camZ != zCenter) {
            doRandomBlockDisplayTicks(camX, camY, camZ);
        }
    }
}