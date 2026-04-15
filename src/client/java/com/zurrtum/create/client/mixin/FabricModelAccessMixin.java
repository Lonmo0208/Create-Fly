package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.caffeinemc.mods.sodium.client.render.helper.ListStorage;
import net.caffeinemc.mods.sodium.fabric.model.FabricModelAccess;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(FabricModelAccess.class)
public class FabricModelAccessMixin {

    @Redirect(
            method = "collectPartsOf",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;collectParts(Lnet/minecraft/util/RandomSource;Ljava/util/List;)V"
            )
    )
    private void redirectCollectParts(
            BlockStateModel model,
            RandomSource random,
            List<BlockStateModelPart> parts,
            // 以下参数是 collectPartsOf 方法的参数（通过 Redirect 捕获）
            BlockStateModel blockStateModel,      // 原第一个参数，与 model 相同
            BlockAndTintGetter world,
            BlockPos pos,
            BlockState state,
            RandomSource random2,                 // 与 random 相同，可忽略
            ListStorage emitter
    ) {
        // 检查是否被 WrapperBlockStateModel 包装
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
        } else {
            // 否则调用原方法
            model.collectParts(random, parts);
        }
    }
}