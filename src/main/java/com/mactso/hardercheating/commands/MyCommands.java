package com.mactso.hardercheating.commands;

import com.mactso.hardercheating.Main;
import com.mactso.hardercheating.myconfig.MyConfig;
import com.mactso.hardercheating.util.Utility;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class MyCommands {
	String subcommand = "";
	String value = "";

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		System.out.println("register Harder Cheating Commands");
		dispatcher.register(Commands.literal(Main.MODID).requires((source) -> {
			return source.hasPermission(2);
		}).then(Commands.literal("debugLevel")
				.then(Commands.argument("debugLevel", IntegerArgumentType.integer(0, 2)).executes(ctx -> {
					ServerPlayer sp = ctx.getSource().getPlayerOrException();
					setDebugLevel(IntegerArgumentType.getInteger(ctx, "debugLevel"));
					doInfo (sp);
					return 1;
				}))).then(Commands.literal("info").executes(ctx -> {
					ServerPlayer sp = ctx.getSource().getPlayerOrException();
					doInfo(sp);
					return 1;
				})));
	}

	private static void doInfo(ServerPlayer sp) {
		Utility.sendBoldChat(sp, "HarderCheating Current Values", ChatFormatting.DARK_GREEN);
		String msg = "  Debug Level...........: " + MyConfig.getDebugLevel();
		Utility.sendChat(sp, msg, ChatFormatting.DARK_GREEN);
	}

	public static int setDebugLevel(int newDebugLevel) {
			MyConfig.setDebugLevel(newDebugLevel);
		return 1;
	}

}
