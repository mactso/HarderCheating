package com.mactso.hardercheating.events;


import com.mactso.hardercheating.Main;
import com.mactso.hardercheating.config.MyConfig;
import com.mactso.hardercheating.util.MyLogger;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class OpenPlayerContainerHandler
{
	@SubscribeEvent
	public static void onChange(PlayerContainerEvent event)
	{
		
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity cheater = (ServerPlayerEntity) event.getPlayer();
			boolean header = true;
			String fixed = "";
			if (MyConfig.isFixBadStacks()) {
				fixed = " : Set to legal maximum value.";
			}
			Container container = event.getContainer();
			String name = container.getClass().getName();
			if (MyConfig.isValidMod(name)) {
				// is it an exception container?  If so, exit.
				NonNullList<ItemStack> stacks = container.getItems();

				for (ItemStack stack : stacks) {
					if (stack.isEmpty()) continue;
//					stack.setCount(64); // TODO remove this line.
					if (stack.getCount() > stack.getMaxStackSize()) {
						String temp =   String.format("%-20s", stack.getDisplayName().getString())  + " stack size " + stack.getCount() + fixed;
						MyLogger.logItem((ServerPlayerEntity) cheater, temp, header);
						header = false;
						if (MyConfig.isFixBadStacks()) {
							stack.setCount(stack.getMaxStackSize());
						}
						// optionally turn chest to TNT
						// check for too many valuable items (like diamond blocks)
						// log warning
					}
				}
			}
		}
				
	}
}
