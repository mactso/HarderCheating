package com.mactso.hardercheating;

import org.apache.commons.lang3.tuple.Pair;

import com.mactso.hardercheating.config.MyConfig;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod("hardercheating")
public class Main {

	    public static final String MODID = "hardercheating"; 
	    
	    public Main()
	    {

			ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
					() -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a,b) -> true));
	    	System.out.println(MODID + ": Registering Mod.");
 	        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,MyConfig.COMMON_SPEC );
	    }

}
