package com.mcbans.firestar.mcbans.log;

import com.mcbans.firestar.mcbans.BukkitInterface;

public class Logger {
    private BukkitInterface MCBans = null;

    public Logger(final BukkitInterface p) {
        MCBans = p;
    }

    public void log(final String message) {
        log(LogLevels.NONE, message);
    }

    public void log(final LogLevels type, final String message) {
        switch (type) {
        case INFO:
            System.out.print("[MCBans] [INFO] " + message);
            break;
        case WARNING:
            System.out.print("[MCBans] [WARNING] " + message);
            break;
        case SEVERE:
            System.out.print("[MCBans] [SEVERE] " + message);
            break;
        case FATAL:
            System.out.print("[MCBans] [FATAL] " + message);
            MCBans.getServer().getPluginManager().disablePlugin(MCBans);
            break;
        default:
            System.out.print("[MCBans] " + message);
            break;
        }
    }
}
