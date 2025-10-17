package com.mactso.hardercheating.myconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.hardercheating.Main;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MyConfig {

	public static final int NINE_SLOTS = 9;
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	public static final long TIME_WINDOW_MS = 1000;
	private static final int DEFAULT_CATEGORY_LIMIT = 256;

	static {

		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	// Logging / Enforcement
	private static int debugLevel;
	private static boolean logActivity;
	private static boolean banPlayer;
	private static int banDays;

	// Feature Flags
	private static boolean fixBadStacks;
	private static boolean checkMaxClicksPerSecond;
	private static boolean checkMaxRecipesPerSecond;
	private static boolean checkMaxEntities;
	private static boolean saveUncommonItems;
	private static boolean saveRareItems;
	private static boolean saveEpicItems;

	// Limits
	private static int chunkEntityLimit;
	private static int ticksBetweenEntityCountAudits;
	private static int maxClickDistance;
	private static int maxClicksPerSecond;
	private static int maxCraftRecipesPerSecond;

	// Inclusion and Exclusion Lists.
	private static String[] unblockableMods;
	private static String[] maxCategoryCountsString;

	private static Map<MobCategory, Integer> maxCategoryCounts = new EnumMap<>(MobCategory.class);
	private static String[] unblockableItemTagsString;
	private static List<TagKey<Item>> unblockableItemTags = new ArrayList<>();
	private static String[] unblockableItemsString;
	private static List<Item> unblockableItems = new ArrayList<>();
	private static String[] blockableItemsString;
	private static List<Item> blockableItems = new ArrayList<>();

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfigEvent configEvent) {
		if (configEvent.getConfig().getSpec() == MyConfig.COMMON_SPEC) {
			bakeConfig();
		}
	}

	public static void bakeConfig() {

		// Logging / Enforcement
		debugLevel = COMMON.debugLevel.get();
		logActivity = COMMON.logActivity.get();
		banPlayer = COMMON.banPlayer.get();
		banDays = COMMON.banDays.get();

		// Feature Flags
		fixBadStacks = COMMON.fixBadStacks.get();
		saveUncommonItems = COMMON.saveUncommonItems.get();
		saveRareItems = COMMON.saveRareItems.get();
		saveEpicItems = COMMON.saveEpicItems.get();

		checkMaxEntities = COMMON.checkMaxEntities.get();
		checkMaxClicksPerSecond = COMMON.checkMaxClicksPerSecond.get();
		checkMaxRecipesPerSecond = COMMON.checkMaxRecipesPerSecond.get();

		// Limits
		chunkEntityLimit = COMMON.chunkEntityLimit.get();
		ticksBetweenEntityCountAudits = COMMON.ticksBetweenEntityCountAudits.get();
		maxClickDistance = COMMON.maxClickDistance.get();
		maxClicksPerSecond = COMMON.maxClicksPerSecond.get();
		maxCraftRecipesPerSecond = COMMON.maxCraftRecipesPerSecond.get();

		// Inclusion and Exclusion Lists.
		unblockableMods = extract(COMMON.unblockableModsStringList.get());
		unblockableItemTagsString = extract(COMMON.unblockableItemTagStringList.get());
		initUnblockableItemTags(unblockableItemTagsString);
		unblockableItemsString = extract(COMMON.unblockableItemStringList.get());
		initUnblockableItems(unblockableItemsString);
		blockableItemsString = extract(COMMON.blockableItemsStringList.get());
		initBlockableItems(blockableItemsString);
		maxCategoryCountsString = extract(COMMON.maxCategoryCountsStringList.get());
		initMaxCategoryCounts(maxCategoryCountsString);

	}

	private static void initMaxCategoryCounts(String[] s) {
		maxCategoryCounts.clear();

		if (s.length == 0) {
			LOGGER.error("Harder Cheating: Max Category Counts not configured. ");
			return;
		}

		for (String pair : s) {
			String[] kv = pair.split(",");
			if (kv.length == 2) {
				try {
					MobCategory category = MobCategory.valueOf(kv[0].trim().toUpperCase());
					int limit = Integer.parseInt(kv[1].trim());
					maxCategoryCounts.put(category, limit);
				} catch (Exception e) {
					LOGGER.error("Harder Cheating: Bad category configuration entry: " + kv[0].trim().toUpperCase());
				}
			}

		}
		// Fill missing categories with default
		for (MobCategory c : MobCategory.values()) {
			maxCategoryCounts.putIfAbsent(c, DEFAULT_CATEGORY_LIMIT);
		}

	}

	private static void initUnblockableItems(String[] s) {

		unblockableItems.clear();

		for (String rlStr : s) {
			if (rlStr.isEmpty())
				continue; // Skip empty strings from multiple commas

			try {
				ResourceLocation rl = ResourceLocation.parse(rlStr);
				Item item = BuiltInRegistries.ITEM.getValue(rl);
				if (item != null && item != net.minecraft.world.item.Items.AIR) {
					unblockableItems.add(item);
				} else {
					LOGGER.warn("Harder Cheating: Could not find or register Item for resource location: " + rlStr);
				}
			} catch (Exception e) {
				LOGGER.error("Harder Cheating: Invalid Resource Location format for unblockable item: " + rlStr);
			}
		}

	}

	private static void initBlockableItems(String[] s) {

		blockableItems.clear();

		for (String rlStr : s) {
			if (rlStr.isEmpty())
				continue; // Skip empty strings from multiple commas

			try {
				ResourceLocation rl = ResourceLocation.parse(rlStr);
				Item item = BuiltInRegistries.ITEM.getValue(rl);
				if (item != null && item != net.minecraft.world.item.Items.AIR) {
					blockableItems.add(item);
				} else {
					LOGGER.warn("Harder Cheating: Could not find or register Item for resource location: " + rlStr);
				}
			} catch (Exception e) {
				LOGGER.error("Harder Cheating: Invalid Resource Location format for unblockable item: " + rlStr);
			}
		}

	}

	private static void initUnblockableItemTags(String[] s) {

		if (unblockableItemTags != null) {
			unblockableItemTags.clear();
		}
		for (String rlStr : s) {
			rlStr = rlStr.trim();
			if (rlStr.startsWith("#")) {
				rlStr = rlStr.substring(1);
			}
			try {
				ResourceLocation rl = ResourceLocation.parse(rlStr);
				TagKey<Item> itemTagKey = TagKey.create(Registries.ITEM, rl);
				unblockableItemTags.add(itemTagKey);
			} catch (Exception e) {
				LOGGER.error("Harder Cheating: Invalid Resource Location format for unblockable item tag: " + rlStr);
			}

		}

	}

	public static int getDebugLevel() {
		return debugLevel;
	}

	public static void setDebugLevel(int debugLevel) {
		MyConfig.debugLevel = debugLevel;
	}

	public static int getChunkEntityLimit() {
		return chunkEntityLimit;
	}

	public static int getTicksBetweenEntityCountAudits() {
		return ticksBetweenEntityCountAudits;
	}

	public static int getMaxCategoryCount(MobCategory c) {
		return maxCategoryCounts.getOrDefault(c, DEFAULT_CATEGORY_LIMIT);
	}

	public static String[] getUnblockableMods() {
		return unblockableMods;
	}

	public static List<TagKey<Item>> getUnblockableItemTags() {
		return unblockableItemTags;
	}

	public static List<Item> getUnblockableItems() {
		return unblockableItems;
	}

	public static List<Item> getBlockableItems() {
		return blockableItems;
	}

	public static int getMaxClickDistance() {
		return maxClickDistance;
	}

	public static int getMaxClicksPerSecond() {
		return maxClicksPerSecond;
	}

	public static int getMaxCraftRecipesPerSecond() {
		return maxCraftRecipesPerSecond;
	}

	public static int getBanDays() {
		return banDays;
	}

	public static boolean isBanPlayer() {
		return banPlayer;
	}

	public static boolean isLogActivity() {
		return logActivity;
	}

	public static boolean isSaveUncommonItems() {
		return saveUncommonItems;
	}

	public static boolean isSaveRareItems() {
		return saveRareItems;
	}

	public static boolean isSaveEpicItems() {
		return saveEpicItems;
	}

	public static boolean isCheckMaxEntities() {
		return checkMaxEntities;
	}

	public static boolean isCheckMaxClicksPerSecond() {
		return checkMaxClicksPerSecond;
	}

	public static boolean isCheckMaxRecipesPerSecond() {
		return checkMaxRecipesPerSecond;
	}

	public static boolean isValidMod(String classname) {
		for (String mod : unblockableMods) {
			if (classname.contains(mod)) {
				return true;
			}
		}
		return false;
	}

	private static String[] extract(List<? extends String> value) {
		return value.toArray(new String[value.size()]);
	}

	public static boolean isFixBadStacks() {
		return fixBadStacks;
	}

	public static class Common {

		public final IntValue debugLevel;
		public final IntValue ticksBetweenEntityCountAudits;
		public final IntValue chunkEntityLimit;
		public final IntValue maxClickDistance;
		public final IntValue maxClicksPerSecond;
		public final IntValue maxCraftRecipesPerSecond;
		public final IntValue banDays;

		public final ConfigValue<List<? extends String>> unblockableModsStringList;
		public final ConfigValue<List<? extends String>> unblockableItemTagStringList;
		public final ConfigValue<List<? extends String>> unblockableItemStringList;
		public final ConfigValue<List<? extends String>> maxCategoryCountsStringList;
		public final ConfigValue<List<? extends String>> blockableItemsStringList;

		public final BooleanValue fixBadStacks;

		public final BooleanValue banPlayer;
		public final BooleanValue logActivity;
		public final BooleanValue checkMaxEntities;
		public final BooleanValue checkMaxClicksPerSecond;
		public final BooleanValue checkMaxRecipesPerSecond;
		public final BooleanValue saveUncommonItems;
		public final BooleanValue saveRareItems;
		public final BooleanValue saveEpicItems;

		public Common(ForgeConfigSpec.Builder builder) {

			List<String> defaultUnblockableModsList = Arrays.asList("twilightforest", "ironchest");

			// @formatter:off
			 List<String> defaultUnblockableItemTagsList = Arrays.asList(
				        "#minecraft:enchantable/durability",
				        "#minecraft:decorated_pot_sherds",
				        "#minecraft:smithing_templates",
				        "#minecraft:shulker_boxes"
				    );

				    List<String> defaultUnblockableItemsList = Arrays.asList(
				        "minecraft:beacon",
				        "minecraft:dragon_egg",
				        "minecraft:dragon_breath",
				        "minecraft:nether_star",
				        "minecraft:ender_chest",
				        "minecraft:netherite_scrap",
				        "minecraft:ancient_debris",
				        "minecraft:conduit",
				        "minecraft:netherite_upgrade_smithing_template"
				    );

				    List<String> defaultBlockableItemsList = Arrays.asList(
				        "minecraft:diamond_block",
				        "minecraft:emerald_block",
				        "minecraft:gold_ingot",
				        "minecraft:diamond",
				        "minecraft:emerald"
				    );

				    List<String> defaultMaxCategoryCountsList = Arrays.asList(
				        "MONSTER,32",
				        "CREATURE,32",
				        "MISC,64",
				        "AMBIENT,16",
				        "AXOLOTLS,16",
				        "UNDERGROUND_WATER_CREATURE,16",
				        "WATER_AMBIENT,32",
				        "WATER_CREATURE,32"
				    );
			// @formatter:off

			String baseTrans = Main.MODID + ".config.";
			String sectionTrans;

			sectionTrans = baseTrans + "general.";

			// ******************************************************************************************************

			builder.push("General Settings");
			debugLevel = builder.comment("Debug Level: 0 = minimal logging, 1 = more logging , 2 = maximumal logging.")
					.translation(Main.MODID + ".config." + "debugLevel").defineInRange("debugLevel", () -> 0, 0, 2);
			builder.pop();

			// ******************************************************************************************************

			builder.push("Logging and Enforcement");

			logActivity = builder.comment("log activity in /config/harderchating/Activity.log")
					.translation(Main.MODID + ".config.logActivity").define("logActivity", true);

			banPlayer = builder.comment("Flag to Ban players who are disconnected for server ddos attacks")
					.translation(Main.MODID + ".config." + "banPlayer").define("banPlayer", false);

			banDays = builder.comment("Number of days to ban someone.").translation(Main.MODID + ".config." + "banDays")
					.defineInRange("banDays", () -> 7, 1, Integer.MAX_VALUE);

			builder.pop();

			// ******************************************************************************************************

			builder.push("Feature Flags:  True = On, False = Off");

			fixBadStacks = builder.comment("fixBadStacks").translation(Main.MODID + ".config." + "fixBadStacks")
					.define("fixBadStacks", true);

			saveUncommonItems = builder.comment(
					"If False, will not protect uncommon items.  Helpful for duping. Risk of losing player items.")
					.translation(Main.MODID + ".config." + "saveUncommonItems").define("saveUncommonItems", true);

			saveRareItems = builder
					.comment("If False, will not protect rare items.  Helpful for duping. Risk of losing player items.")
					.translation(Main.MODID + ".config." + "saveRareItems").define("saveRareItems", true);

			saveEpicItems = builder
					.comment("If False, will not protect epic items.  Helpful for duping. Risk of losing player items.")
					.translation(Main.MODID + ".config." + "saveRareItems").define("saveRareItems", true);

			checkMaxEntities = builder.comment("checkMaxMonsters").translation(Main.MODID + ".config.checkMaxMonsters")
					.define("checkMaxMonsters", true);
			checkMaxClicksPerSecond = builder.comment("checkMaxClicksPerSecond")
					.translation(Main.MODID + ".config." + "checkMaxClicksPerSecond")
					.define("checkMaxClicksPerSecond", true);
			checkMaxRecipesPerSecond = builder.comment("checkMaxRecipesPerSecond")
					.translation(Main.MODID + ".config.checkMaxRecipesPerSecond")
					.define("checkMaxRecipesPerSecond", true);

			builder.pop();

			// ******************************************************************************************************

			builder.push("Limits : Numerical limit values for various things");

			ticksBetweenEntityCountAudits = builder
					.comment("Chunk entity count audit throttle.  Default is 100 ticks (5 seconds).")
					.translation(Main.MODID + ".config." + "ticksBetweenEntityCountAudits")
					.defineInRange("ticksBetweenEntityCountAudits", () -> 10, 5, Integer.MAX_VALUE);

			chunkEntityLimit = builder.comment(
					"If entities in a chunk (mob and item stacks) are less, skip checking and let entity enter world.")
					.translation(Main.MODID + ".config." + "chunkEntityLimit")
					.defineInRange("chunkEntityLimit", () -> 60, 5, 350);

			maxClicksPerSecond = builder.comment("Legal Max Clicks per Second (15 to 99) Default 20.")
					.translation(Main.MODID + ".config." + "maxClicksPerSecond")
					.defineInRange("maxClicksPerSecond", () -> 32, 15, 99);

			maxCraftRecipesPerSecond = builder
					.comment("Legal Max Crafting Recipe Changes per Second (132 to 512) Default 128.")
					.translation(Main.MODID + ".config." + "maxCraftRecipesPerSecond")
					.defineInRange("maxCraftRecipesPerSecond", () -> 132, 128, 512);

			maxClickDistance = builder.comment("Legal Max Click Distance (6 to 64) Default 12.")
					.translation(Main.MODID + ".config." + "maxClickDistance")
					.defineInRange("maxClickDistance", () -> 12, 6, 64);

			maxCategoryCountsStringList = builder
					.comment("Remove entities if too many in that category.  Format: Category,Integer list")
					.defineList("maxCategoryCountsStringList", defaultMaxCategoryCountsList, Common::isString);

			builder.pop();

			// ******************************************************************************************************

			builder.push("Blockable and Unblockable Lists  (inclusion and exclusion");

			blockableItemsStringList = builder.comment("Blocked regardless of item rarity or on unblockable lists")
					.defineList("blockItemsStringList", defaultBlockableItemsList, Common::isString);

			unblockableModsStringList = builder
					.comment("Unblockable Mods Name List : Items from these mods will not be stopped from entering crowded chunks.")
					.translation(sectionTrans + "valid_mod_set")
					.defineList("unblockableModsStringList", defaultUnblockableModsList, Common::isString);
			
			
			unblockableItemTagStringList = builder.comment("Items in these Item Tags won't be stopped from entering the crowded chunks.")
					.defineList("unblockableItemTagStringList", defaultUnblockableItemTagsList, Common::isString);

			unblockableItemStringList = builder.comment("Itemsin this list won't be stopped from entering the crowded chunks.")
					.defineList("unblockableItemStringList", defaultUnblockableItemsList, Common::isString);

			builder.pop();
		}

		public static boolean isString(Object o) {
			return (o instanceof String);
		}
	}

}
