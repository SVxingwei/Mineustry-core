package com.coremod.menu;

import com.coremod.blockentity.CoreBlockEntity;
import com.coremod.init.ModMenuTypes;
import com.coremod.storage.CoreStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 核心菜单 - 处理GUI的逻辑
 */
public class CoreMenu extends AbstractContainerMenu {
    private final CoreBlockEntity blockEntity;
    private final Level level;
    private final BlockPos pos;
    
    // 工作台合成槽位（3x3）
    private final ItemStackHandler craftingGrid = new ItemStackHandler(9);
    private final ItemStackHandler craftingResult = new ItemStackHandler(1);
    
    // 玩家背包槽位索引
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    public CoreMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, null, extraData.readBlockPos());
    }

    public CoreMenu(int containerId, Inventory playerInv, CoreBlockEntity blockEntity, BlockPos pos) {
        super(ModMenuTypes.CORE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInv.player.level();
        this.pos = pos;

        // 添加合成槽位 (3x3网格)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new SlotItemHandler(craftingGrid, col + row * 3, 30 + col * 18, 17 + row * 18) {
                    @Override
                    public void setChanged() {
                        super.setChanged();
                        slotsChanged(craftingGrid);
                    }
                });
            }
        }

        // 添加合成结果槽位
        this.addSlot(new SlotItemHandler(craftingResult, 0, 124, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public @NotNull ItemStack remove(int amount) {
                if (hasItem()) {
                    onCraftingResultTaken();
                }
                return super.remove(amount);
            }
        });

        // 添加玩家背包
        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && blockEntity.canPlayerAccess(player) &&
               player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // 从合成结果槽位快速移动
            if (index == 9) {
                if (!this.moveItemStackTo(slotStack, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
                onCraftingResultTaken();
            }
            // 从玩家背包快速移动
            else if (index >= 10 && index < 46) {
                if (!this.moveItemStackTo(slotStack, 0, 9, false)) {
                    if (index < 37) {
                        if (!this.moveItemStackTo(slotStack, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotStack, 10, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // 从合成网格快速移动
            else if (!this.moveItemStackTo(slotStack, 10, 46, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    /**
     * 当合成网格内容改变时调用
     */
    public void slotsChanged(ItemStackHandler handler) {
        if (!level.isClientSide) {
            updateCraftingResult();
        }
    }

    /**
     * 更新合成结果
     */
    private void updateCraftingResult() {
        CraftingContainer craftingContainer = new TransientCraftingContainer(this, 3, 3);
        for (int i = 0; i < 9; i++) {
            craftingContainer.setItem(i, craftingGrid.getStackInSlot(i));
        }

        Optional<CraftingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftingContainer, level);

        if (recipe.isPresent()) {
            ItemStack result = recipe.get().assemble(craftingContainer, level.registryAccess());
            craftingResult.setStackInSlot(0, result);
        } else {
            craftingResult.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    /**
     * 当合成结果被取出时调用
     */
    private void onCraftingResultTaken() {
        CraftingContainer craftingContainer = new TransientCraftingContainer(this, 3, 3);
        for (int i = 0; i < 9; i++) {
            craftingContainer.setItem(i, craftingGrid.getStackInSlot(i));
        }

        Optional<CraftingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftingContainer, level);

        if (recipe.isPresent()) {
            // 消耗材料
            for (int i = 0; i < 9; i++) {
                ItemStack stack = craftingGrid.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    stack.shrink(1);
                    craftingGrid.setStackInSlot(i, stack);
                }
            }
            updateCraftingResult();
        }
    }

    public CoreBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ItemStackHandler getCraftingGrid() {
        return craftingGrid;
    }

    public ItemStackHandler getCraftingResult() {
        return craftingResult;
    }

    /**
     * 获取核心的存储
     */
    public CoreStorage getStorage() {
        return blockEntity != null ? blockEntity.getStorage() : null;
    }

    /**
     * 获取核心ID
     */
    public String getCoreId() {
        return blockEntity != null ? blockEntity.getConfig().getActualStorageId() : "";
    }
}
