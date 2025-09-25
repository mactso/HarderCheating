package com.mactso.hardercheating.events;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mactso.hardercheating.Main;
import com.mactso.hardercheating.config.MyConfig;
import com.mactso.hardercheating.util.MyLogger;
import com.mactso.hardercheating.util.PlayerInteractionData;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class PlayerClickHandler {

	private static final Map<UUID, PlayerInteractionData> ClickInteractionDataMap = new ConcurrentHashMap<>();


	@SubscribeEvent
	public static void onChange(PlayerInteractEvent event) {
		
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {

			// System.out.println ("Player Click");
			UUID playerUUID = serverPlayer.getUUID();
			PlayerInteractionData data = ClickInteractionDataMap.computeIfAbsent(playerUUID, k -> new PlayerInteractionData());
			
			long currentTime = System.currentTimeMillis();
			// Check if a full second has passed since the last tracked event
			if ((currentTime - data.lastInteractionTime > MyConfig.TIME_WINDOW_MS) && (data.interactionCount > 0)) {
				// Reset the counter and update the timestamp
				// System.out.println("Player " + serverPlayer.getName().getString() + " click counter reset.");
				data.interactionCount = 0;
				data.lastInteractionTime = currentTime;
				return;
			}
			data.interactionCount++;

			// System.out.println("Player " + serverPlayer.getName().getString() + " " + data.interactionCount + " clicks per second.");

			if (data.interactionCount > MyConfig.getMaxClicksPerSecond() - 10 ) {

				MyLogger.logItem(serverPlayer, "Disconnected for Clicking too many times per second.", false);
				
				serverPlayer.connection.disconnect(
						net.minecraft.network.chat.Component.literal("Disconnected for Clicking too many times per second."));
				return;
			}
			
			int dist = event.getPos().distManhattan(serverPlayer.blockPosition());

			if (dist > MyConfig.getMaxClickDistance()) {

				String temp = "Player at " + String.format("%-20s", serverPlayer.blockPosition().toString()) + " clicked too far away "
						+ String.format("%-20s", event.getPos().toString() + " " + dist + " blocks away.");
				MyLogger.logItem(serverPlayer, temp, true);
				serverPlayer.connection.disconnect(
						net.minecraft.network.chat.Component.literal("Disconnected for Clicking too Far Away."));

			}
		}
	}

}
