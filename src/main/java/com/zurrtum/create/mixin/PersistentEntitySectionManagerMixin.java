package com.zurrtum.create.mixin;

import com.zurrtum.create.content.contraptions.ContraptionHandler;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.logistics.depot.EjectorLivingBlock;
import com.zurrtum.create.foundation.item.EntityItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.livingblock.LivingBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentEntitySectionManager.class)
public class PersistentEntitySectionManagerMixin {
    @ModifyVariable(method = "addEntity(Lnet/minecraft/world/level/entity/EntityAccess;Z)Z", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private EntityAccess addEntity(EntityAccess entity) {
        if (entity instanceof LivingBlock itemEntity) {
            if (entity instanceof EjectorLivingBlock) {
                return entity;
            }
            ItemStack stack = itemEntity.getItemStack();
            if (stack.getItem() instanceof EntityItem item) {
                Entity newEntity = item.createEntity(itemEntity.level(), itemEntity, stack);
                if (newEntity != null) {
                    itemEntity.discard();
                    return newEntity;
                }
            }
        }
        return entity;
    }

    @Inject(method = "addEntity(Lnet/minecraft/world/level/entity/EntityAccess;Z)Z", at = @At("HEAD"))
    private void addEntity(EntityAccess entity, boolean existing, CallbackInfoReturnable<Boolean> cir) {
        ContraptionHandler.addSpawnedContraptionsToCollisionList(entity);
        if (!existing) {
            CapabilityMinecartController.attach(entity);
        }
    }
}
