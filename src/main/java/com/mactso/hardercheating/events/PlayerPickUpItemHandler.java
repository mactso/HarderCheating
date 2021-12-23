package com.mactso.hardercheating.events;

import java.io.File;

import com.mactso.hardercheating.Main;

import net.minecraft.world.item.ItemStack;
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
		System.out.println("player picked up stack");
//		stack.setCount(64);
		if (stack.getCount() > stack.getMaxStackSize()) {
			System.out.println("stack size violation");
			// event to check when player picks up a stack
			// log violation
			// optionally fix error
		}
	}
}
