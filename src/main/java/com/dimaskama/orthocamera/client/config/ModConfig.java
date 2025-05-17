package com.dimaskama.orthocamera.client.config;

import com.dimaskama.orthocamera.client.OrthoCamera;
import dev.tr7zw.entityculling.EntityCullingModBase;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModConfig extends JsonConfig {
    public static final float MIN_SCALE = 0.01F;
    public static final float MAX_SCALE = 10000.0F;
    public static boolean originalUseBlockFaceCulling;
    public static boolean originalEntityCullingState = true;
    public boolean enabled = false;
    public boolean save_enabled_state;
    public float scale_x = 3.0F;
    public float scale_y = 3.0F;
    public float min_distance = -1000.0F;
    public float max_distance = 1000.0F;
    public boolean fixed = false;
    public float fixed_yaw = 0.0F;
    public float fixed_pitch = 0.0F;
    public float fixed_rotate_speed_y = 3.0F;
    public float fixed_rotate_speed_x = 3.0F;
    public boolean auto_third_person = true;
    private transient boolean dirty;
    private transient float prevScaleX;
    private transient float prevScaleY;
    private transient float prevFixedYaw;
    private transient float prevFixedPitch;
    private transient Perspective prevPerspective;

    public ModConfig(String path, String defaultPath) {
        super(path, defaultPath);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void tick() {
        prevScaleX = scale_x;
        prevScaleY = scale_y;
        prevFixedYaw = fixed_yaw;
        prevFixedPitch = fixed_pitch;
    }

    public float getScaleX(float delta) {
        return MathHelper.lerp(delta, prevScaleX, scale_x);
    }

    public float getScaleY(float delta) {
        return MathHelper.lerp(delta, prevScaleY, scale_y);
    }

    public float getFixedYaw(float delta) {
        return MathHelper.lerpAngleDegrees(delta, prevFixedYaw, fixed_yaw);
    }

    public float getFixedPitch(float delta) {
        return MathHelper.lerpAngleDegrees(delta, prevFixedPitch, fixed_pitch);
    }

    public void setScaleX(float scale) {
        scale = MathHelper.clamp(scale, MIN_SCALE, MAX_SCALE);
        if (scale != scale_x) {
            scale_x = scale;
            setDirty(true);
        }
    }

    public void setScaleY(float scale) {
        scale = MathHelper.clamp(scale, MIN_SCALE, MAX_SCALE);
        if (scale != scale_y) {
            scale_y = scale;
            setDirty(true);
        }
    }

    public void setFixedYaw(float yaw) {
        if (yaw < 0) {
            yaw = 360 + yaw;
        }
        yaw = yaw % 360;
        if (yaw != fixed_yaw) {
            fixed_yaw = yaw;
            setDirty(true);
        }
    }

    public void setFixedPitch(float pitch) {
        pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
        if (pitch != fixed_pitch) {
            fixed_pitch = pitch;
            setDirty(true);
        }
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
        if (fixed) {
            Entity entity = MinecraftClient.getInstance().getCameraEntity();
            if (entity != null) {
                setFixedYaw(entity.getYaw() + 180);
                prevFixedYaw = fixed_yaw;
                setFixedPitch(entity.getPitch());
                prevFixedPitch = fixed_pitch;
            }
        }
        setDirty(true);
    }

    public void toggle() {
        enabled = !enabled;
        updateSodiumSettings();
        updateEntityCullingSettings();

        if (auto_third_person) {
            if (enabled) {
                prevPerspective = OrthoCamera.CLIENT.options.getPerspective();
                OrthoCamera.CLIENT.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            } else if (prevPerspective != null) {
                OrthoCamera.CLIENT.options.setPerspective(prevPerspective);
            }
        }
        setDirty(true);
    }

    public void updateSodiumSettings() {
        if (!FabricLoader.getInstance().isModLoaded("sodium")) {
            return;
        }

        SodiumClientMod.options().performance.useBlockFaceCulling = !OrthoCamera.isEnabled() && originalUseBlockFaceCulling;
        if (OrthoCamera.CLIENT.world != null) {
            OrthoCamera.CLIENT.worldRenderer.reload();
        }
    }

    public void updateEntityCullingSettings() {
        if (!FabricLoader.getInstance().isModLoaded("entityculling")) {
            return;
        }

        EntityCullingModBase.enabled = !OrthoCamera.isEnabled() && originalEntityCullingState;
    }
}
