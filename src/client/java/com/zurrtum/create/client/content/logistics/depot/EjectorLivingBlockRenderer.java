package com.zurrtum.create.client.content.logistics.depot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.depot.EjectorLivingBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingBlockRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.livingblock.LivingBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EjectorLivingBlockRenderer extends LivingBlockRenderer {
    private final RandomSource random;
    
    public EjectorLivingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.random = RandomSource.create();
    }

    @Override
    public LivingBlockRenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(LivingBlock itemEntity, LivingBlockRenderState itemEntityRenderState, float f) {
        super.extractRenderState(itemEntity, itemEntityRenderState, f);
        EjectorLivingBlock entity = (EjectorLivingBlock) itemEntity;
        RenderState state = (RenderState) itemEntityRenderState;
        state.alive = entity.isAlive();
        if (state.alive) {
            if (entity.data.initAge == -1) {
                itemEntityRenderState.ageInTicks = 0;
            } else {
                itemEntityRenderState.ageInTicks = (entity.age - entity.data.initAge + f) / 10.0F;
            }
        } else {
            state.isPackage = PackageItem.isPackage(entity.getItemStack());
            float time = entity.progress + f;
            if (state.isPackage) {
                state.rotateY = Mth.DEG_TO_RAD * time * 20;
            } else {
                state.rotateY = entity.data.rotateY;
                state.rotateX = Mth.DEG_TO_RAD * time * 40;
            }
            state.location = entity.getLaunchedItemLocation(time).subtract(entity.position());
        }
        state.bobOffset = entity.data.animateOffset;
    }

    public AABB getBoundingBoxForCulling(LivingBlock itemEntity) {
        EjectorLivingBlock entity = (EjectorLivingBlock) itemEntity;
        if (entity.isAlive()) {
            return entity.getBoundingBox();
        } else {
            return entity.data.renderBox;
        }
    }

    @Override
    public void submit(
        LivingBlockRenderState itemEntityRenderState,
        PoseStack matrixStack,
        SubmitNodeCollector queue,
        CameraRenderState cameraRenderState
    ) {
        if (!itemEntityRenderState.item.isEmpty()) {
            RenderState state = (RenderState) itemEntityRenderState;
            AABB box = state.item.getModelBoundingBox();
            matrixStack.pushPose();
            float f = -((float) box.minY) + 0.0625F;
            matrixStack.translate(0, state.bobOffset + f, -0.0625f);
            if (!state.alive) {
                matrixStack.translate(state.location);
                matrixStack.translate(0, 0.25f, 0);
                if (state.isPackage) {
                    matrixStack.translate(0, 0.25f, 0);
                    matrixStack.scale(3f, 3f, 3f);
                }
                if (state.rotateY != 0) {
                    matrixStack.mulPose(Axis.YP.rotation(state.rotateY));
                }
                if (state.rotateX != 0) {
                    matrixStack.mulPose(Axis.XP.rotation(state.rotateX));
                }
                matrixStack.translate(0, -0.25f, 0);
            } else if (state.ageInTicks > 0) {
                float g = Mth.sin(state.ageInTicks) * 0.1F + 0.1F;
                matrixStack.translate(0, g, 0);
                matrixStack.mulPose(Axis.YP.rotation(state.ageInTicks / 2F));
            }
            state.item.submit(matrixStack, queue, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, state.outlineColor);
            matrixStack.popPose();

            if (state.alive) {
                if (state.leashStates != null) {
                    for (EntityRenderState.LeashState leashData : state.leashStates) {
                        queue.submitLeash(matrixStack, leashData);
                    }
                }

                submitNameDisplay(state, matrixStack, queue, cameraRenderState);
            }
        }
    }

    public static class RenderState extends LivingBlockRenderState {
        public boolean alive;
        public float rotateY;
        public float rotateX;
        public Vec3 location;
        public boolean isPackage;
        public float bobOffset;
    }
}
