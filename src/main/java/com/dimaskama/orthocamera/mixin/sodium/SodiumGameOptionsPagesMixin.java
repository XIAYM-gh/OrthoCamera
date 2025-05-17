package com.dimaskama.orthocamera.mixin.sodium;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.dimaskama.orthocamera.client.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptions;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SodiumGameOptionPages.class)
public class SodiumGameOptionsPagesMixin {
    @WrapOperation(method = "performance", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gui/options/OptionImpl$Builder;build()Lnet/caffeinemc/mods/sodium/client/gui/options/OptionImpl;"), remap = false)
    private static OptionImpl<?, ?> performance(OptionImpl.Builder<SodiumGameOptions, Boolean> instance, Operation<OptionImpl<?, ?>> original) {
        if (((OptionBuilderAccessor) instance).name()
                .getContent() instanceof TranslatableTextContent content && content.getKey()
                .equals("sodium.options.use_block_face_culling.name")) {
            instance.setEnabled(() -> !OrthoCamera.isEnabled())
                    .setBinding((SodiumGameOptions opt, Boolean value) -> {
                        opt.performance.useBlockFaceCulling = value;
                        ModConfig.originalUseBlockFaceCulling = value;
                    }, (SodiumGameOptions opts) -> ModConfig.originalUseBlockFaceCulling);
        }

        return instance.build();
    }
}
