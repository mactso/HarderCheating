package com.mactso.hardercheating.util;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.entity.LivingEntity;

import com.mactso.hardercheating.myconfig.MyConfig;
import com.mojang.authlib.GameProfile;

public class Utility {
	
	private static final Logger LOGGER = LogManager.getLogger();

	public static void debugMsg(int level, String dMsg) {

		if (MyConfig.getDebugLevel() > level - 1) {
			LOGGER.info("L" + level + ":" + dMsg);
		}

	}

	public static void debugMsg(int level, BlockPos pos, String dMsg) {

		if (MyConfig.getDebugLevel() > level - 1) {
			LOGGER.info("L" + level + " (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "): " + dMsg);
		}

	}

	public static void debugMsg(int level, LivingEntity le, String dMsg) {

		debugMsg(level, le.blockPosition(), dMsg);
	}

	public static void sendBoldChat(ServerPlayer p, String chatMessage, ChatFormatting textColor) {

		MutableComponent component = Component.literal(chatMessage);
		component.setStyle(component.getStyle().withBold(true));
		component.setStyle(component.getStyle().withColor(textColor));
		p.sendSystemMessage(component);


	}

	public static void sendChat(ServerPlayer p, String chatMessage, ChatFormatting textColor) {

		MutableComponent component = Component.literal(chatMessage);
		component.setStyle(component.getStyle().withColor(textColor));
		p.sendSystemMessage(component);

	}
	
    /**
     * Bans the given player permanently (if expires == null) or temporarily, then disconnects them with a message.
     *
     * @param player The player to ban.
     * @param reason The reason for banning.
     * @param expires The expiration date (null = permanent ban).
     */
    public static void banAndKick(ServerPlayer player, String reason, int days) {
        MinecraftServer server = player.level().getServer();  // or player.server
        if (server == null) {
            // Shouldn’t normally happen for a connected player
            return;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        Date expires = cal.getTime();
        
        if (days < 1)  // I don't allow a value below 1 in the configuration currently.  
        	expires = null;

        UserBanList banList = server.getPlayerList().getBans();


		// Create a ban entry for this player
        GameProfile profile = player.getGameProfile();
        NameAndId gp = new NameAndId(profile);

		UserBanListEntry entry = new UserBanListEntry(
        	    gp,          // GameProfile of the player
        	    new Date(),                       // ban start time (now)
        	    "Server",      // source of the ban (operator name or "Server")
        	    expires,                          // expiration date (null = permanent)
        	    reason         // ban reason
        	);

        banList.add(entry);

        // Disconnect the player with a ban message
        String kickMsg = "You have been banned: " + reason;
        player.connection.disconnect(Component.literal(kickMsg));
    }
	

}
