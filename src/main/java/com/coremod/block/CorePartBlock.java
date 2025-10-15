package com.coremod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 核心方块的组成部分（2x2结构中的非主方块）
 */
public class CorePartBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<MultiBlockPart> PART = EnumProperty.create("part", MultiBlockPart.class);

    public enum MultiBlockPart implements net.minecraft.util.StringRepresentable {
        TOP_RIGHT("top_right"),
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_RIGHT("bottom_right");

        private final String name;

        MultiBlockPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public CorePartBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, MultiBlockPart.TOP_RIGHT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        // 找到主方块并委托给它
        BlockPos masterPos = getMasterPos(pos, state);
        if (masterPos != null) {
            BlockState masterState = level.getBlockState(masterPos);
            return masterState.use(level, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !isMoving) {
            // 破坏整个多方块结构
            BlockPos masterPos = getMasterPos(pos, state);
            if (masterPos != null) {
                level.destroyBlock(masterPos, true);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    /**
     * 获取主方块位置
     */
    private BlockPos getMasterPos(BlockPos partPos, BlockState partState) {
        Direction facing = partState.getValue(FACING);
        MultiBlockPart part = partState.getValue(PART);

        return switch (part) {
            case TOP_RIGHT -> switch (facing) {
                case NORTH -> partPos.west();
                case SOUTH -> partPos.east();
                case WEST -> partPos.south();
                case EAST -> partPos.north();
                default -> partPos;
            };
            case BOTTOM_LEFT -> switch (facing) {
                case NORTH -> partPos.east().above();
                case SOUTH -> partPos.west().above();
                case WEST -> partPos.north().above();
                case EAST -> partPos.south().above();
                default -> partPos;
            };
            case BOTTOM_RIGHT -> switch (facing) {
                case NORTH -> partPos.above();
                case SOUTH -> partPos.above();
                case WEST -> partPos.above();
                case EAST -> partPos.above();
                default -> partPos;
            };
        };
    }
}
