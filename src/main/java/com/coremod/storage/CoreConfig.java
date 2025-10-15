package com.coremod.storage;

import net.minecraft.nbt.CompoundTag;

/**
 * 核心配置数据
 */
public class CoreConfig {
    private String coreId;
    private boolean isPublic;
    private String ownerId; // UUID字符串
    private boolean allowOthers;
    private boolean burnOverflow;

    public CoreConfig() {
        this.coreId = "default";
        this.isPublic = true;
        this.ownerId = "";
        this.allowOthers = true;
        this.burnOverflow = true;
    }

    public void save(CompoundTag tag) {
        tag.putString("CoreId", coreId);
        tag.putBoolean("IsPublic", isPublic);
        tag.putString("OwnerId", ownerId);
        tag.putBoolean("AllowOthers", allowOthers);
        tag.putBoolean("BurnOverflow", burnOverflow);
    }

    public void load(CompoundTag tag) {
        this.coreId = tag.getString("CoreId");
        this.isPublic = tag.getBoolean("IsPublic");
        this.ownerId = tag.getString("OwnerId");
        this.allowOthers = tag.getBoolean("AllowOthers");
        this.burnOverflow = tag.getBoolean("BurnOverflow");
    }

    // Getters and Setters
    public String getCoreId() {
        return coreId;
    }

    public void setCoreId(String coreId) {
        this.coreId = coreId;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isAllowOthers() {
        return allowOthers;
    }

    public void setAllowOthers(boolean allowOthers) {
        this.allowOthers = allowOthers;
    }

    public boolean isBurnOverflow() {
        return burnOverflow;
    }

    public void setBurnOverflow(boolean burnOverflow) {
        this.burnOverflow = burnOverflow;
    }

    /**
     * 获取实际的存储ID（如果是私有的，则包含玩家ID）
     */
    public String getActualStorageId() {
        if (isPublic) {
            return coreId;
        } else {
            return coreId + "_" + ownerId;
        }
    }
}
