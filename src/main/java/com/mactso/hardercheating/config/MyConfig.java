package com.mactso.hardercheating.config;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.hardercheating.Main;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
	public class MyConfig
	{
		
		private static final Logger LOGGER = LogManager.getLogger();
		public static final Common COMMON;
		public static final ForgeConfigSpec COMMON_SPEC;
		static
		{

			final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
			COMMON_SPEC = specPair.getRight();
			COMMON = specPair.getLeft();
		}

		public static int maxClickDistance;
		public static String[] validModSet;
		private static boolean fixBadStacks;
		
		public static int getMaxClickDistance() {
			return maxClickDistance;
		}

		public static boolean isValidMod(String classname)
		{	
			for (String mod : validModSet) {
				if (classname.contains(mod)) {
					return true;
				}
			}
			return false;
		}

		@SubscribeEvent
		public static void onModConfigEvent(final ModConfigEvent configEvent)
		{
			if (configEvent.getConfig().getSpec() == MyConfig.COMMON_SPEC)
			{
				bakeConfig();
			}
		}

		public static void bakeConfig()
		{
			validModSet = extract(COMMON.ValidModSet.get());
			fixBadStacks  = COMMON.fixBadStacks.get();
			maxClickDistance= COMMON.maxClickDistance.get();
		}



		private static String[] extract(List<? extends String> value)
		{
			return value.toArray(new String[value.size()]);
		}



		public static boolean isFixBadStacks() {
			return fixBadStacks;
		}



		public static class Common
		{
			public final ConfigValue<List<? extends String>> ValidModSet;
			public final BooleanValue fixBadStacks;
			public final IntValue maxClickDistance;
			
			public Common(ForgeConfigSpec.Builder builder)
			{
				List<String> includeModsList = Arrays.asList("minecraft","ironchest");

				
				String baseTrans = Main.MODID + ".config.";
				String sectionTrans;

				sectionTrans = baseTrans + "general.";

				maxClickDistance = builder.comment("Legal Max Click Distance (6 to 64) Default 12.")
						.translation(Main.MODID + ".config." + "maxClickDistance").defineInRange("maxClickDistance", () -> 6, 12, 64);
				
				
				fixBadStacks = builder.comment("fixBadStacks")
						.translation(Main.MODID + ".config." + "fixBadStacks")
						.define("fixBadStacks", true);
				
				ValidModSet = builder
						.comment("Checked Mods Name List")
						.translation(sectionTrans + "valid_mod_set")
						.defineList("Valid Mod Set", includeModsList, Common::isString);
			}

			public IntValue getClickDistance() {
				return maxClickDistance;
			}

			public static boolean isString(Object o)
			{
				return (o instanceof String);
			}
		}
	
	}

