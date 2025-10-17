package com.mactso.hardercheating.events;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mactso.hardercheating.Main;
import com.mactso.hardercheating.myconfig.MyConfig;
import com.mactso.hardercheating.util.MyLogger;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class HandleExcessEntities {

	private static final int TICK_AUDIT_ENTITY_RANDOMIZER= 500;
	private static long tickCheckThrottle = 0;
	private static final Set<EntityType<?>> RARE_MOBS = Set.of(EntityType.CHEST_MINECART, EntityType.LIGHTNING_BOLT,
			EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WARDEN, EntityType.ILLUSIONER // etc.
	);

	@SubscribeEvent
	public static void handleEntityTickExcessEntitiesInChunk(LivingTickEvent event) {

		if (!MyConfig.isCheckMaxEntities()) {
			return;
		}

		Entity entity = event.getEntity();

		if (entity.level().isClientSide())
			return;

		ServerLevel serverLevel = (ServerLevel) entity.level();
		RandomSource rand = serverLevel.getRandom();

		// The probability of any chunk being audited is proportional to the number of
		// living entities in it, since each entity is rolling the die. This weights the
		// audit towards high-density (problematic) chunks.

		if (rand.nextInt(TICK_AUDIT_ENTITY_RANDOMIZER) > 0)
			return;

		long gameTime = serverLevel.getGameTime();
		if (gameTime < tickCheckThrottle + MyConfig.getTicksBetweenEntityCountAudits()) { // default: don't try again
																							// for 250 ticks
			return;
		}

		tickCheckThrottle = gameTime;

		doLogEntityDebugInfo(entity, serverLevel);

		removeExcessEntities(serverLevel, entity.chunkPosition());

	}

	private static String getLogEntityDescription(Entity e) {
		String persistentMob = " (n) ";
	    String cleanEntityName = e.getType().builtInRegistryHolder().key().location().getPath();
	    String tempItem = "";
	    
	    if (e instanceof ItemEntity itemEntity) {
	        tempItem = ", " + itemEntity.getItem().getDisplayName().getString();
	    }
	    
		if (e instanceof Mob me) {
			if (me.isPersistenceRequired()) {
				persistentMob = " (P) ";
			}
		}
	    
	    // Returns " (entity_name, item_name)"
	    return "(" + persistentMob + cleanEntityName + tempItem + ")";
	}
	
	private static String getLogCategoryName(Entity e) {
	    MobCategory category = e.getType().getCategory();
	    String categoryName;
	    
	    if (category == null) {
	        categoryName = "UNDEFINED";
	    } else {
	        categoryName = category.getName().toUpperCase();
	    }
	    
	    return categoryName;
	}
	
	
	private static void doLogEntityDebugInfo(Entity entity, ServerLevel serverLevel) {

		if (MyConfig.getDebugLevel() == 0)
			return;

		String categoryName = getLogCategoryName(entity);
		String cleanEntityDescription = getLogEntityDescription (entity);

		ChunkPos chunkPos = entity.chunkPosition();
		List<Entity> entitiesInChunk = getEntitiesInChunk(serverLevel, chunkPos);
		int totalCount = entitiesInChunk.size();

				
		String message = "Tick Audit: " + categoryName + " Entity " + entity.getId() + " " 
				+ cleanEntityDescription  + " ticking at " + entity.blockPosition() + " with " + totalCount
				+ " chunk entities.";
		MyLogger.logItem(message);
	}

	private static void removeExcessEntities(ServerLevel serverLevel, ChunkPos chunkPos) {

		List<Entity> entitiesInChunk = getEntitiesInChunk(serverLevel, chunkPos);
		if (entitiesInChunk.size() <= MyConfig.getChunkEntityLimit())
			return;

		Map<MobCategory, Integer> countsByCategory = getCountsByCategory(entitiesInChunk);
		doLogCategoryCountsDebugInfo(countsByCategory);
		Collections.shuffle(entitiesInChunk);

		int totalRemoved = 0;

		Map<MobCategory, Integer> removedByCategory = new EnumMap<>(MobCategory.class);

		for (Entity e : entitiesInChunk) {
			MobCategory category = e.getType().getCategory();

			if (category == null) 
				continue;
			
			String categoryName = getLogCategoryName(e);
			String cleanEntityDescription = getLogEntityDescription (e);

			int tempCount = countsByCategory.getOrDefault(category, 0);
			int removedInCategory = removedByCategory.getOrDefault(category, 0);
			if ((tempCount - removedInCategory - 1) < MyConfig.getMaxCategoryCount(category)) {
				continue;
			}

			
			if (MyConfig.getDebugLevel() > 0) {
				String message = "Considering removing " + categoryName + " (" + cleanEntityDescription + " at "
						+ e.blockPosition() + ").";
				MyLogger.logItem(message);
			}
			if (e instanceof Player)
				continue;

			if (isUnblockableEntity(e))
				continue;
			if (e instanceof ItemEntity itemEntity) {
				if (isItemUnblockable(itemEntity))
					continue;
			}

			e.remove(Entity.RemovalReason.DISCARDED);
			removedByCategory.merge(category, 1, Integer::sum);
			totalRemoved++;

			if (MyConfig.getDebugLevel() > 0) {
				String message = "Removed: Entity " + category.getName().toUpperCase() + " (" + cleanEntityDescription
						+ ")" + " at " + e.blockPosition() + " with "
						+ (tempCount - removedByCategory.getOrDefault(category, 0)) + " chunk category entities.";
				MyLogger.logItem(message);
			}
		}

		if (MyConfig.getDebugLevel() > 0) {
			
			if (totalRemoved > 0) {
				String totalMessage = "Removed a total of " + totalRemoved + " entities.";
				MyLogger.logItem(totalMessage);
			}
			
			entitiesInChunk = getEntitiesInChunk(serverLevel, chunkPos);
			countsByCategory = getCountsByCategory(entitiesInChunk);
			
			doLogCategoryCountsDebugInfo(countsByCategory);
			
		}

	}

	public static Map<MobCategory, Integer> getCountsByCategory(List<Entity> entitiesInChunk) {
		Map<MobCategory, Integer> countsByCategory = new EnumMap<>(MobCategory.class);
		for (Entity e : entitiesInChunk) {
			MobCategory tempCategory = e.getType().getCategory();
			if (tempCategory != null) {
				countsByCategory.merge(tempCategory, 1, Integer::sum);
			}
		}
		return countsByCategory;
	}

	public static void doLogCategoryCountsDebugInfo(Map<MobCategory, Integer> countsByCategory) {
		
		if (countsByCategory == null || countsByCategory.isEmpty()) {
			if (MyConfig.getDebugLevel() > 0) {
				String messageNoCategories = "  No categories found.";
				MyLogger.logItem(messageNoCategories);
			}
			return;
		}

		for (Map.Entry<MobCategory, Integer> entry : countsByCategory.entrySet()) {
			if (MyConfig.getDebugLevel() > 0) {
				String messageCategoryCount = "  " + entry.getKey() + ": " + entry.getValue() + " of "
						+ MyConfig.getMaxCategoryCount(entry.getKey());
				MyLogger.logItem(messageCategoryCount);
			}
		}
	}

	@SubscribeEvent
	public static void handleEntityJoinExcessEntitiesInChunk(EntityJoinLevelEvent event) {

		if (!MyConfig.isCheckMaxEntities()) {
			return;
		}

		if (event.getLevel().isClientSide()) {
			return;
		}

		Entity entity = event.getEntity();
		if (isUnblockable(entity))
			return;

		MobCategory category = entity.getType().getCategory();
		ServerLevel serverLevel = (ServerLevel) entity.level();
		List<Entity> entitiesInChunk = getEntitiesInChunk(serverLevel, entity.chunkPosition());
		// 1. Initial Fast Check (The Compromise/Hack)
		int totalCount = entitiesInChunk.size();
		if (totalCount <= MyConfig.getChunkEntityLimit())
			return; // Fast Exit: Don't do the slow category counting

		// 2. Slow, Detailed Category Check (Only run if totalCount is over the rough
		// chunk limit)
		// This will ensure the number of entities in each category is below the
		// category limit.
		
		Map<MobCategory, Integer> countsByCategory = getCountsByCategory(entitiesInChunk);
		
		if (countsByCategory.getOrDefault(category, 0) < MyConfig.getMaxCategoryCount(category))
			return;

		String cleanEntityDescription = getLogEntityDescription(entity);
		String categoryName = getLogCategoryName(entity);

		String messageBlockedEntity = "Blocked Entity " + categoryName + " " + cleanEntityDescription
				+ " trying to join world via Join Event at " + entity.blockPosition() + " with "
				+ totalCount + " chunk entities.";
		MyLogger.logItem(messageBlockedEntity);

		event.setCanceled(true); // block a non-rare excess entity or item entity from entering the world.

	}

	private static boolean isUnblockable(Entity entity) {
		MobCategory category = entity.getType().getCategory();
		if (category == null) {
			return true;
		}

		if (isUnblockableEntity(entity)) { // allow join because rare entity
			return true;
		}

		if (entity instanceof ItemEntity ie) { // allow join because rare / player equipment items;
			if (isItemUnblockable(ie))
				return true; // protect valuable player items
		}

		return false;

	}

	private static List<Entity> getEntitiesInChunk(ServerLevel serverLevel, ChunkPos chunkPos) {
		AABB chunkBounds = new AABB(chunkPos.getMinBlockX(), serverLevel.getMinY(), chunkPos.getMinBlockZ(),
				chunkPos.getMaxBlockX() + 1, serverLevel.getHeight(), chunkPos.getMaxBlockZ() + 1);
		return serverLevel.getEntitiesOfClass(Entity.class, chunkBounds, e -> true);
	}

	private static boolean isUnblockableEntity(Entity entity) {

		if (entity instanceof ServerPlayer) {
			return true;
		}

		if (entity instanceof FallingBlockEntity) {
			return true;
		}

		if (RARE_MOBS.contains(entity.getType())) {
			return true;
		}

		if (entity instanceof InventoryCarrier carrier) { // Creatures that have chests or other inventory
			return true;
		}

		if (entity.hasCustomName()) {
			return true;
		}

		MobCategory category = entity.getType().getCategory();
		if (category == MobCategory.CREATURE) {
			return false;
		}

		if (!(entity instanceof Mob)) {
			return false;
		}

		if (entity instanceof Mob me) {
			if (!me.isPersistenceRequired()) // the mob hasn't picked up items if it isn't persistent.
				return false;
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				ItemStack stack = me.getItemBySlot(slot);
				if (stack.isEmpty())
					continue;
				if (isItemUnblockable(stack)) {
					return true;
				}
			}
		}

		return false;

	}
	

	public static boolean isItemUnblockable(ItemStack stack) {
		Item item = stack.getItem();

		ResourceLocation rl = BuiltInRegistries.ITEM.getKey(item);
		if (rl == null)
			return false; // Treat unknown items as blockable

		List<Item> blockableItems = MyConfig.getUnblockableItems();
		if (blockableItems.contains(stack.getItem())) {
			return true;
		}

		Rarity rarity = stack.getRarity();
		if ((rarity == Rarity.UNCOMMON) && (MyConfig.isSaveUncommonItems())) {
			return true;
		}
		if ((rarity == Rarity.RARE) && (MyConfig.isSaveRareItems())){
			return true;
		} 
		if ((rarity == Rarity.EPIC) && (MyConfig.isSaveEpicItems())) {
			return true;
		}

		List<Item> items = MyConfig.getUnblockableItems();
		if (items.contains(stack.getItem())) {
			return true;
		}

		
		List<TagKey<Item>> itemTags = MyConfig.getUnblockableItemTags();
		for (TagKey<Item> iTag : itemTags) {
			if (stack.is(iTag))
				return true;
		}

		List<Item> unBlockableItems = MyConfig.getUnblockableItems();
		if (unBlockableItems.contains(stack.getItem())) {
			return true;
		}

		return false; // this item can be blocked from entering or removed from the world.

	}

	public static boolean isItemUnblockable(ItemEntity ie) {
		if (ie == null || ie.getItem() == null)
			return true;
		ItemStack stack = ie.getItem();
		return isItemUnblockable(stack);
	}

}
