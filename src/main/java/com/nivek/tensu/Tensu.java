package com.nivek.tensu;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Tensu.MODID)
public class Tensu {
    public static final String MODID = "tensu";

    public Tensu(FMLJavaModLoadingContext context) {
        Network.init();
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        CommandHandler.register();
    }
}
