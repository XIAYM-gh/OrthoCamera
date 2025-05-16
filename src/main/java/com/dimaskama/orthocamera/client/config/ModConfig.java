package com.dimaskama.orthocamera.client.config;

import com.dimaskama.orthocamera.client.OrthoCamera;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModConfig extends JsonConfig {
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static final float MIN_SCALE = 0.01F;
    public static final float MAX_SCALE = 10000.0F;
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
    public transient boolean useBlockFaceCulling = true;
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

        if (auto_third_person) {
            if (enabled) {
                prevPerspective = CLIENT.options.getPerspective();
                CLIENT.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            } else if (prevPerspective != null) {
                CLIENT.options.setPerspective(prevPerspective);
            }
        }
        setDirty(true);
    }

    public void updateSodiumSettings() {
        SodiumGameOptions.PerformanceSettings performanceSettings = SodiumClientMod.options().performance;

        if (CLIENT.world == null || !enabled) {
            performanceSettings.useBlockFaceCulling = useBlockFaceCulling;
        } else {
            performanceSettings.useBlockFaceCulling = false;
        }

        if (CLIENT.world != null) {
            CLIENT.worldRenderer.reload();
        }
    }
}
