package com.coremod.init;

import com.coremod.CoreMod;
import com.coremod.menu.CoreMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, CoreMod.MOD_ID);

    public static final RegistryObject<MenuType<CoreMenu>> CORE_MENU =
            MENU_TYPES.register("core_menu",
                    () -> IForgeMenuType.create(CoreMenu::new));
}
