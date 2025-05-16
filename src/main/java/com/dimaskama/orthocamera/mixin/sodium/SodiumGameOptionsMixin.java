package com.dimaskama.orthocamera.mixin.sodium;

import com.dimaskama.orthocamera.client.config.ModConfig;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SodiumGameOptions.class)
public class SodiumGameOptionsMixin {
    @Inject(method = "loadFromDisk", at = @At("RETURN"), remap = false)
    private static void loadFromDisk(CallbackInfoReturnable<SodiumGameOptions> cir, @Local SodiumGameOptions config) {
        ModConfig.originalUseBlockFaceCulling = config.performance.useBlockFaceCulling;
    }
}
