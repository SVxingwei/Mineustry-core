package com.coremod.init;

import com.coremod.CoreMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 创造模式标签页
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CoreMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CORE_TAB = CREATIVE_MODE_TABS.register("core_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.CORE_BLOCK_ITEM.get()))
                    .title(Component.translatable("itemGroup." + CoreMod.MOD_ID + ".core_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.CORE_BLOCK_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
