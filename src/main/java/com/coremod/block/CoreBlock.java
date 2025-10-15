package com.coremod.block;

import com.coremod.blockentity.CoreBlockEntity;
import com.coremod.init.ModBlockEntities;
import com.coremod.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * 核心方块 - 2x2多方块结构的主方块（左上角）
 */
public class CoreBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 检查2x2区域是否可以放置
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection();

        if (!canPlaceMultiBlock(level, pos, facing)) {
            return null;
        }

        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        
        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);
            createMultiBlock(level, pos, facing);
        }
    }

    /**
     * 检查是否可以放置2x2结构
     */
    private boolean canPlaceMultiBlock(Level level, BlockPos pos, Direction facing) {
        BlockPos[] positions = getMultiBlockPositions(pos, facing);
        
        for (int i = 1; i < positions.length; i++) { // 跳过主方块位置
            if (!level.getBlockState(positions[i]).canBeReplaced()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 创建2x2多方块结构
     */
    private void createMultiBlock(Level level, BlockPos masterPos, Direction facing) {
        BlockPos[] positions = getMultiBlockPositions(masterPos, facing);
        CorePartBlock.MultiBlockPart[] parts = {
            CorePartBlock.MultiBlockPart.TOP_RIGHT,
            CorePartBlock.MultiBlockPart.BOTTOM_LEFT,
            CorePartBlock.MultiBlockPart.BOTTOM_RIGHT
        };

        for (int i = 0; i < parts.length; i++) {
            BlockState partState = ModBlocks.CORE_PART_BLOCK.get().defaultBlockState()
                    .setValue(CorePartBlock.FACING, facing)
                    .setValue(CorePartBlock.PART, parts[i]);
            level.setBlock(positions[i + 1], partState, 3);
        }
    }

    /**
     * 获取2x2结构的所有位置
     * 索引0：主方块（左上）
     * 索引1：右上
     * 索引2：左下
     * 索引3：右下
     */
    private BlockPos[] getMultiBlockPositions(BlockPos masterPos, Direction facing) {
        return switch (facing) {
            case NORTH -> new BlockPos[]{
                masterPos,                              // 左上（主方块）
                masterPos.east(),                       // 右上
                masterPos.below(),                      // 左下
                masterPos.east().below()                // 右下
            };
            case SOUTH -> new BlockPos[]{
                masterPos,
                masterPos.west(),
                masterPos.below(),
                masterPos.west().below()
            };
            case WEST -> new BlockPos[]{
                masterPos,
                masterPos.south(),
                masterPos.below(),
                masterPos.south().below()
            };
            case EAST -> new BlockPos[]{
                masterPos,
                masterPos.north(),
                masterPos.below(),
                masterPos.north().below()
            };
            default -> new BlockPos[]{masterPos, masterPos, masterPos, masterPos};
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, 
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CoreBlockEntity coreBlockEntity) {
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openScreen(serverPlayer, coreBlockEntity, pos);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, 
                                                                   BlockEntityType<T> type) {
        return level.isClientSide ? null : 
               createTickerHelper(type, ModBlockEntities.CORE_BLOCK_ENTITY.get(), CoreBlockEntity::serverTick);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> type, BlockEntityType<E> requiredType, BlockEntityTicker<? super E> ticker) {
        return requiredType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // 移除方块实体
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CoreBlockEntity coreBlockEntity) {
                coreBlockEntity.onBlockRemoved();
            }
            
            // 移除多方块结构的其他部分
            if (!isMoving) {
                Direction facing = state.getValue(FACING);
                BlockPos[] positions = getMultiBlockPositions(pos, facing);
                
                for (int i = 1; i < positions.length; i++) {
                    BlockState partState = level.getBlockState(positions[i]);
                    if (partState.getBlock() instanceof CorePartBlock) {
                        level.removeBlock(positions[i], false);
                    }
                }
            }
            
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
