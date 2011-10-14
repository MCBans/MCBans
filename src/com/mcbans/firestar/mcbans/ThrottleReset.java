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
		int deleteCount = 0;
		String[] deletions = new String[MCBans.resetTime.size()];
		
		if (MCBans.resetTime == null) return;
		for (Map.Entry<String, Long> entry : MCBans.resetTime.entrySet()) {
			if (timeInMillis >= entry.getValue()) {
				deletions[deleteCount] = entry.getKey();
				++deleteCount;
			}
		}
		
		if (deleteCount != 0) {
			for(int i = 0; i < deletions.length; i++) { 
				String key = deletions[i];
				if (key != null) {
					MCBans.clearThrottle(key);
					MCBans.log.write("Resetting throttle timer for " + key);
				}
			}
		}
	}
}
