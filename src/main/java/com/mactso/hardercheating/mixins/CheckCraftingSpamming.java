package com.mactso.hardercheating.mixins;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mactso.hardercheating.myconfig.MyConfig;
import com.mactso.hardercheating.util.MyLogger;
import com.mactso.hardercheating.util.PlayerInteractionData;
import com.mactso.hardercheating.util.Utility;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(targets = "net.minecraft.server.level.ServerPlayer$1")
public class CheckCraftingSpamming {

	@Shadow
	@Final
	private ServerPlayer this$0;
	// A map to store the interaction count and last interaction time for each
	// player.
	// This needs to be static because the method we're injecting into is static.
	private static final Map<UUID, PlayerInteractionData> RecipeInteractionDataMap = new ConcurrentHashMap<>();

	/**
	 * Injects code at the head of the slotChangedCraftingGrid method. This will run
	 * our code before the original method's logic executes.
	 */

	@Inject(method = "sendSlotChange(Lnet/minecraft/world/inventory/AbstractContainerMenu;ILnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
	private void onSendSlotChange(AbstractContainerMenu menu, int slot, ItemStack stack, CallbackInfo ci) {

		if (!MyConfig.isCheckMaxRecipesPerSecond())
			return;
		

		boolean doDebug = false;
		if (MyConfig.getDebugLevel() > 0)
			doDebug = true;

		// Remove records older than 6 seconds
		cleanupOldEntries(6_000L);

		ServerPlayer serverPlayer = this$0;
		System.out.println("Recipe Change");

		UUID playerUUID = serverPlayer.getUUID();

		// Retrieve or create the player's interaction data
		PlayerInteractionData data = RecipeInteractionDataMap.computeIfAbsent(playerUUID,
				k -> new PlayerInteractionData());
		long currentTime = System.currentTimeMillis();
		long netTime = currentTime - data.lastInteractionTime;
		if (doDebug)
			Utility.debugMsg(1, "player=" + serverPlayer.getName().getString() + ", currentTime=" + currentTime
					+ ", netTime=" + netTime + ", interactionCount=" + data.interactionCount);
		// Check if a full second has passed since the last tracked event
		if ((netTime > MyConfig.TIME_WINDOW_MS) && (data.interactionCount > 0)) {
			// Reset the counter and update the timestamp once per second.
			Utility.debugMsg(0, "Player " + serverPlayer.getName().getString() + " crafting counter reset.");
			data.interactionCount = 0;
			data.lastInteractionTime = currentTime;
			return;
		}
		data.interactionCount++;
		int maxInteractions = MyConfig.getMaxCraftRecipesPerSecond();
		if (doDebug)
			Utility.debugMsg(1, "player=" + serverPlayer.getName().getString() + ", interactionCount="
					+ data.interactionCount + ", maxInteractions=" + maxInteractions);
		if (data.interactionCount > maxInteractions) {

			MyLogger.logItem(serverPlayer, " Disconnected for Rapid Crafting Recipe Spamming.", true);

			serverPlayer.connection.disconnect(
					net.minecraft.network.chat.Component.literal("Disconnected for Rapid Crafting Recipe Spamming."));

			// Cancel the original method to stop the malicious behavior
			ci.cancel();
			return;
		}


	}
	
	// Remove players whose last interaction was more than 'thresholdMs' milliseconds ago
    public static void cleanupOldEntries(long thresholdMs) {
        long now = System.currentTimeMillis();

        Iterator<Entry<UUID, PlayerInteractionData>> iter = RecipeInteractionDataMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, PlayerInteractionData> entry = iter.next();
            PlayerInteractionData data = entry.getValue();
            if (now - data.lastInteractionTime > thresholdMs) {
                iter.remove();
            }
        }
    }

}