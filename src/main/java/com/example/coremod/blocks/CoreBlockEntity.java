package com.example.coremod.blocks;

import com.example.coremod.storage.CoreStorageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoreBlockEntity extends BlockEntity {
    
    private final EnergyStorage energyStorage = new EnergyStorage(1000000, 100, 100); // 1M RF capacity, 100 RF/t input/output
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energyStorage);
    
    private String coreId = "";
    private boolean isPublic = false;
    private boolean allowOthers = true;
    private boolean burnOverflow = false;
    
    public CoreBlockEntity(BlockPos pos, BlockState state) {
        super(CoreMod.CORE_BLOCK_ENTITY.get(), pos, state);
    }
    
    public void tick() {
        if (level != null && !level.isClientSide) {
            // 每tick产生100 RF能量
            energyStorage.receiveEnergy(100, false);
            
            // 更新存储管理器
            if (!coreId.isEmpty()) {
                CoreStorageManager.getInstance().updateCore(this);
            }
        }
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energyStorage.deserializeNBT(tag.getCompound("Energy"));
        coreId = tag.getString("CoreId");
        isPublic = tag.getBoolean("IsPublic");
        allowOthers = tag.getBoolean("AllowOthers");
        burnOverflow = tag.getBoolean("BurnOverflow");
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Energy", energyStorage.serializeNBT());
        tag.putString("CoreId", coreId);
        tag.putBoolean("IsPublic", isPublic);
        tag.putBoolean("AllowOthers", allowOthers);
        tag.putBoolean("BurnOverflow", burnOverflow);
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        return super.getCapability(cap, side);
    }
    
    // Getters and Setters
    public String getCoreId() { return coreId; }
    public void setCoreId(String coreId) { this.coreId = coreId; }
    
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    
    public boolean allowOthers() { return allowOthers; }
    public void setAllowOthers(boolean allowOthers) { this.allowOthers = allowOthers; }
    
    public boolean burnOverflow() { return burnOverflow; }
    public void setBurnOverflow(boolean burnOverflow) { this.burnOverflow = burnOverflow; }
    
    public IEnergyStorage getEnergyStorage() { return energyStorage; }
}