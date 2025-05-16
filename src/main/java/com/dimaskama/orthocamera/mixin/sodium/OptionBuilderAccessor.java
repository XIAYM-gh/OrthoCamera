package com.dimaskama.orthocamera.mixin.sodium;

import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionImpl.Builder.class)
public interface OptionBuilderAccessor {
    @Accessor("name")
    Text name();
}
