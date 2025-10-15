package com.coremod.client.screen;

import com.coremod.CoreMod;
import com.coremod.menu.CoreMenu;
import com.coremod.network.NetworkHandler;
import com.coremod.network.PacketStorageRequest;
import com.coremod.storage.CoreStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 核心GUI屏幕
 */
public class CoreScreen extends AbstractContainerScreen<CoreMenu> {
    private static final ResourceLocation TEXTURE = 
            new ResourceLocation(CoreMod.MOD_ID, "textures/gui/core_gui.png");
    
    private int scrollOffset = 0;
    private List<Map.Entry<String, ItemStack>> storageItems = new ArrayList<>();
    private boolean showCrafting = true; // true=合成界面, false=存储浏览界面

    public CoreScreen(CoreMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        requestStorageUpdate();
    }

    @Override
    protected void init() {
        super.init();
        
        // 添加切换按钮（合成/浏览）
        this.addRenderableWidget(Button.builder(
                Component.literal(showCrafting ? "浏览" : "合成"),
                button -> {
                    showCrafting = !showCrafting;
                    button.setMessage(Component.literal(showCrafting ? "浏览" : "合成"));
                })
                .bounds(this.leftPos + 150, this.topPos + 5, 40, 20)
                .build());
        
        // 添加设置按钮
        this.addRenderableWidget(Button.builder(
                Component.literal("⚙"),
                button -> {
                    if (this.minecraft != null && this.menu.getBlockEntity() != null) {
                        this.minecraft.setScreen(new CoreConfigScreen(
                                this, 
                                this.menu.getBlockEntity().getBlockPos(), 
                                this.menu.getBlockEntity().getConfig()
                        ));
                    }
                })
                .bounds(this.leftPos + 155, this.topPos + 5, 16, 16)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 绘制背景
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (showCrafting) {
            renderCraftingInterface(guiGraphics, x, y);
        } else {
            renderStorageInterface(guiGraphics, x, y, mouseX, mouseY);
        }
    }

    /**
     * 渲染合成界面
     */
    private void renderCraftingInterface(GuiGraphics guiGraphics, int x, int y) {
        // 合成网格在Menu中已经有槽位，这里只需要绘制装饰
        // 绘制箭头（指向结果）
        guiGraphics.blit(TEXTURE, x + 90, y + 34, 176, 0, 22, 15);
    }

    /**
     * 渲染存储浏览界面
     */
    private void renderStorageInterface(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        // 绘制物品网格（6列 x 3行）
        int startX = x + 8;
        int startY = y + 18;
        int itemsPerRow = 6;
        int rows = 3;
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < itemsPerRow; col++) {
                int index = scrollOffset * itemsPerRow + row * itemsPerRow + col;
                if (index < storageItems.size()) {
                    Map.Entry<String, ItemStack> entry = storageItems.get(index);
                    ItemStack stack = entry.getValue();
                    
                    int slotX = startX + col * 18;
                    int slotY = startY + row * 18;
                    
                    // 绘制槽位背景
                    guiGraphics.blit(TEXTURE, slotX - 1, slotY - 1, 176, 16, 18, 18);
                    
                    // 绘制物品
                    guiGraphics.renderItem(stack, slotX, slotY);
                    guiGraphics.renderItemDecorations(this.font, stack, slotX, slotY, String.valueOf(stack.getCount()));
                }
            }
        }
        
        // 绘制滚动条
        int scrollBarX = x + 156;
        int scrollBarY = y + 18;
        int scrollBarHeight = 54;
        guiGraphics.blit(TEXTURE, scrollBarX, scrollBarY, 176, 34, 12, scrollBarHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
        
        // 在浏览模式下显示物品提示
        if (!showCrafting) {
            renderStorageTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    /**
     * 渲染存储物品的提示
     */
    private void renderStorageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startX = this.leftPos + 8;
        int startY = this.topPos + 18;
        int itemsPerRow = 6;
        int rows = 3;
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < itemsPerRow; col++) {
                int index = scrollOffset * itemsPerRow + row * itemsPerRow + col;
                if (index < storageItems.size()) {
                    int slotX = startX + col * 18;
                    int slotY = startY + row * 18;
                    
                    if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                        Map.Entry<String, ItemStack> entry = storageItems.get(index);
                        guiGraphics.renderTooltip(this.font, entry.getValue(), mouseX, mouseY);
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!showCrafting) {
            int maxScroll = Math.max(0, (storageItems.size() + 5) / 6 - 3);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!showCrafting) {
            // 如果玩家手持物品，尝试插入到存储
            if (this.minecraft != null && this.minecraft.player != null) {
                ItemStack carried = this.menu.getCarried();
                if (!carried.isEmpty()) {
                    // 发送插入物品的网络包（使用-1表示光标上的物品）
                    insertCarriedItem();
                    return true;
                }
            }
            
            // 处理存储物品点击（提取物品）
            int startX = this.leftPos + 8;
            int startY = this.topPos + 18;
            int itemsPerRow = 6;
            int rows = 3;
            
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < itemsPerRow; col++) {
                    int index = scrollOffset * itemsPerRow + row * itemsPerRow + col;
                    if (index < storageItems.size()) {
                        int slotX = startX + col * 18;
                        int slotY = startY + row * 18;
                        
                        if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                            Map.Entry<String, ItemStack> entry = storageItems.get(index);
                            extractItem(entry.getKey(), button);
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * 插入光标上的物品到核心存储
     */
    private void insertCarriedItem() {
        // 这个功能由Minecraft的槽位系统自动处理
        // 暂不需要额外实现
    }

    /**
     * 提取物品
     * @param itemKey 物品键
     * @param button 鼠标按钮（0=左键, 1=右键）
     */
    private void extractItem(String itemKey, int button) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        
        int amount;
        boolean shift = hasShiftDown();
        
        if (button == 0) { // 左键
            if (shift) {
                // Shift+左键：提取所有
                amount = Integer.MAX_VALUE;
            } else {
                // 左键：提取一组（64个）
                amount = 64;
            }
        } else if (button == 1) { // 右键
            // 右键：提取一个
            amount = 1;
        } else {
            return;
        }
        
        // 发送提取物品的网络包
        NetworkHandler.sendToServer(new PacketExtractItem(this.menu.getCoreId(), itemKey, amount));
    }

    /**
     * 请求服务器更新存储数据
     */
    private void requestStorageUpdate() {
        if (this.minecraft != null && this.minecraft.player != null) {
            NetworkHandler.sendToServer(new PacketStorageRequest(this.menu.getCoreId()));
        }
    }

    /**
     * 更新存储物品列表（由网络包调用）
     */
    public void updateStorageItems(Map<String, ItemStack> items) {
        this.storageItems = new ArrayList<>(items.entrySet());
        this.scrollOffset = 0;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        
        // 显示容量信息
        CoreStorage storage = this.menu.getStorage();
        if (storage != null) {
            String coreId = this.menu.getCoreId();
            int current = storage.getCurrentUsage(coreId);
            int max = storage.getMaxCapacity(coreId);
            String capacityText = String.format("%d / %d", current, max);
            guiGraphics.drawString(this.font, capacityText, 8, this.imageHeight - 94, 4210752, false);
        }
    }
}
