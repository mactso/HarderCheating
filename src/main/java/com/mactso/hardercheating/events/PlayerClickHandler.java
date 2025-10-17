package com.mactso.hardercheating.events;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mactso.hardercheating.Main;
import com.mactso.hardercheating.myconfig.MyConfig;
import com.mactso.hardercheating.util.MyLogger;
import com.mactso.hardercheating.util.PlayerInteractionData;
import com.mactso.hardercheating.util.Utility;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class PlayerClickHandler {

	private static final Map<UUID, PlayerInteractionData> ClickInteractionDataMap = new ConcurrentHashMap<>();
	private static final String TOO_MANY_CLICK_MSG = "Disconnected for Clicking too many times per second.";
	private static final String TOO_FAR_CLICK_MSG = "Disconnected for Clicking too far away.";

	@SubscribeEvent
	public static void onChange(PlayerInteractEvent event) {

		if (event.getEntity() instanceof ServerPlayer serverPlayer) {

			checkClickingTooFarAway(event, serverPlayer);

			if (MyConfig.isCheckMaxClicksPerSecond()) {
				if (isClickingTooMuchPerSecond(serverPlayer)) {
					MyLogger.logItem(serverPlayer, TOO_MANY_CLICK_MSG, true);
					
					if (MyConfig.isBanPlayer()) {
						Utility.banAndKick(serverPlayer, TOO_MANY_CLICK_MSG, MyConfig.getBanDays());
					} else {
						serverPlayer.connection.disconnect(net.minecraft.network.chat.Component.literal(TOO_MANY_CLICK_MSG));
					}

					return;
				}
			}

		}
	}

	private static boolean isClickingTooMuchPerSecond(ServerPlayer serverPlayer) {
		UUID playerUUID = serverPlayer.getUUID();
		PlayerInteractionData data = ClickInteractionDataMap.computeIfAbsent(playerUUID,
				k -> new PlayerInteractionData());

		long currentTime = System.currentTimeMillis();
		// Check if a full second has passed since the last tracked event
		if ((currentTime - data.lastInteractionTime > MyConfig.TIME_WINDOW_MS) && (data.interactionCount > 0)) {
			data.interactionCount = 0;
			data.lastInteractionTime = currentTime;
			return false;
		}
		data.interactionCount++;

		// System.out.println("Player " + serverPlayer.getName().getString() + " " +
		// data.interactionCount + " clicks per second.");

		if (data.interactionCount > MyConfig.getMaxClicksPerSecond() - 10) {
			return true;
		}

		return false;

	}

	private static void checkClickingTooFarAway(PlayerInteractEvent event, ServerPlayer serverPlayer) {
		int dist = event.getPos().distManhattan(serverPlayer.blockPosition());

		if (dist > MyConfig.getMaxClickDistance()) {

			String temp = "Player at " + String.format("%-20s", serverPlayer.blockPosition().toString())
					+ " clicked too far away "
					+ String.format("%-20s", event.getPos().toString() + " " + dist + " blocks away.");
			MyLogger.logItem(serverPlayer, temp, true);
			
			if (MyConfig.isBanPlayer()) {
				Utility.banAndKick(serverPlayer, TOO_FAR_CLICK_MSG, MyConfig.getBanDays());
			} else {
				serverPlayer.connection.disconnect(net.minecraft.network.chat.Component.literal(TOO_FAR_CLICK_MSG));
			}

		}
	}

}
