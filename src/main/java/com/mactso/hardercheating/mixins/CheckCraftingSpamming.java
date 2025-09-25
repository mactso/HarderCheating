package com.mactso.hardercheating.mixins;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mactso.hardercheating.config.MyConfig;
import com.mactso.hardercheating.util.MyLogger;
import com.mactso.hardercheating.util.PlayerInteractionData;

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


		ServerPlayer serverPlayer = this$0;
		// System.out.println ("Recipe Change");
		// System.out.println("Player " + serverPlayer.getName().getString() + " is crafting counter reset.");

		UUID playerUUID = serverPlayer.getUUID();

		// Retrieve or create the player's interaction data
		PlayerInteractionData data = RecipeInteractionDataMap.computeIfAbsent(playerUUID, k -> new PlayerInteractionData());
		
		long currentTime = System.currentTimeMillis();
		// Check if a full second has passed since the last tracked event
		if ((currentTime - data.lastInteractionTime > MyConfig.TIME_WINDOW_MS) && (data.interactionCount > 0)) {
			// Reset the counter and update the timestamp
			// System.out.println("Player " + serverPlayer.getName().getString() + " crafting counter reset.");
			data.interactionCount = 0;
			data.lastInteractionTime = currentTime;
			return;
		}
		data.interactionCount++;
		if (data.interactionCount > MyConfig.getmaxCraftRecipesPerSecond() *  MyConfig.NINE_SLOTS) {

			MyLogger.logItem(serverPlayer, " Disconnected for Rapid Crafting Recipe Spamming.", true);
			
			serverPlayer.connection.disconnect(
					net.minecraft.network.chat.Component.literal("Disconnected for Rapid Crafting Recipe Spamming."));

			// Cancel the original method to stop the malicious behavior
			ci.cancel();
			return;
		}


		// Increment the count and check if the player is exceeding the limit



	}

}