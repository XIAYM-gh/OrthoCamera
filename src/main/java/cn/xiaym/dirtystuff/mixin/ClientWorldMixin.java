package cn.xiaym.dirtystuff.mixin;

import cn.xiaym.dirtystuff.EntitySelector;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Unique
    private static long lastUpdateTime = -1;

    @Unique
    private static void update() {
        if (System.currentTimeMillis() - lastUpdateTime <= 1) {
            return;
        }

        lastUpdateTime = System.currentTimeMillis();
        EntitySelector.instance.updateEntityList();
    }

    @Shadow
    @Nullable
    public abstract Entity getEntityById(int id);

    @Inject(method = "addEntity", at = @At("RETURN"))
    private void addEntity(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        update();
    }

    @Inject(method = "removeEntity", at = @At("RETURN"))
    private void removeEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
        if (!(getEntityById(entityId) instanceof LivingEntity)) {
            return;
        }

        update();
    }
}
