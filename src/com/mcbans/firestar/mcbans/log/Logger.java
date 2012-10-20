package com.mcbans.firestar.mcbans.log;

import com.mcbans.firestar.mcbans.BukkitInterface;

public class Logger {
    private BukkitInterface MCBans = null;

    public Logger(BukkitInterface p) {
        MCBans = p;
    }

    public void log(String message) {
        log(LogLevels.NONE, message);
    }

    public void log(LogLevels type, String message) {
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
            MCBans.getServer().getPluginManager().disablePlugin(MCBans.pluginInterface("mcbans"));
            break;
        default:
            System.out.print("[MCBans] " + message);
            break;
        }
    }
}
