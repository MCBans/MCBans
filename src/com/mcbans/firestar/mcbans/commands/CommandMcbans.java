package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.callBacks.ManualResync;
import com.mcbans.firestar.mcbans.callBacks.ManualSync;
import com.mcbans.firestar.mcbans.callBacks.MessageCallback;
import com.mcbans.firestar.mcbans.callBacks.ServerChoose;
import com.mcbans.firestar.mcbans.exception.CommandException;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.PingRequest;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandMcbans extends BaseCommand{
    public CommandMcbans(){
        bePlayer = false;
        name = "mcbans";
        argLength = 0;
        usage = "show information";
        banning = false;
    }

    @Override
    public void execute() throws CommandException {
        /* General Help */
        if (args.size() == 0){
            send("&bMCBans &3" + plugin.getDescription().getVersion() + "&b Help &f|| &b<> &f= required, &b[] &f= optional");
            send("&f/mcbans banning" + ChatColor.BLUE + " Help with banning/unban command");
            send("&f/mcbans user" + ChatColor.BLUE + " Help with user management commands");
            send("&f/mcbans get" + ChatColor.BLUE + " Get time till next call");
            send("&f/mcbans ping" + ChatColor.BLUE + " Check overall response time from API");
            send("&f/mcbans sync" + ChatColor.BLUE + " Force a sync to occur");
            send("&f/mcbans reload" + ChatColor.BLUE + " Reload settings and language file");
            return;
        }

        final String first = args.remove(0);
        /* Banning Help */
        if (first.equalsIgnoreCase("banning")){
            send("&f/ban <name> [reason]" + ChatColor.BLUE + " Local ban user");
            send("&f/ban <name> g <reason>" + ChatColor.BLUE + " Global ban user");
            send("&f/ban <name> t <time> <m, h, d, w> <reason>" + ChatColor.BLUE + " Temporarily ban");
            send("&f/tban <name> <time> <m(minute), h(hour), d(day), w(week)> [reason]" + ChatColor.BLUE + " Temp ban user");
            send("&f/gban <name> <reason>" + ChatColor.BLUE + " Global ban user");
            send("&f/rban <name> [reason]" + ChatColor.BLUE + " Rollback and local ban");
            send("&f/rban <name> g <reason>" + ChatColor.BLUE + " Rollback and global ban");
            send("&f/rban <name> t <time> <m, h, d, w> <reason>" + ChatColor.BLUE + " Rollback and temporarily ban");
            send("&f/banip <ip> [reason]" + ChatColor.BLUE + " Bans an IP address");
            send("&f/unban <name|ip>" + ChatColor.BLUE + " Bans an IP address");
            return;
        }
        /* User Help */
        if (first.equalsIgnoreCase("user")){
            send("&f/lookup <name>" + ChatColor.BLUE + " Lookup the player information");
            send("&f/banlookup <banID>" + ChatColor.BLUE + " Lookup the ban information");
            send("&f/altlookup <name>" + ChatColor.BLUE + " Lookup the alt information");
            send("&f/kick <name> [reason]" + ChatColor.BLUE + " Kick user from the server");
            return;
        }
        /* Check response time */
        if (first.equalsIgnoreCase("ping")){
            if (!Perms.ADMIN.has(sender)){
                throw new CommandException(ChatColor.RED + _("permissionDenied"));
            }
            PingRequest request = new PingRequest(plugin, new MessageCallback(plugin, sender));
            (new Thread(request)).start();
            return;
        }
        /* Sync banned-players.txt */
        if (first.equalsIgnoreCase("sync")){
            if (!Perms.ADMIN.has(sender)){
                throw new CommandException(ChatColor.RED + _("permissionDenied"));
            }

            // Check if all sync
            if (args.size() > 0 && args.get(0).equalsIgnoreCase("all")){
                send(ChatColor.GREEN + " Re-Sync has started!");
                ManualResync manualSyncBanRunner = new ManualResync(plugin, senderName);
                (new Thread(manualSyncBanRunner)).start();
            }else{
                long syncInterval = 60 * config.getSyncInterval();
                if(syncInterval < (60 * 5)){ // minimum 5 minutes
                    syncInterval = 60 * 5;
                }
                long ht = (plugin.lastSync + syncInterval) - (System.currentTimeMillis() / 1000);
                if (ht > 10) {
                    send(ChatColor.GREEN + " Sync has started!");
                    ManualSync manualSyncBanRunner = new ManualSync(plugin, senderName);
                    (new Thread(manualSyncBanRunner)).start();
                } else {
                    throw new CommandException(ChatColor.RED + "[Unable] Sync will occur in less than 10 seconds!");
                }
            }
            return;
        }
        /* Get next scheduling time */
        if (first.equalsIgnoreCase("get")){
            if (args.size() > 0 && args.get(0).equalsIgnoreCase("call")){
                long callBackInterval = 0;
                callBackInterval = 60 * config.getCallBackInterval();
                if(callBackInterval < (60 * 15)){
                    callBackInterval = (60 * 15);
                }
                String r = this.timeRemain( (plugin.lastCallBack + callBackInterval) - (System.currentTimeMillis() / 1000) );
                send(ChatColor.GOLD + r + " until next callback request.");
            }
            else if (args.size() > 0 && args.get(0).equalsIgnoreCase("sync")){
                if (config.isEnableAutoSync()){
                    long syncInterval = 60 * config.getSyncInterval();
                    if(syncInterval < (60 * 5)){
                        syncInterval = (60 * 5);
                    }
                    String r = this.timeRemain( (plugin.lastSync + syncInterval) - (System.currentTimeMillis() / 1000) );
                    send(ChatColor.GOLD + r + " until next sync.");
                }else{
                    send(ChatColor.RED + "Auto sync is disabled by config.yml!");
                }
            }
            else{
                send(ChatColor.WHITE + "/mcbans get call" + ChatColor.BLUE + " Time until callback thread sends data.");
                send(ChatColor.WHITE + "/mcbans get sync" + ChatColor.BLUE + " Time until next sync.");
            }
            return;
        }
        /* Reload plugin */
        if (first.equalsIgnoreCase("reload")){
            if (!Perms.ADMIN.has(sender)){
                throw new CommandException(ChatColor.RED + _("permissionDenied"));
            }

            send(ChatColor.AQUA + "Reloading Settings..");
            try{
                config.loadConfig(false);
                send(ChatColor.GREEN + "Reload completed!");
            }catch (Exception ex){
                send(ChatColor.RED + "An error occured while trying to load the config file.");
            }
            send(ChatColor.AQUA + "Reloading Language File..");
            try{
                I18n.setCurrentLanguage(config.getLanguage());
                send(ChatColor.GREEN + "Reload completed!");
            }catch(Exception ex){
                send(ChatColor.RED + "An error occured while trying to load the language file.");
            }
            ServerChoose serverChooser = new ServerChoose(plugin);
            (new Thread(serverChooser)).start();
            return;
        }
        /* for MCBans Mod */
        if (first.equalsIgnoreCase("staff") && player != null && plugin.mcbStaff.contains(player.getName())){
            if (args.size() > 0 && args.get(0).equalsIgnoreCase("perms")){
                send("&6-=== All Online Players Perms ===-");
                for (Perms perm : Perms.values()){
                    send("&6" + perm.getNode() + ": &e" + Util.join(perm.getPlayerNames(), ", "));
                }
            }else if (args.size() > 0 && args.get(0).equalsIgnoreCase("debug")){
                send("&6-=== Debug Information ===-)");
                send("&6CraftBukkit: &e" + Bukkit.getVersion());
                send("&6Bukkit: &e" + Bukkit.getBukkitVersion());
                send("&6connData.size: &e" + plugin.connectionData.size() + "&6 pCache.size: &e" + plugin.playerCache.size() + "&6 resetTime.size: &e" + plugin.resetTime.size());
                send("&6ApiServer: &e" + plugin.apiServer + " &6last_req: &e" + plugin.last_req + " &6last_sync: &e" + plugin.lastSync);
                send("&6timeRecieved: &e" + plugin.timeRecieved + " &6syncRunning: &e" + plugin.syncRunning + " &6lastID: &e" + plugin.lastID);
                send("&6NCP: &e" + plugin.isEnabledNCP() + " &6AC: &e" + plugin.isEnabledAC());
            }else{
                send("&6-=== Server Settings ===-)");
                send("&6ValidApiKey: &e" + config.isValidApiKey() + "&6 PermissionCtrl: &e" + config.getPermission());
                send("&6MinRep: &e" + config.getMinRep() + "&6 AutoSync: &e" + config.isEnableAutoSync());
                send("&6Max Alts: &e" + config.isEnableMaxAlts() + " (" + config.getMaxAlts() +")");
                send("&6Failsafe: &e" + config.isFailsafe() + "&6 isDebug: &e" + config.isDebug() + "&6 Log: &e" + config.isEnableLog());
                
                send("&6-=== Server Status ===-)");
                send("&6MCBans Plugin: &e" + plugin.getDescription().getVersion());
                send("&6Name: &e" + Bukkit.getServerName() + "&6 IP: &e" + Bukkit.getServer().getIp() + ":" + Bukkit.getServer().getPort());
                send("&6OnlineMode: &e" + Bukkit.getOnlineMode());
                
                send("&6-=== Online Players ===-");
                send("&6mcbans.admin: &e" + Util.join(Perms.ADMIN.getPlayerNames(), ", "));
                send("&6mcbans.ban.global: &e" + Util.join(Perms.BAN_GLOBAL.getPlayerNames(), ", "));
            }
            return;
        }

        // Format error
        throw new CommandException(ChatColor.RED + _("formatError"));
    }

    private void send(final String msg){
        Util.message(sender, Util.color(msg));
    }

    private String timeRemain(long remain) {
        try {
            String format = "";
            long timeRemaining = remain;
            long sec = timeRemaining % 60;
            long min = (timeRemaining / 60) % 60;
            long hours = (timeRemaining / (60 * 60)) % 24;
            long days = (timeRemaining / (60 * 60 * 24)) % 7;
            long weeks = (timeRemaining / (60 * 60 * 24 * 7));
            if (sec != 0) {
                format = sec + " seconds";
            }
            if (min != 0) {
                format = min + " minutes " + format;
            }
            if (hours != 0) {
                format = hours + " hours " + format;
            }
            if (days != 0) {
                format = days + " days " + format;
            }
            if (weeks != 0) {
                format = weeks + " weeks " + format;
            }
            return format;
        } catch (ArithmeticException e) {
            return "";
        }
    }

    @Override
    public boolean permission(CommandSender sender) {
        return true;
    }
}
