package cn.xiaym.dirtystuff.mixin;

import cn.xiaym.dirtystuff.EntitySelector;
import com.dimaskama.orthocamera.client.OrthoCamera;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
@SuppressWarnings("ConstantValue")
public class LivingEntityMixin {
    @Unique
    private boolean isSelected() {
        return OrthoCamera.isEnabled() && OrthoCamera.CONFIG.fixed && (Object) this == EntitySelector.instance.selectedEntity;
    }

    @WrapMethod(method = "isGlowing")
    private boolean isGlowing(Operation<Boolean> original) {
        if (isSelected()) {
            return true;
        }

        return original.call();
    }

    @Inject(method = "setHealth", at = @At("HEAD"))
    private void setHealth(float health, CallbackInfo ci) {
        if (isSelected() && health <= 0) {
            EntitySelector.instance.selectedEntity = null;
        }
    }
}
