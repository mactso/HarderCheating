package com.mactso.hardercheating;

import com.mactso.hardercheating.config.MyConfig;

import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.fml.config.ModConfig;

@Mod("hardercheating")
public class Main {

	    public static final String MODID = "hardercheating"; 
	    
	    public Main()
	    {
			ModLoadingContext.get().registerExtensionPoint(DisplayTest.class,
					() -> new DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));	
	    	System.out.println(MODID + ": Registering Mod.");
 	        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,MyConfig.COMMON_SPEC );
	    }

}
