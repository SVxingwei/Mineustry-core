package com.example.coremod.storage;

import com.example.coremod.blocks.CoreBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CoreStorageManager extends SavedData {
    
    private static final String DATA_NAME = "core_storage";
    private static CoreStorageManager instance;
    
    private final Map<String, CoreStorage> storages = new ConcurrentHashMap<>();
    private final Map<String, Set<CoreBlockEntity>> coresByStorage = new ConcurrentHashMap<>();
    
    public static void initialize() {
        // 将在服务器启动时调用
    }
    
    public static CoreStorageManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CoreStorageManager not initialized");
        }
        return instance;
    }
    
    public static CoreStorageManager getOrCreate(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        instance = storage.computeIfAbsent(CoreStorageManager::load, CoreStorageManager::new, DATA_NAME);
        return instance;
    }
    
    public CoreStorageManager() {
        super();
    }
    
    public static CoreStorageManager load(CompoundTag tag) {
        CoreStorageManager manager = new CoreStorageManager();
        ListTag storageList = tag.getList("storages", Tag.TAG_COMPOUND);
        
        for (int i = 0; i < storageList.size(); i++) {
            CompoundTag storageTag = storageList.getCompound(i);
            String id = storageTag.getString("id");
            CoreStorage storage = new CoreStorage();
            storage.deserializeNBT(storageTag.getCompound("data"));
            manager.storages.put(id, storage);
        }
        
        return manager;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag storageList = new ListTag();
        
        for (Map.Entry<String, CoreStorage> entry : storages.entrySet()) {
            CompoundTag storageTag = new CompoundTag();
            storageTag.putString("id", entry.getKey());
            storageTag.put("data", entry.getValue().serializeNBT());
            storageList.add(storageTag);
        }
        
        tag.put("storages", storageList);
        return tag;
    }
    
    public void updateCore(CoreBlockEntity core) {
        String coreId = core.getCoreId();
        if (coreId.isEmpty()) {
            coreId = generateCoreId();
            core.setCoreId(coreId);
        }
        
        coresByStorage.computeIfAbsent(coreId, k -> ConcurrentHashMap.newKeySet()).add(core);
        storages.computeIfAbsent(coreId, k -> new CoreStorage());
        
        setDirty();
    }
    
    public void removeCore(CoreBlockEntity core) {
        String coreId = core.getCoreId();
        if (!coreId.isEmpty()) {
            Set<CoreBlockEntity> cores = coresByStorage.get(coreId);
            if (cores != null) {
                cores.remove(core);
                if (cores.isEmpty()) {
                    coresByStorage.remove(coreId);
                    storages.remove(coreId);
                }
            }
        }
        setDirty();
    }
    
    public CoreStorage getStorage(String coreId) {
        return storages.get(coreId);
    }
    
    public int getCoreCount(String coreId) {
        Set<CoreBlockEntity> cores = coresByStorage.get(coreId);
        return cores != null ? cores.size() : 0;
    }
    
    public int getMaxItemsPerType(String coreId) {
        return getCoreCount(coreId) * 8000;
    }
    
    private String generateCoreId() {
        return UUID.randomUUID().toString();
    }
    
    public static class CoreStorage {
        private final Map<ItemStack, Integer> items = new ConcurrentHashMap<>();
        
        public boolean addItem(ItemStack stack) {
            if (stack.isEmpty()) return false;
            
            ItemStack key = stack.copy();
            key.setCount(1);
            
            int currentCount = items.getOrDefault(key, 0);
            items.put(key, currentCount + stack.getCount());
            
            return true;
        }
        
        public ItemStack removeItem(ItemStack stack, int amount) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            
            ItemStack key = stack.copy();
            key.setCount(1);
            
            int currentCount = items.getOrDefault(key, 0);
            if (currentCount <= 0) return ItemStack.EMPTY;
            
            int toRemove = Math.min(amount, currentCount);
            items.put(key, currentCount - toRemove);
            
            if (items.get(key) <= 0) {
                items.remove(key);
            }
            
            ItemStack result = stack.copy();
            result.setCount(toRemove);
            return result;
        }
        
        public int getItemCount(ItemStack stack) {
            if (stack.isEmpty()) return 0;
            
            ItemStack key = stack.copy();
            key.setCount(1);
            
            return items.getOrDefault(key, 0);
        }
        
        public Map<ItemStack, Integer> getAllItems() {
            return new HashMap<>(items);
        }
        
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            ListTag itemList = new ListTag();
            
            for (Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
                CompoundTag itemTag = new CompoundTag();
                entry.getKey().save(itemTag);
                itemTag.putInt("Count", entry.getValue());
                itemList.add(itemTag);
            }
            
            tag.put("items", itemList);
            return tag;
        }
        
        public void deserializeNBT(CompoundTag tag) {
            items.clear();
            ListTag itemList = tag.getList("items", Tag.TAG_COMPOUND);
            
            for (int i = 0; i < itemList.size(); i++) {
                CompoundTag itemTag = itemList.getCompound(i);
                ItemStack stack = ItemStack.of(itemTag);
                int count = itemTag.getInt("Count");
                if (!stack.isEmpty()) {
                    items.put(stack, count);
                }
            }
        }
    }
}