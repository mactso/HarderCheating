package com.mactso.hardercheating.events;

import com.mactso.hardercheating.Main;
import com.mactso.hardercheating.util.MyLogger;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class PlayerPickUpItemHandler {

	@SubscribeEvent
	public static void onChange(ItemPickupEvent event )
	{
		ItemStack stack = event.getStack();
		boolean header = true;
		
		if (stack.getCount() > stack.getMaxStackSize()) {
			String temp =  "Player picked up " + String.format("%-20s", stack.getDisplayName().getString()) + " stack size " + stack.getCount();
			MyLogger.logItem((ServerPlayerEntity) event.getPlayer(), temp, header);
			header = false;

		}
	}
}
