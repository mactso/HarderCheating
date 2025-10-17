package com.mactso.hardercheating.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.mactso.hardercheating.myconfig.MyConfig;

import net.minecraft.server.level.ServerPlayer;

public class MyLogger {

	private static final String LOG_FILE_PATH = "config/hardercheating/activity.log";

	private static PrintStream p = null;
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

	public static void resetLogFile() {

		File logFile = new File(LOG_FILE_PATH);
		logFile.getParentFile().mkdirs();
		
		if (logFile.exists()) {
		    if (!logFile.delete()) {
		        Utility.debugMsg(0,"WARNING: Could not delete existing log file: " + LOG_FILE_PATH);
		    }
		}
	    try {
	        p = new PrintStream(new FileOutputStream(LOG_FILE_PATH, true), true, StandardCharsets.UTF_8);
	    } catch (IOException e) {
	        e.printStackTrace();
	        p = System.out; // Fallback to console
	    }
	    p.println("HarderCheating Activity Log.  If empty, set 'logActivity' to true in the configuration.");
	    p.println("HarderCheating Activity Log.  To suppress, set 'logActivity' to false in the configuration.");		
	}

	public static void closeLog() {
		
	    if (p == null || p == System.out) return;

	    try {
            p.flush();  // Write all pending data to disk.
            p.close();
            
        } catch (Exception e) {
            // Log this critical failure to the standard error stream, 
            // as the file logger itself is failing.
            Utility.debugMsg(0,"Critical error closing log stream: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            p = null;
        }
	    
	}

	public static void flushLog () {
		if (p == null) 
			return;
		p.flush();
	}
	
	public static synchronized void logItem(String s) {
		if (!MyConfig.isLogActivity())
			return;
		LocalDateTime now = LocalDateTime.now();
		String s1 = dtf.format(now) + "  " + s;
		if (p == null) {
	        Utility.debugMsg(0, "Activity Logger not initialized, cannot log: " + s1);
	        return;
	    }
	    p.println(s1);
	}
	
	public static void logItem(ServerPlayer cheater, String violation, boolean header) {
		
		if (!MyConfig.isLogActivity())
			return;
		
		String pos = cheater.blockPosition().toString();
		String name = cheater.getName().getString();

		if (header) 
			logItem ("  (" + String.format("%-20s", pos) + ")  " + String.format("%-16s", name) + ": "
					+ cheater.getStringUUID());
		logItem(String.format("%-16s", name) + ":   " + violation);
	}


}
