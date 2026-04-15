package com.zurrtum.create.mixin;

import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.config.CKinetics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobCanAttackMixin {

    @Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
    private void onCanAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof DeployerPlayer) {
            CKinetics.DeployerAggroSetting setting = AllConfigs.server().kinetics.ignoreDeployerAttacks.get();
            switch (setting) {
                case ALL:
                    cir.setReturnValue(false);
                    return;
                case CREEPERS:
                    if ((Object) this instanceof Creeper) {
                        cir.setReturnValue(false);
                        return;
                    }
                    break;
            }
        }
    }
}