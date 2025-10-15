package com.example.coremod.gui;

import com.example.coremod.blocks.CoreBlockEntity;
import com.example.coremod.storage.CoreStorageManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class CoreMenu extends AbstractContainerMenu {
    
    private final CoreBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;
    private final IItemHandler playerInventory;
    private final IItemHandler coreInventory;
    
    // 合成栏槽位
    private static final int CRAFTING_SLOTS = 9;
    private static final int CRAFTING_RESULT_SLOT = 9;
    
    // 玩家背包槽位
    private static final int PLAYER_INVENTORY_SLOTS = 27;
    private static final int PLAYER_HOTBAR_SLOTS = 9;
    
    public CoreMenu(int containerId, Inventory playerInventory, CoreBlockEntity blockEntity) {
        super(CoreMod.CORE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.playerInventory = new InvWrapper(playerInventory);
        this.coreInventory = new ItemStackHandler(1); // 临时，实际使用存储管理器
        
        // 添加合成栏槽位 (3x3)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new SlotItemHandler(coreInventory, row * 3 + col, 8 + col * 18, 18 + row * 18));
            }
        }
        
        // 合成结果槽位
        addSlot(new SlotItemHandler(coreInventory, CRAFTING_RESULT_SLOT, 123, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        
        // 玩家背包槽位
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new SlotItemHandler(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        
        // 玩家快捷栏
        for (int col = 0; col < 9; col++) {
            addSlot(new SlotItemHandler(playerInventory, col, 8 + col * 18, 142));
        }
    }
    
    public CoreMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }
    
    private static CoreBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof CoreBlockEntity coreEntity) {
            return coreEntity;
        }
        throw new IllegalStateException("Block entity is not a CoreBlockEntity");
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            if (index < CRAFTING_SLOTS) {
                // 从合成栏移动到玩家背包
                if (!this.moveItemStackTo(itemstack1, CRAFTING_SLOTS + 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == CRAFTING_RESULT_SLOT) {
                // 合成结果槽位
                if (!this.moveItemStackTo(itemstack1, CRAFTING_SLOTS + 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else {
                // 从玩家背包移动到合成栏或存储
                if (index < CRAFTING_SLOTS + 1 + PLAYER_INVENTORY_SLOTS) {
                    if (!this.moveItemStackTo(itemstack1, 0, CRAFTING_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(itemstack1, 0, CRAFTING_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            
            slot.onTake(player, itemstack1);
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, this.blockEntity.getBlockState().getBlock());
    }
    
    public CoreBlockEntity getBlockEntity() {
        return blockEntity;
    }
}