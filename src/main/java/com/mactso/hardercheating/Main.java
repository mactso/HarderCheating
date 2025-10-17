package com.mactso.hardercheating;

import com.mactso.hardercheating.commands.MyCommands;
import com.mactso.hardercheating.myconfig.MyConfig;
import com.mactso.hardercheating.util.MyLogger;
import com.mactso.hardercheating.util.Utility;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;

@Mod("hardercheating")
public class Main {

	public static final String MODID = "hardercheating";

	public Main() {
		Utility.debugMsg(0,MODID + ": Registering Mod.");
		Utility.debugMsg(0, MODID + ": Maintains a log file in config/hardercheating/activity.log.");
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MyConfig.COMMON_SPEC);
		MyLogger.resetLogFile();
	}

	@Mod.EventBusSubscriber()
	public static class ForgeEvents {
		@SubscribeEvent
		public static void onCommandsRegistry(final RegisterCommandsEvent event) {
			Utility.debugMsg(0,MODID + ": Registering MyCommands.");
			MyCommands.register(event.getDispatcher());
		}

	}
	
	@Mod.EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
	public class ServerLifecycleHandler {

	    @SubscribeEvent
	    public static void onServerStopping(ServerStoppingEvent event) {
	        MyLogger.closeLog();
	    }
	}
}
