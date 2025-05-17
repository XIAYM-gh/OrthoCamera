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
    private List<LivingEntity> entityList;
    private int index = 0;

    public void tick() {
        if (selectedEntity == null || OrthoCamera.CLIENT.player == null) {
            return;
        }

        if (!OrthoCamera.isEnabled() || selectedEntity.isDead() || selectedEntity.isRemoved() || selectedEntity.getPos()
                .distanceTo(OrthoCamera.CLIENT.player.getPos()) > OrthoCamera.CONFIG.max_select_distance) {
            selectedEntity = null;
            return;
        }

        if (OrthoCamera.isEnabled() && OrthoCamera.CONFIG.fixed) {
            lookAt();
        }
    }

    public void updateEntityList() {
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
            if (dist > 100) {
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
        if (entityList == null || entityList.isEmpty()) {
            updateEntityList();
        }

        selectedEntity = entityList.getFirst();
    }

    public void selectNext() {
        if (entityList == null) {
            updateEntityList();
        }

        if (entityList.size() < index + 1) {
            selectNearest();
            return;
        }

        selectedEntity = entityList.get(index++);
    }

    public void lookAt() {
        assert OrthoCamera.CLIENT.player != null && selectedEntity != null;
        ClientPlayerEntity playerEntity = OrthoCamera.CLIENT.player;
        Vec3d entityPos = selectedEntity.getPos();

        playerEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, entityPos);
    }
}
