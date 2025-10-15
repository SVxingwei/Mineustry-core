package com.coremod.blockentity;

import com.coremod.init.ModBlockEntities;
import com.coremod.menu.CoreMenu;
import com.coremod.storage.CoreConfig;
import com.coremod.storage.CoreStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 核心方块实体 - 处理存储、能量生成和GUI
 */
public class CoreBlockEntity extends BlockEntity implements MenuProvider {
    private final CoreConfig config = new CoreConfig();
    private boolean isRegistered = false;
    
    // 能量存储（用于与其他模组兼容）
    private final CoreEnergyStorage energyStorage = new CoreEnergyStorage();
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energyStorage);

    public CoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CORE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.coremod.core");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        // 检查玩家是否有权限访问
        if (!canPlayerAccess(player)) {
            return null;
        }
        return new CoreMenu(containerId, playerInv, this, this.worldPosition);
    }

    public boolean canPlayerAccess(Player player) {
        if (config.isPublic()) {
            return config.isAllowOthers() || config.getOwnerId().isEmpty() || 
                   config.getOwnerId().equals(player.getStringUUID());
        } else {
            return config.getOwnerId().equals(player.getStringUUID());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoreBlockEntity blockEntity) {
        // 每tick生成100 FE的能量
        blockEntity.energyStorage.generateEnergy(100);
        
        // 确保已注册到全局存储
        if (!blockEntity.isRegistered && level instanceof ServerLevel serverLevel) {
            CoreStorage storage = CoreStorage.get(serverLevel);
            storage.registerCore(blockEntity.config.getActualStorageId());
            blockEntity.isRegistered = true;
            blockEntity.setChanged();
        }
    }

    public void onBlockRemoved() {
        if (level instanceof ServerLevel serverLevel && isRegistered) {
            CoreStorage storage = CoreStorage.get(serverLevel);
            storage.unregisterCore(config.getActualStorageId(), config.isBurnOverflow());
            isRegistered = false;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        config.load(tag.getCompound("Config"));
        isRegistered = tag.getBoolean("IsRegistered");
        energyStorage.deserializeNBT(tag.getCompound("Energy"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag configTag = new CompoundTag();
        config.save(configTag);
        tag.put("Config", configTag);
        tag.putBoolean("IsRegistered", isRegistered);
        tag.put("Energy", energyStorage.serializeNBT());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }

    public CoreConfig getConfig() {
        return config;
    }

    public CoreStorage getStorage() {
        if (level instanceof ServerLevel serverLevel) {
            return CoreStorage.get(serverLevel);
        }
        return null;
    }

    /**
     * 内部能量存储类
     */
    private static class CoreEnergyStorage implements IEnergyStorage {
        private int energy = 0;
        private static final int MAX_ENERGY = 1000000; // 1M FE缓存

        public void generateEnergy(int amount) {
            energy = Math.min(energy + amount, MAX_ENERGY);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0; // 核心只生成能量，不接收
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = Math.min(energy, maxExtract);
            if (!simulate) {
                energy -= extracted;
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_ENERGY;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Energy", energy);
            return tag;
        }

        public void deserializeNBT(CompoundTag tag) {
            energy = tag.getInt("Energy");
        }
    }
}
