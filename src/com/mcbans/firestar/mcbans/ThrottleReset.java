package com.mcbans.firestar.mcbans;

import java.util.Map;

import com.mcbans.firestar.mcbans.bukkitInterface;

public class ThrottleReset implements Runnable {
	private bukkitInterface MCBans;
	
	public ThrottleReset(bukkitInterface iface) {
		MCBans = iface;
	}
	
	public void run() {
		long timeInMillis = System.currentTimeMillis();
		
		if (MCBans.resetTime == null) return;
		
		for (Map.Entry<String, Long> entry : MCBans.resetTime.entrySet()) {
			if (timeInMillis >= entry.getValue()) {
				MCBans.resetTime.remove(entry.getKey());
				MCBans.connectionData.remove(entry.getKey());
				MCBans.log.write("Resetting throttle timer for " + entry.getKey());
			}
		}
	}
}
