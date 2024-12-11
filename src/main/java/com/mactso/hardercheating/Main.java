package com.mactso.hardercheating;

import com.mactso.hardercheating.config.MyConfig;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod("hardercheating")
public class Main {

	    public static final String MODID = "hardercheating"; 
	    
	    public Main()
	    {
	    	System.out.println(MODID + ": Registering Mod.");
 	        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,MyConfig.COMMON_SPEC );
	    }

}
