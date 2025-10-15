package com.coremod;

import com.coremod.init.ModBlocks;
import com.coremod.init.ModItems;
import com.coremod.init.ModBlockEntities;
import com.coremod.init.ModMenuTypes;
import com.coremod.init.ModCreativeTabs;
import com.coremod.network.NetworkHandler;
import com.coremod.compat.ae2.AE2Integration;
import com.coremod.compat.refinedstorage.RSIntegration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CoreMod.MOD_ID)
public class CoreMod {
    public static final String MOD_ID = "coremod";
    public static final Logger LOGGER = LogManager.getLogger();

    public CoreMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.register();
            AE2Integration.init();
            RSIntegration.init();
        });
    }
}
