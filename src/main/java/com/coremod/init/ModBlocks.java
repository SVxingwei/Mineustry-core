package com.coremod.init;

import com.coremod.CoreMod;
import com.coremod.block.CoreBlock;
import com.coremod.block.CorePartBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CoreMod.MOD_ID);

    public static final RegistryObject<Block> CORE_BLOCK = BLOCKS.register("core_block",
            () -> new CoreBlock(BlockBehaviour.Properties.of()
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Block> CORE_PART_BLOCK = BLOCKS.register("core_part_block",
            () -> new CorePartBlock(BlockBehaviour.Properties.of()
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .noLootTable())); // 不掉落物品，因为主方块会掉落
}
