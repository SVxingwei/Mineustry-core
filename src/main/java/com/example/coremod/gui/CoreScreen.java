package com.example.coremod.gui;

import com.example.coremod.blocks.CoreBlockEntity;
import com.example.coremod.storage.CoreStorageManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CoreScreen extends AbstractContainerScreen<CoreMenu> {
    
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("coremod", "textures/gui/core_gui.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    
    private int scrollOffset = 0;
    private int maxScroll = 0;
    
    public CoreScreen(CoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }
    
    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96;
        
        // 计算滚动相关参数
        updateScrollParameters();
    }
    
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(GUI_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        
        // 绘制合成栏背景
        graphics.blit(GUI_TEXTURE, x + 7, y + 17, 176, 0, 54, 54);
        
        // 绘制合成结果槽位背景
        graphics.blit(GUI_TEXTURE, x + 122, y + 35, 176, 54, 22, 22);
        
        // 绘制物品浏览区域
        drawItemBrowser(graphics, x, y);
        
        // 绘制滚动条
        if (maxScroll > 0) {
            int scrollBarX = x + GUI_WIDTH - 12;
            int scrollBarY = y + 18;
            int scrollBarHeight = 54;
            
            int scrollThumbHeight = Math.max(8, (scrollBarHeight * scrollBarHeight) / (maxScroll + scrollBarHeight));
            int scrollThumbY = scrollBarY + (scrollOffset * (scrollBarHeight - scrollThumbHeight)) / maxScroll;
            
            graphics.fill(scrollBarX, scrollBarY, scrollBarX + 6, scrollBarY + scrollBarHeight, 0xFF808080);
            graphics.fill(scrollBarX, scrollThumbY, scrollBarX + 6, scrollThumbY + scrollThumbHeight, 0xFF404040);
        }
        
        // 绘制设置按钮
        graphics.blit(GUI_TEXTURE, x + GUI_WIDTH - 20, y + 6, 176, 76, 16, 16);
    }
    
    private void drawItemBrowser(GuiGraphics graphics, int x, int y) {
        CoreBlockEntity blockEntity = menu.getBlockEntity();
        String coreId = blockEntity.getCoreId();
        
        if (coreId.isEmpty()) return;
        
        CoreStorageManager.CoreStorage storage = CoreStorageManager.getInstance().getStorage(coreId);
        if (storage == null) return;
        
        var items = storage.getAllItems();
        if (items.isEmpty()) return;
        
        int startX = x + 8;
        int startY = y + 18;
        int slotSize = 18;
        int slotsPerRow = 8;
        int visibleRows = 3;
        
        int itemIndex = scrollOffset * slotsPerRow;
        int maxItems = visibleRows * slotsPerRow;
        
        var itemList = items.entrySet().stream().toList();
        
        for (int i = 0; i < maxItems && itemIndex + i < itemList.size(); i++) {
            var entry = itemList.get(itemIndex + i);
            ItemStack stack = entry.getKey();
            int count = entry.getValue();
            
            int slotX = startX + (i % slotsPerRow) * slotSize;
            int slotY = startY + (i / slotsPerRow) * slotSize;
            
            // 绘制槽位背景
            graphics.blit(GUI_TEXTURE, slotX, slotY, 176, 18, slotSize, slotSize);
            
            // 绘制物品
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, slotX + 1, slotY + 1);
                graphics.renderItemDecorations(this.font, stack, slotX + 1, slotY + 1);
                
                // 绘制数量
                if (count > 1) {
                    String countText = count > 999 ? "999+" : String.valueOf(count);
                    graphics.drawString(this.font, countText, slotX + 19 - this.font.width(countText), 
                        slotY + 19 - this.font.lineHeight, 0xFFFFFF, true);
                }
            }
        }
    }
    
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        
        // 绘制能量信息
        CoreBlockEntity blockEntity = menu.getBlockEntity();
        int energy = blockEntity.getEnergyStorage().getEnergyStored();
        int maxEnergy = blockEntity.getEnergyStorage().getMaxEnergyStored();
        String energyText = String.format("能量: %d/%d RF", energy, maxEnergy);
        graphics.drawString(this.font, energyText, 8, 18, 0x404040);
        
        // 绘制存储信息
        String coreId = blockEntity.getCoreId();
        if (!coreId.isEmpty()) {
            int coreCount = CoreStorageManager.getInstance().getCoreCount(coreId);
            int maxItems = CoreStorageManager.getInstance().getMaxItemsPerType(coreId);
            String storageText = String.format("核心数量: %d | 每类最大: %d", coreCount, maxItems);
            graphics.drawString(this.font, storageText, 8, 30, 0x404040);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 检查设置按钮点击
        if (mouseX >= x + GUI_WIDTH - 20 && mouseX < x + GUI_WIDTH - 4 &&
            mouseY >= y + 6 && mouseY < y + 22) {
            // TODO: 打开设置GUI
            return true;
        }
        
        // 检查滚动条点击
        if (maxScroll > 0) {
            int scrollBarX = x + GUI_WIDTH - 12;
            int scrollBarY = y + 18;
            int scrollBarHeight = 54;
            
            if (mouseX >= scrollBarX && mouseX < scrollBarX + 6 &&
                mouseY >= scrollBarY && mouseY < scrollBarY + scrollBarHeight) {
                int newScroll = (int) ((mouseY - scrollBarY) * maxScroll / scrollBarHeight);
                setScrollOffset(Math.max(0, Math.min(maxScroll, newScroll)));
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0) {
            setScrollOffset(scrollOffset - (int) delta);
            return true;
        }
        return false;
    }
    
    private void updateScrollParameters() {
        CoreBlockEntity blockEntity = menu.getBlockEntity();
        String coreId = blockEntity.getCoreId();
        
        if (coreId.isEmpty()) {
            maxScroll = 0;
            scrollOffset = 0;
            return;
        }
        
        CoreStorageManager.CoreStorage storage = CoreStorageManager.getInstance().getStorage(coreId);
        if (storage == null) {
            maxScroll = 0;
            scrollOffset = 0;
            return;
        }
        
        int totalItems = storage.getAllItems().size();
        int visibleItems = 3 * 8; // 3行 x 8列
        maxScroll = Math.max(0, (totalItems + visibleItems - 1) / visibleItems - 1);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
    }
    
    private void setScrollOffset(int newOffset) {
        scrollOffset = Math.max(0, Math.min(maxScroll, newOffset));
    }
}