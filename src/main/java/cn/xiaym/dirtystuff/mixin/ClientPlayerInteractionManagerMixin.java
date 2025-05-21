package cn.xiaym.dirtystuff.mixin;

import cn.xiaym.dirtystuff.EntitySelector;
import com.dimaskama.orthocamera.client.OrthoCamera;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"))
    private void attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (!EntitySelector.featureAvailable() || EntitySelector.instance == null || !OrthoCamera.CONFIG.auto_select_entity) {
            return;
        }

        if (!(target instanceof LivingEntity livingEntity)) {
            return;
        }

        EntitySelector.instance.selectedEntity = livingEntity;
    }
}
