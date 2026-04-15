package com.zurrtum.create.content.kinetics.fan.processing;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.livingblock.LivingBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class FanProcessing {
    public static boolean canProcess(LivingBlock entity, FanProcessingType type) {
        String itemType = AllSynchedDatas.ITEM_TYPE.get(entity);
        if (!itemType.isEmpty()) {
            if (FanProcessingType.parse(itemType) != type) {
                return type.canProcess(entity.getItemStack(), entity.level());
            } else {
                return AllSynchedDatas.ITEM_TIME.get(entity) != -1;
            }
        }
        return type.canProcess(entity.getItemStack(), entity.level());
    }

    public static boolean applyProcessing(LivingBlock entity, FanProcessingType type) {
        if (decrementProcessingTime(entity, type) != 0) {
            return false;
        }
        List<ItemStack> stacks = type.process(entity.getItemStack(), entity.level());
        if (stacks == null) {
            return false;
        }
        if (stacks.isEmpty()) {
            entity.discard();
            return false;
        }
        entity.setItemStack(stacks.removeFirst());
        for (ItemStack additional : stacks) {
            BlockPos pos = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());
            LivingBlock entityIn = LivingBlock.createAt(entity.level(), pos, additional);
            if (entityIn != null) {
                entityIn.setDeltaMovement(entity.getDeltaMovement());
            }
        }
        return true;
    }

    public static TransportedResult applyProcessing(
        TransportedItemStack transported,
        Level world,
        FanProcessingType type
    ) {
        TransportedResult ignore = TransportedResult.doNothing();
        if (transported.processedBy != type) {
            transported.processedBy = type;
            int timeModifierForStackSize = ((transported.stack.getCount() - 1) / 16) + 1;
            transported.processingTime = (AllConfigs.server().kinetics.fanProcessingTime.get() * timeModifierForStackSize) + 1;
            if (!type.canProcess(transported.stack, world)) {
                transported.processingTime = -1;
            }
            return ignore;
        }
        if (transported.processingTime == -1) {
            return ignore;
        }
        if (transported.processingTime-- > 0) {
            return ignore;
        }

        List<ItemStack> stacks = type.process(transported.stack, world);
        if (stacks == null) {
            return ignore;
        }

        List<TransportedItemStack> transportedStacks = new ArrayList<>();
        for (ItemStack additional : stacks) {
            TransportedItemStack newTransported = transported.getSimilar();
            newTransported.stack = additional.copy();
            transportedStacks.add(newTransported);
        }
        return TransportedResult.convertTo(transportedStacks);
    }

    private static int decrementProcessingTime(LivingBlock entity, FanProcessingType type) {
        String itemType = AllSynchedDatas.ITEM_TYPE.get(entity);
        int time;
        if (itemType.isEmpty() || FanProcessingType.parse(itemType) != type) {
            Identifier key = CreateRegistries.FAN_PROCESSING_TYPE.getKey(type);
            if (key == null) {
                throw new IllegalArgumentException("Could not get id for FanProcessingType " + type + "!");
            }
            AllSynchedDatas.ITEM_TYPE.set(entity, key.toString());
            int timeModifierForStackSize = ((entity.getItemStack().getCount() - 1) / 16) + 1;
            time = (AllConfigs.server().kinetics.fanProcessingTime.get() * timeModifierForStackSize);
        } else {
            time = AllSynchedDatas.ITEM_TIME.get(entity) - 1;
        }

        AllSynchedDatas.ITEM_TIME.set(entity, time);
        return time;
    }
}