package com.dimaskama.orthocamera.mixin.entityculling;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.dimaskama.orthocamera.client.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tr7zw.entityculling.EntityCullingModBase;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityCullingModBase.class)
public class EntityCullingModBaseMixin {
    @Redirect(method = "clientTick", at = @At(value = "FIELD", target = "Ldev/tr7zw/entityculling/EntityCullingModBase;enabled:Z", opcode = Opcodes.PUTSTATIC), remap = false)
    private void entityCullingEnabled(boolean enabled) {
        if (OrthoCamera.isEnabled()) {
            return;
        }

        ModConfig.originalEntityCullingState = enabled;
        EntityCullingModBase.enabled = enabled;
    }

    @WrapOperation(method = "clientTick", at = @At(value = "INVOKE", target = "Ldev/tr7zw/transition/mc/ClientUtil;sendChatMessage(Lnet/minecraft/text/Text;)V"))
    private void entityCullingSendChatMessage(Text message, Operation<Void> original) {
        if (OrthoCamera.isEnabled() && message.getContent() instanceof PlainTextContent.Literal(
                String string
        ) && (string.equals("Culling on") || string.equals("Culling off"))) {
            return;
        }

        original.call(message);
    }
}
