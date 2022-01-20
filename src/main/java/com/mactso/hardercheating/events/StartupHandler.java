package com.mactso.hardercheating.events;

import java.io.File;

import com.mactso.hardercheating.Main;


import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class StartupHandler {

	public static void initReports() {
		File fd = new File("config/hardercheating");
		if (!fd.exists())
			fd.mkdir();
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void onStartup(FMLServerStartingEvent event) {
		System.out.println("Server Starting");
		initReports();
	}
	
}
