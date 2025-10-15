package com.coremod.client.screen;

import com.coremod.network.NetworkHandler;
import com.coremod.network.PacketConfigUpdate;
import com.coremod.storage.CoreConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * 核心配置界面
 */
public class CoreConfigScreen extends Screen {
    private final Screen parentScreen;
    private final BlockPos blockPos;
    private final CoreConfig config;
    
    private EditBox coreIdField;
    private Button publicButton;
    private Button allowOthersButton;
    private Button burnOverflowButton;
    private Button saveButton;
    private Button cancelButton;
    
    private boolean isPublic;
    private boolean allowOthers;
    private boolean burnOverflow;

    public CoreConfigScreen(Screen parentScreen, BlockPos blockPos, CoreConfig config) {
        super(Component.translatable("gui.coremod.core.settings"));
        this.parentScreen = parentScreen;
        this.blockPos = blockPos;
        this.config = config;
        this.isPublic = config.isPublic();
        this.allowOthers = config.isAllowOthers();
        this.burnOverflow = config.isBurnOverflow();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;
        
        // 核心ID输入框
        this.addRenderableWidget(Component.literal(""));
        coreIdField = new EditBox(this.font, centerX - 100, startY, 200, 20, Component.literal(""));
        coreIdField.setMaxLength(32);
        coreIdField.setValue(config.getCoreId());
        this.addRenderableWidget(coreIdField);
        
        // 公共核心按钮
        publicButton = Button.builder(
                Component.translatable("gui.coremod.core.config.is_public")
                        .append(": ")
                        .append(isPublic ? Component.literal("✓") : Component.literal("✗")),
                button -> {
                    isPublic = !isPublic;
                    button.setMessage(Component.translatable("gui.coremod.core.config.is_public")
                            .append(": ")
                            .append(isPublic ? Component.literal("✓") : Component.literal("✗")));
                })
                .bounds(centerX - 100, startY + 30, 200, 20)
                .build();
        this.addRenderableWidget(publicButton);
        
        // 允许其他玩家按钮
        allowOthersButton = Button.builder(
                Component.translatable("gui.coremod.core.config.allow_others")
                        .append(": ")
                        .append(allowOthers ? Component.literal("✓") : Component.literal("✗")),
                button -> {
                    allowOthers = !allowOthers;
                    button.setMessage(Component.translatable("gui.coremod.core.config.allow_others")
                            .append(": ")
                            .append(allowOthers ? Component.literal("✓") : Component.literal("✗")));
                })
                .bounds(centerX - 100, startY + 55, 200, 20)
                .build();
        this.addRenderableWidget(allowOthersButton);
        
        // 烧毁溢出物品按钮
        burnOverflowButton = Button.builder(
                Component.translatable("gui.coremod.core.config.burn_overflow")
                        .append(": ")
                        .append(burnOverflow ? Component.literal("✓") : Component.literal("✗")),
                button -> {
                    burnOverflow = !burnOverflow;
                    button.setMessage(Component.translatable("gui.coremod.core.config.burn_overflow")
                            .append(": ")
                            .append(burnOverflow ? Component.literal("✓") : Component.literal("✗")));
                })
                .bounds(centerX - 100, startY + 80, 200, 20)
                .build();
        this.addRenderableWidget(burnOverflowButton);
        
        // 保存按钮
        saveButton = Button.builder(
                Component.translatable("gui.done"),
                button -> {
                    saveConfig();
                    this.minecraft.setScreen(parentScreen);
                })
                .bounds(centerX - 100, startY + 110, 95, 20)
                .build();
        this.addRenderableWidget(saveButton);
        
        // 取消按钮
        cancelButton = Button.builder(
                Component.translatable("gui.cancel"),
                button -> this.minecraft.setScreen(parentScreen))
                .bounds(centerX + 5, startY + 110, 95, 20)
                .build();
        this.addRenderableWidget(cancelButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // 绘制标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // 绘制标签
        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;
        
        guiGraphics.drawString(this.font, 
                Component.translatable("gui.coremod.core.config.core_id"), 
                centerX - 100, startY - 12, 0xFFFFFF, false);
        
        // 显示拥有者信息
        if (!config.getOwnerId().isEmpty()) {
            guiGraphics.drawString(this.font, 
                    Component.translatable("gui.coremod.core.config.owner")
                            .append(": ")
                            .append(config.getOwnerId()), 
                    centerX - 100, startY + 140, 0xAAAAAA, false);
        }
    }

    /**
     * 保存配置到服务器
     */
    private void saveConfig() {
        String newCoreId = coreIdField.getValue();
        if (newCoreId.isEmpty()) {
            newCoreId = "default";
        }
        
        NetworkHandler.sendToServer(new PacketConfigUpdate(
                blockPos,
                newCoreId,
                isPublic,
                allowOthers,
                burnOverflow
        ));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
