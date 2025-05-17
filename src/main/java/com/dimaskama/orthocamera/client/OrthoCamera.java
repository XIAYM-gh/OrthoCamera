package com.dimaskama.orthocamera.client;

import cn.xiaym.dirtystuff.EntitySelector;
import com.dimaskama.orthocamera.client.config.ModConfig;
import com.dimaskama.orthocamera.client.config.ModConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class OrthoCamera implements ClientModInitializer {
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static final String MOD_ID = "orthocamera";
    public static final Logger LOGGER = LogManager.getLogger("OrthoCamera");
    public static final ModConfig CONFIG = new ModConfig("config/orthocamera.json", "assets/orthocamera/default_config.json");
    private static final KeyBinding TOGGLE_KEY = createKeybinding("toggle", GLFW.GLFW_KEY_KP_4);
    private static final KeyBinding SCALE_INCREASE_KEY = createKeybinding("scale_increase", GLFW.GLFW_KEY_KP_SUBTRACT);
    private static final KeyBinding SCALE_DECREASE_KEY = createKeybinding("scale_decrease", GLFW.GLFW_KEY_KP_ADD);
    private static final KeyBinding OPEN_OPTIONS_KEY = createKeybinding("options", -1);
    private static final KeyBinding FIX_CAMERA_KEY = createKeybinding("fix_camera", GLFW.GLFW_KEY_KP_MULTIPLY);
    private static final KeyBinding FIXED_CAMERA_ROTATE_UP_KEY = createKeybinding("fixed_camera_rotate_up", -1);
    private static final KeyBinding FIXED_CAMERA_ROTATE_DOWN_KEY = createKeybinding("fixed_camera_rotate_down", -1);
    private static final KeyBinding FIXED_CAMERA_ROTATE_LEFT_KEY = createKeybinding("fixed_camera_rotate_left", -1);
    private static final KeyBinding FIXED_CAMERA_ROTATE_RIGHT_KEY = createKeybinding("fixed_camera_rotate_right", -1);
    private static final KeyBinding SELECT_NEAREST_ENTITY_KEY = createKeybinding("select_nearest_entity", GLFW.GLFW_KEY_DOWN);
    private static final KeyBinding SELECT_NEXT_ENTITY_KEY = createKeybinding("select_next_entity", GLFW.GLFW_KEY_RIGHT);
    private static final KeyBinding SELECT_ENTITY_AUTO_KEY = createKeybinding("select_entity_auto", GLFW.GLFW_KEY_UP);
    private static final KeyBinding SELECT_ENTITY_STOP_KEY = createKeybinding("select_entity_stop", GLFW.GLFW_KEY_LEFT);
    private static final Text ENABLED_TEXT = Text.translatable("orthocamera.enabled");
    private static final Text DISABLED_TEXT = Text.translatable("orthocamera.disabled");
    private static final Text FIXED_TEXT = Text.translatable("orthocamera.fixed");
    private static final Text UNFIXED_TEXT = Text.translatable("orthocamera.unfixed");
    private static final Text AUTO_SELECT_ENABLED_TEXT = Text.translatable("orthocamera.auto_select_enabled");
    private static final Text AUTO_SELECT_DISABLED_TEXT = Text.translatable("orthocamera.auto_select_disabled");
    private static final Text SELECT_STOPPED = Text.translatable("orthocamera.selecting_stopped");
    private static final float SCALE_MUL_INTERVAL = 1.1F;

    public static boolean isEnabled() {
        return CONFIG.enabled && CLIENT.world != null;
    }

    public static Matrix4f createOrthoMatrix(float delta, float minScale) {
        float width = Math.max(minScale, CONFIG.getScaleX(delta) * CLIENT.getWindow()
                .getFramebufferWidth() / CLIENT.getWindow().getFramebufferHeight());
        float height = Math.max(minScale, CONFIG.getScaleY(delta));
        return new Matrix4f().setOrtho(-width, width, -height, height, CONFIG.min_distance, CONFIG.max_distance);
    }

    private static KeyBinding createKeybinding(String name, int key) {
        return new KeyBinding("orthocamera.key." + name, InputUtil.Type.KEYSYM, key, MOD_ID);
    }

    public static void sendScaleMessage() {
        CLIENT.getMessageHandler()
                .onGameMessage(Text.translatable("orthocamera.scale", String.format("%.1f", CONFIG.scale_x), String.format("%.1f", CONFIG.scale_y)), true);
    }

    public static void increaseScale() {
        CONFIG.setScaleX(CONFIG.scale_x * SCALE_MUL_INTERVAL);
        CONFIG.setScaleY(CONFIG.scale_y * SCALE_MUL_INTERVAL);
        CONFIG.setDirty(true);
    }

    public static void decreaseScale() {
        CONFIG.setScaleX(CONFIG.scale_x / SCALE_MUL_INTERVAL);
        CONFIG.setScaleY(CONFIG.scale_y / SCALE_MUL_INTERVAL);
        CONFIG.setDirty(true);
    }

    @Override
    public void onInitializeClient() {
        CONFIG.loadOrCreate();
        CONFIG.enabled &= CONFIG.save_enabled_state;

        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);
        KeyBindingHelper.registerKeyBinding(SCALE_INCREASE_KEY);
        KeyBindingHelper.registerKeyBinding(SCALE_DECREASE_KEY);
        KeyBindingHelper.registerKeyBinding(OPEN_OPTIONS_KEY);
        KeyBindingHelper.registerKeyBinding(FIX_CAMERA_KEY);
        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_UP_KEY);
        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_DOWN_KEY);
        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_LEFT_KEY);
        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_RIGHT_KEY);
        KeyBindingHelper.registerKeyBinding(SELECT_NEAREST_ENTITY_KEY);
        KeyBindingHelper.registerKeyBinding(SELECT_NEXT_ENTITY_KEY);
        KeyBindingHelper.registerKeyBinding(SELECT_ENTITY_AUTO_KEY);
        KeyBindingHelper.registerKeyBinding(SELECT_ENTITY_STOP_KEY);

        ClientTickEvents.START_CLIENT_TICK.register(c -> CONFIG.tick());
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
    }

    private void onEndClientTick(MinecraftClient client) {
        while (TOGGLE_KEY.wasPressed()) {
            CONFIG.toggle();
            client.getMessageHandler().onGameMessage(CONFIG.enabled ? ENABLED_TEXT : DISABLED_TEXT, true);
        }

        boolean on = CONFIG.enabled;
        boolean scaleChanged = false;
        while (SCALE_INCREASE_KEY.wasPressed()) {
            if (on) {
                increaseScale();
                scaleChanged = true;
            }
        }
        while (SCALE_DECREASE_KEY.wasPressed()) {
            if (on) {
                decreaseScale();
                scaleChanged = true;
            }
        }
        if (scaleChanged) {
            sendScaleMessage();
        }
        boolean fixPressed = false;
        while (FIX_CAMERA_KEY.wasPressed()) {
            fixPressed = true;
            CONFIG.setFixed(!CONFIG.fixed);
        }
        if (fixPressed) {
            client.getMessageHandler().onGameMessage(CONFIG.fixed ? FIXED_TEXT : UNFIXED_TEXT, true);
        }
        if (FIXED_CAMERA_ROTATE_LEFT_KEY.isPressed()) {
            CONFIG.setFixedYaw(CONFIG.fixed_yaw + CONFIG.fixed_rotate_speed_y);
        }
        if (FIXED_CAMERA_ROTATE_RIGHT_KEY.isPressed()) {
            CONFIG.setFixedYaw(CONFIG.fixed_yaw - CONFIG.fixed_rotate_speed_y);
        }
        if (FIXED_CAMERA_ROTATE_UP_KEY.isPressed()) {
            CONFIG.setFixedPitch(CONFIG.fixed_pitch + CONFIG.fixed_rotate_speed_x);
        }
        if (FIXED_CAMERA_ROTATE_DOWN_KEY.isPressed()) {
            CONFIG.setFixedPitch(CONFIG.fixed_pitch - CONFIG.fixed_rotate_speed_x);
        }
        boolean openScreen = false;
        while (OPEN_OPTIONS_KEY.wasPressed()) {
            openScreen = true;
        }
        if (openScreen) {
            client.setScreen(new ModConfigScreen(null));
        }

        if (SELECT_ENTITY_AUTO_KEY.wasPressed()) {
            CONFIG.auto_select_entity = !CONFIG.auto_select_entity;
            client.getMessageHandler()
                    .onGameMessage(CONFIG.auto_select_entity ? AUTO_SELECT_ENABLED_TEXT : AUTO_SELECT_DISABLED_TEXT, true);
        }

        if (EntitySelector.instance != null) {
            boolean selected = false;
            if (SELECT_NEAREST_ENTITY_KEY.wasPressed()) {
                EntitySelector.instance.selectNearest();
                selected = true;
            }

            if (SELECT_NEXT_ENTITY_KEY.wasPressed()) {
                EntitySelector.instance.selectNext();
                selected = true;
            }

            if (selected && EntitySelector.instance.selectedEntity != null) {
                client.getMessageHandler()
                        .onGameMessage(Text.translatable("orthocamera.selecting_entity", EntitySelector.instance.selectedEntity.getDisplayName()), true);
            }

            if (SELECT_ENTITY_STOP_KEY.wasPressed()) {
                EntitySelector.instance.selectedEntity = null;
                client.getMessageHandler().onGameMessage(SELECT_STOPPED, true);
            }

            EntitySelector.instance.tick();
        }
    }

    private void onClientStopping(MinecraftClient client) {
        if (CONFIG.isDirty()) {
            CONFIG.save();
        }
    }
}
