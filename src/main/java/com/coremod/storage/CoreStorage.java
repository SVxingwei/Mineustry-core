package com.coremod.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * 核心存储系统 - 保存在世界数据中，所有相同ID的核心共享存储
 */
public class CoreStorage extends SavedData {
    private static final String DATA_NAME = "coremod_storage";
    
    // 存储结构: coreId -> (itemKey -> ItemStack)
    private final Map<String, Map<String, ItemStack>> storageMap = new HashMap<>();
    
    // 追踪每个核心ID有多少个实例（用于计算容量）
    private final Map<String, Integer> coreInstanceCount = new HashMap<>();
    
    // 每个核心实例提供8000个物品槽位
    private static final int CAPACITY_PER_CORE = 8000;

    public CoreStorage() {
    }

    /**
     * 获取或创建核心存储数据
     */
    public static CoreStorage get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                CoreStorage::load,
                CoreStorage::new,
                DATA_NAME
        );
    }

    /**
     * 从NBT加载数据
     */
    public static CoreStorage load(CompoundTag tag) {
        CoreStorage storage = new CoreStorage();
        
        // 加载存储映射
        CompoundTag storageTag = tag.getCompound("Storage");
        for (String coreId : storageTag.getAllKeys()) {
            Map<String, ItemStack> items = new HashMap<>();
            CompoundTag coreTag = storageTag.getCompound(coreId);
            
            ListTag itemsList = coreTag.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < itemsList.size(); i++) {
                CompoundTag itemTag = itemsList.getCompound(i);
                String key = itemTag.getString("Key");
                ItemStack stack = ItemStack.of(itemTag.getCompound("Stack"));
                items.put(key, stack);
            }
            
            storage.storageMap.put(coreId, items);
        }
        
        // 加载实例计数
        CompoundTag countTag = tag.getCompound("InstanceCount");
        for (String coreId : countTag.getAllKeys()) {
            storage.coreInstanceCount.put(coreId, countTag.getInt(coreId));
        }
        
        return storage;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // 保存存储映射
        CompoundTag storageTag = new CompoundTag();
        for (Map.Entry<String, Map<String, ItemStack>> entry : storageMap.entrySet()) {
            CompoundTag coreTag = new CompoundTag();
            ListTag itemsList = new ListTag();
            
            for (Map.Entry<String, ItemStack> itemEntry : entry.getValue().entrySet()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putString("Key", itemEntry.getKey());
                itemTag.put("Stack", itemEntry.getValue().save(new CompoundTag()));
                itemsList.add(itemTag);
            }
            
            coreTag.put("Items", itemsList);
            storageTag.put(entry.getKey(), coreTag);
        }
        tag.put("Storage", storageTag);
        
        // 保存实例计数
        CompoundTag countTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : coreInstanceCount.entrySet()) {
            countTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("InstanceCount", countTag);
        
        return tag;
    }

    /**
     * 注册核心实例
     */
    public void registerCore(String coreId) {
        coreInstanceCount.put(coreId, coreInstanceCount.getOrDefault(coreId, 0) + 1);
        storageMap.putIfAbsent(coreId, new HashMap<>());
        setDirty();
    }

    /**
     * 注销核心实例
     */
    public void unregisterCore(String coreId, boolean burnOverflow) {
        int count = coreInstanceCount.getOrDefault(coreId, 0);
        if (count > 0) {
            count--;
            if (count == 0) {
                coreInstanceCount.remove(coreId);
                if (burnOverflow) {
                    storageMap.remove(coreId);
                }
            } else {
                coreInstanceCount.put(coreId, count);
                if (burnOverflow) {
                    // 移除超出容量的物品
                    trimStorage(coreId);
                }
            }
            setDirty();
        }
    }

    /**
     * 获取核心的最大容量
     */
    public int getMaxCapacity(String coreId) {
        return coreInstanceCount.getOrDefault(coreId, 0) * CAPACITY_PER_CORE;
    }

    /**
     * 获取核心的当前使用量
     */
    public int getCurrentUsage(String coreId) {
        Map<String, ItemStack> items = storageMap.get(coreId);
        if (items == null) return 0;
        
        int total = 0;
        for (ItemStack stack : items.values()) {
            total += stack.getCount();
        }
        return total;
    }

    /**
     * 插入物品到核心
     */
    public ItemStack insertItem(String coreId, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        
        Map<String, ItemStack> items = storageMap.get(coreId);
        if (items == null) return stack;
        
        int maxCapacity = getMaxCapacity(coreId);
        int currentUsage = getCurrentUsage(coreId);
        int availableSpace = maxCapacity - currentUsage;
        
        if (availableSpace <= 0) return stack;
        
        String itemKey = getItemKey(stack);
        ItemStack existing = items.get(itemKey);
        
        int toInsert = Math.min(stack.getCount(), availableSpace);
        
        if (!simulate) {
            if (existing == null) {
                ItemStack newStack = stack.copy();
                newStack.setCount(toInsert);
                items.put(itemKey, newStack);
            } else {
                existing.grow(toInsert);
            }
            setDirty();
        }
        
        if (toInsert < stack.getCount()) {
            ItemStack remainder = stack.copy();
            remainder.setCount(stack.getCount() - toInsert);
            return remainder;
        }
        
        return ItemStack.EMPTY;
    }

    /**
     * 从核心提取物品
     */
    public ItemStack extractItem(String coreId, String itemKey, int amount, boolean simulate) {
        Map<String, ItemStack> items = storageMap.get(coreId);
        if (items == null) return ItemStack.EMPTY;
        
        ItemStack existing = items.get(itemKey);
        if (existing == null || existing.isEmpty()) return ItemStack.EMPTY;
        
        int toExtract = Math.min(amount, existing.getCount());
        ItemStack extracted = existing.copy();
        extracted.setCount(toExtract);
        
        if (!simulate) {
            existing.shrink(toExtract);
            if (existing.isEmpty()) {
                items.remove(itemKey);
            }
            setDirty();
        }
        
        return extracted;
    }

    /**
     * 获取核心中的所有物品
     */
    public Map<String, ItemStack> getItems(String coreId) {
        return new HashMap<>(storageMap.getOrDefault(coreId, new HashMap<>()));
    }

    /**
     * 修剪存储，移除超出容量的物品
     */
    private void trimStorage(String coreId) {
        Map<String, ItemStack> items = storageMap.get(coreId);
        if (items == null) return;
        
        int maxCapacity = getMaxCapacity(coreId);
        int currentUsage = getCurrentUsage(coreId);
        
        if (currentUsage > maxCapacity) {
            int toRemove = currentUsage - maxCapacity;
            Iterator<Map.Entry<String, ItemStack>> iterator = items.entrySet().iterator();
            
            while (iterator.hasNext() && toRemove > 0) {
                Map.Entry<String, ItemStack> entry = iterator.next();
                ItemStack stack = entry.getValue();
                
                if (stack.getCount() <= toRemove) {
                    toRemove -= stack.getCount();
                    iterator.remove();
                } else {
                    stack.shrink(toRemove);
                    toRemove = 0;
                }
            }
            setDirty();
        }
    }

    /**
     * 生成物品的唯一键
     */
    private String getItemKey(ItemStack stack) {
        CompoundTag tag = stack.save(new CompoundTag());
        tag.remove("Count");
        return tag.toString();
    }
}
