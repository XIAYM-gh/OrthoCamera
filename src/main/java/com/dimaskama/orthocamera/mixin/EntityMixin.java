package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
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

        OrthoCamera.CONFIG.setFixedYaw(OrthoCamera.CONFIG.fixed_yaw + (float) cursorDeltaX / 10);
        OrthoCamera.CONFIG.setFixedPitch(OrthoCamera.CONFIG.fixed_pitch + (float) cursorDeltaY / 10);
        ci.cancel();
    }
}
