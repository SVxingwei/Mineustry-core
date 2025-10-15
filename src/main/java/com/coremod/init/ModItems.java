package com.coremod.init;

import com.coremod.CoreMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CoreMod.MOD_ID);

    public static final RegistryObject<Item> CORE_BLOCK_ITEM = ITEMS.register("core_block",
            () -> new BlockItem(ModBlocks.CORE_BLOCK.get(), new Item.Properties()));
}
