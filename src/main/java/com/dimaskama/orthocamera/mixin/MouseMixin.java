package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Unique
    private static final long HANDLE = OrthoCamera.CLIENT.getWindow().getHandle();

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!OrthoCamera.isEnabled() || !InputUtil.isKeyPressed(HANDLE, InputUtil.GLFW_KEY_LEFT_ALT)) {
            return;
        }

        if (vertical == 1) {
            OrthoCamera.decreaseScale();
        } else if (vertical == -1) {
            OrthoCamera.increaseScale();
        }

        OrthoCamera.sendScaleMessage();
        ci.cancel();
    }
}
