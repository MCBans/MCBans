package com.mcbans.mcbans;

import java.util.Map;

public class ThrottleReset implements Runnable {
	private BukkitInterface MCBans;
	
	public ThrottleReset(BukkitInterface iface) {
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
