package com.example.coremod;

import com.example.coremod.blocks.CoreBlock;
import com.example.coremod.blocks.CoreBlockEntity;
import com.example.coremod.blocks.CoreBlockItem;
import com.example.coremod.gui.CoreMenu;
import com.example.coremod.gui.CoreScreen;
import com.example.coremod.storage.CoreStorageManager;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(CoreMod.MODID)
public class CoreMod {
    public static final String MODID = "coremod";
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    
    public static final RegistryObject<CoreBlock> CORE_BLOCK = BLOCKS.register("core_block", CoreBlock::new);
    public static final RegistryObject<BlockItem> CORE_BLOCK_ITEM = ITEMS.register("core_block", 
        () -> new CoreBlockItem(CORE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<CoreBlockEntity>> CORE_BLOCK_ENTITY = BLOCK_ENTITIES.register("core_block",
        () -> BlockEntityType.Builder.of(CoreBlockEntity::new, CORE_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<CoreMenu>> CORE_MENU = MENUS.register("core_menu",
        () -> new MenuType<>(CoreMenu::new));
    
    public static final RegistryObject<CreativeModeTab> CORE_TAB = CREATIVE_MODE_TABS.register("core_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.coremod"))
            .icon(() -> CORE_BLOCK_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CORE_BLOCK_ITEM.get());
            })
            .build());
    
    public CoreMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        CoreStorageManager.initialize();
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(CORE_MENU.get(), CoreScreen::new);
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        CoreStorageManager.initialize();
    }
}