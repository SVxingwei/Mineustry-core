package com.coremod.blockentity;

import com.coremod.storage.CoreStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 核心方块的物品处理器
 * 用于支持漏斗、管道等自动物品插入
 */
public class CoreItemHandler implements IItemHandler {
    private final CoreBlockEntity blockEntity;
    private final String coreId;

    public CoreItemHandler(CoreBlockEntity blockEntity, String coreId) {
        this.blockEntity = blockEntity;
        this.coreId = coreId;
    }

    @Override
    public int getSlots() {
        // 返回一个很大的数字，表示"近乎无限"的槽位
        if (blockEntity.getLevel() instanceof ServerLevel serverLevel) {
            CoreStorage storage = CoreStorage.get(serverLevel);
            int maxCapacity = storage.getMaxCapacity(coreId);
            int currentUsage = storage.getCurrentUsage(coreId);
            return Math.max(1, (maxCapacity - currentUsage) / 64 + 1);
        }
        return 1000; // 客户端默认值
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        // 核心存储不需要按槽位索引，返回空
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return stack; // 只在服务器端处理
        }

        CoreStorage storage = CoreStorage.get(serverLevel);
        return storage.insertItem(coreId, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        // 自动提取功能暂不支持
        // 如果需要支持，需要知道要提取哪种物品
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        // 每个槽位理论上可以存储很多物品
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        // 接受所有物品
        return true;
    }
}
