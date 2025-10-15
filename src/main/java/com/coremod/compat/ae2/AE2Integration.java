package com.coremod.compat.ae2;

import com.coremod.CoreMod;
import com.coremod.blockentity.CoreBlockEntity;
import net.minecraftforge.fml.ModList;

/**
 * AE2集成 - 应用能源2兼容
 * 
 * 注意：此类需要AE2的API依赖才能完全实现
 * 当前提供基础框架，实际实现需要添加AE2 API到build.gradle
 */
public class AE2Integration {
    private static boolean isAE2Loaded = false;

    public static void init() {
        isAE2Loaded = ModList.get().isLoaded("ae2");
        if (isAE2Loaded) {
            CoreMod.LOGGER.info("应用能源2已加载，启用AE2集成");
            // TODO: 注册AE2网络节点和存储提供者
            // 需要实现：
            // 1. IGridNode - 将核心连接到AE2网络
            // 2. IStorageProvider - 提供存储给AE2网络
            // 3. ICraftingProvider - 提供合成能力给AE2网络
        }
    }

    public static boolean isLoaded() {
        return isAE2Loaded;
    }

    /**
     * 将核心方块实体连接到AE2网络
     * 
     * @param blockEntity 核心方块实体
     */
    public static void connectToAE2Network(CoreBlockEntity blockEntity) {
        if (!isAE2Loaded) return;
        
        // TODO: 实现AE2网络连接
        // 需要：
        // 1. 创建IGridNode
        // 2. 注册IStorageProvider capability
        // 3. 将核心存储暴露给AE2网络
        CoreMod.LOGGER.info("正在将核心方块连接到AE2网络...");
    }

    /**
     * 断开核心方块实体与AE2网络的连接
     * 
     * @param blockEntity 核心方块实体
     */
    public static void disconnectFromAE2Network(CoreBlockEntity blockEntity) {
        if (!isAE2Loaded) return;
        
        // TODO: 实现AE2网络断开
        CoreMod.LOGGER.info("正在断开核心方块与AE2网络的连接...");
    }
}
