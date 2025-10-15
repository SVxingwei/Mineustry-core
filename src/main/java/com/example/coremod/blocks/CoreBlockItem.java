package com.example.coremod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CoreBlockItem extends BlockItem {
    
    public CoreBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
    
    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        
        // 检查2x2区域是否可用
        if (!canPlace2x2(level, pos)) {
            return false;
        }
        
        // 放置2x2方块
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                BlockPos placePos = pos.offset(x, 0, z);
                if (x == 0 && z == 0) {
                    // 主方块
                    level.setBlock(placePos, state, 3);
                } else {
                    // 辅助方块
                    level.setBlock(placePos, state.setValue(CoreBlock.MULTIBLOCK_PART, 
                        CoreBlock.MultiblockPart.values()[x * 2 + z]), 3);
                }
            }
        }
        
        return true;
    }
    
    private boolean canPlace2x2(Level level, BlockPos pos) {
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                if (!level.getBlockState(checkPos).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }
}