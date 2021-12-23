package com.mactso.hardercheating.events;

import com.mactso.hardercheating.Main;
import com.mactso.hardercheating.config.MyConfig;
import com.mactso.hardercheating.util.MyLogger;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class PlayerClickHandler {

	@SubscribeEvent
	public static void onChange(PlayerInteractEvent event) {
		boolean header = true;
		if (event.getPlayer() instanceof ServerPlayer player) {

			int dist = event.getPos().distManhattan(player.blockPosition());

			if (dist > MyConfig.getMaxClickDistance()) {

				String temp = "Player at " + String.format("%-20s", player.blockPosition().toString()) + " clicked on"
						+ String.format("%-20s", event.getPos().toString() + " " + dist + " blocks away.");
				MyLogger.logItem(player, temp, header);
			}
		}
	}

}
