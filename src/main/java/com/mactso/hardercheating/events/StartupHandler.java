package com.mactso.hardercheating.events;

import java.io.File;

import com.mactso.hardercheating.Main;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class StartupHandler {

	public static void initReports() {
		File fd = new File("config/hardercheating");
		if (!fd.exists())
			fd.mkdir();
	}
	
	@SubscribeEvent(priority = Priority.NORMAL)
	public static void onStartup(ServerStartingEvent event) {
		System.out.println("Server Starting");
		initReports();
	}
	
}
