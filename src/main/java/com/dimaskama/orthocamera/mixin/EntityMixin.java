package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Unique
    private static float normalizeYaw(float yaw) {
        if (yaw > 360) {
            return normalizeYaw(yaw - 360);
        }

        if (yaw < 0) {
            return normalizeYaw(yaw + 360);
        }

        return yaw;
    }

    @Unique
    private static float normalizePitch(float pitch) {
        if (pitch > 90) {
            return 90;
        }

        if (pitch < -90) {
            return -90;
        }

        return pitch;
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), order = 999, cancellable = true)
    private void changeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof ClientPlayerEntity)) {
            return;
        }

        if (!OrthoCamera.isEnabled() || !OrthoCamera.CONFIG.fixed || !InputUtil.isKeyPressed(OrthoCamera.CLIENT.getWindow()
                .getHandle(), InputUtil.GLFW_KEY_LEFT_ALT)) {
            return;
        }

        OrthoCamera.CONFIG.fixed_yaw = normalizeYaw(OrthoCamera.CONFIG.fixed_yaw + (float) cursorDeltaX / 10);
        OrthoCamera.CONFIG.fixed_pitch = normalizePitch(OrthoCamera.CONFIG.fixed_pitch + (float) cursorDeltaY / 10);
        ci.cancel();
    }
}
