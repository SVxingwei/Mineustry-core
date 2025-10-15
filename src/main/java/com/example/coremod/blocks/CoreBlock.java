package com.example.coremod.blocks;

import com.example.coremod.gui.CoreMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class CoreBlock extends Block implements EntityBlock {
    
    public static final EnumProperty<MultiblockPart> MULTIBLOCK_PART = EnumProperty.create("part", MultiblockPart.class);
    
    public enum MultiblockPart {
        MAIN(0, 0),
        X_POS(1, 0),
        Z_POS(0, 1),
        X_POS_Z_POS(1, 1);
        
        private final int xOffset;
        private final int zOffset;
        
        MultiblockPart(int xOffset, int zOffset) {
            this.xOffset = xOffset;
            this.zOffset = zOffset;
        }
        
        public int getXOffset() { return xOffset; }
        public int getZOffset() { return zOffset; }
        
        public static MultiblockPart fromOffsets(int x, int z) {
            for (MultiblockPart part : values()) {
                if (part.xOffset == x && part.zOffset == z) {
                    return part;
                }
            }
            return MAIN;
        }
    }
    
    public CoreBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(3.0f, 10.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(MULTIBLOCK_PART, MultiblockPart.MAIN));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MULTIBLOCK_PART);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, 
                                InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            // 找到主方块位置
            BlockPos mainPos = getMainBlockPos(pos, state);
            BlockEntity blockEntity = level.getBlockEntity(mainPos);
            if (blockEntity instanceof CoreBlockEntity coreEntity) {
                MenuProvider menuProvider = new SimpleMenuProvider(
                    (containerId, inventory, playerEntity) -> new CoreMenu(containerId, inventory, coreEntity),
                    Component.translatable("container.coremod.core_block")
                );
                NetworkHooks.openScreen((ServerPlayer) player, menuProvider, mainPos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    
    private BlockPos getMainBlockPos(BlockPos pos, BlockState state) {
        MultiblockPart part = state.getValue(MULTIBLOCK_PART);
        return pos.offset(-part.getXOffset(), 0, -part.getZOffset());
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // 如果是主方块，移除整个2x2结构
            if (state.getValue(MULTIBLOCK_PART) == MultiblockPart.MAIN) {
                for (int x = 0; x < 2; x++) {
                    for (int z = 0; z < 2; z++) {
                        BlockPos removePos = pos.offset(x, 0, z);
                        if (!removePos.equals(pos)) {
                            level.setBlock(removePos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoreBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, 
                                                                 BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof CoreBlockEntity coreEntity) {
                coreEntity.tick();
            }
        };
    }
}