package cn.xiaym.dirtystuff;

import com.dimaskama.orthocamera.client.OrthoCamera;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class EntitySelector {
    public static EntitySelector instance;
    public LivingEntity selectedEntity;
    public List<LivingEntity> entityList;
    private int index = 0;

    public static boolean featureAvailable() {
        return OrthoCamera.isEnabled() && OrthoCamera.CONFIG.fixed;
    }

    public void tick() {
        if (selectedEntity == null || OrthoCamera.CLIENT.player == null) {
            return;
        }

        if (!OrthoCamera.isEnabled() || !selectedEntity.isAlive() || selectedEntity.getPos()
                .distanceTo(OrthoCamera.CLIENT.player.getPos()) > OrthoCamera.CONFIG.max_select_distance) {
            selectedEntity = null;
            return;
        }

        if (featureAvailable()) {
            lookAt();
        }
    }

    public void updateEntityList() {
        if (!featureAvailable()) {
            return;
        }

        assert OrthoCamera.CLIENT.player != null;
        assert OrthoCamera.CLIENT.world != null;

        ClientPlayerEntity playerEntity = OrthoCamera.CLIENT.player;
        Vec3d playerPos = playerEntity.getPos();

        HashMap<LivingEntity, Double> distMap = new HashMap<>();
        ArrayList<LivingEntity> entities = new ArrayList<>();
        for (Entity entity : OrthoCamera.CLIENT.world.getEntities()) {
            if (entity == playerEntity) {
                continue;
            }

            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            double dist = livingEntity.getPos().distanceTo(playerPos);
            if (dist > OrthoCamera.CONFIG.max_select_distance) {
                continue;
            }

            entities.add(livingEntity);
            distMap.put(livingEntity, dist);
        }

        entities.sort(Comparator.comparingDouble(distMap::get));
        entityList = entities;
        index = 0;
    }

    public void selectNearest() {
        if (!featureAvailable()) {
            return;
        }

        updateEntityList();
        selectedEntity = entityList.isEmpty() ? null : entityList.getFirst();
    }

    public void selectNext() {
        if (!featureAvailable()) {
            return;
        }

        if (entityList == null || entityList.size() < index + 1) {
            selectNearest();
            return;
        }

        assert OrthoCamera.CLIENT.player != null;
        Vec3d playerPos = OrthoCamera.CLIENT.player.getPos();
        for (int i = ++index, size = entityList.size(); i < size; i++, index++) {
            LivingEntity entity = entityList.get(i);
            if (entity.isAlive() && entity.getPos().distanceTo(playerPos) <= OrthoCamera.CONFIG.max_select_distance) {
                selectedEntity = entity;
                return;
            }
        }

        selectNearest();
    }

    public void lookAt() {
        assert OrthoCamera.CLIENT.player != null && selectedEntity != null;
        ClientPlayerEntity playerEntity = OrthoCamera.CLIENT.player;
        Vec3d entityPos = selectedEntity.getPos();

        playerEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, entityPos);
    }
}
