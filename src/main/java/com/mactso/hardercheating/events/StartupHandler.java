package com.mactso.hardercheating.events;

import java.io.File;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class StartupHandler {

	static {
		initReports();
	}
	
	public static void initReports() {
		File fd = new File("config/hardercheating");
		if (!fd.exists())
			fd.mkdir();
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void onStartup(ServerStartingEvent event) {
		System.out.println("Server Starting");
	}
	
}
