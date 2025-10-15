package com.coremod.init;

import com.coremod.CoreMod;
import com.coremod.blockentity.CoreBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CoreMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<CoreBlockEntity>> CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("core_block_entity",
                    () -> BlockEntityType.Builder.of(CoreBlockEntity::new, ModBlocks.CORE_BLOCK.get())
                            .build(null));
}
