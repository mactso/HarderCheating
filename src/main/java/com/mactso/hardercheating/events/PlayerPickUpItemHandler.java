package com.mactso.hardercheating.events;

import com.mactso.hardercheating.Main;
import com.mactso.hardercheating.util.MyLogger;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
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
			MyLogger.logItem((ServerPlayer) event.getEntity(), temp, header);
			header = false;
			// optionally fix error
//			if (MyConfig.isFixBadStacks()) {
//				stack.setCount(stack.getMaxStackSize());
//			}
		}
	}
}
