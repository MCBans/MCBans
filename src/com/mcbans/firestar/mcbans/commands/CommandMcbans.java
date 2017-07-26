package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            send("&f/mcbans banning" + ChatColor.BLUE + " Help with banning/unbanning commands");
            send("&f/mcbans user" + ChatColor.BLUE + " Help with user management commands");
            send("&f/mcbans perms" + ChatColor.BLUE + " Permission list for MCBans");
            send("&f/mcbans get" + ChatColor.BLUE + " Get time till next API call");
            send("&f/mcbans ping" + ChatColor.BLUE + " Check overall response time from API");
            send("&f/mcbans sync" + ChatColor.BLUE + " Force a sync to occur with MCBans API");
            send("&f/mcbans reload" + ChatColor.BLUE + " Reload settings and language files");
            return;
        }

        final String first = args.remove(0);
        /* Banning Help */
        if (first.equalsIgnoreCase("banning")){
        	send("&f------------------------------------------");
            send("&f/ban <name|uuid> [reason]" + ChatColor.BLUE + " Local ban a player");
            send("&f/ban <name|uuid> g <reason>" + ChatColor.BLUE + " Global ban a player");
            send("&f/ban <name|uuid> t <time> <m, h, d, w> <reason>" + ChatColor.BLUE + " Temporarily ban a player");
            send("&f/tban <name|uuid> <time> <m(minute), h(hour), d(day), w(week)> [reason]" + ChatColor.BLUE + " Temp ban a player");
            send("&f/gban <name|uuid> <reason>" + ChatColor.BLUE + " Global ban a player");
            send("&f/rban <name|uuid> [reason]" + ChatColor.BLUE + " Rollback and local ban a player");
            send("&f/rban <name|uuid> g <reason>" + ChatColor.BLUE + " Rollback and global ban a player");
            send("&f/rban <name|uuid> t <time> <m, h, d, w> <reason>" + ChatColor.BLUE + " Rollback and temporarily ban a player");
            send("&f/banip <ip> [reason]" + ChatColor.BLUE + " Bans an IP address");
            send("&f/unban <name|ip|uuid>" + ChatColor.BLUE + " Bans an IP address");
            return;
        }
        /* User Help */
        if (first.equalsIgnoreCase("user")){
        	send("&f------------------------------------------");
            send("&f/lookup <name|uuid>" + ChatColor.BLUE + " Lookup the player ban information");
            send("&f/banlookup <banID>" + ChatColor.BLUE + " Lookup the player ban information");
            send("&f/altlookup <name>" + ChatColor.BLUE + " Lookup the alt account information");
            send("&f/kick <name> [reason]" + ChatColor.BLUE + " Kick player from the server");
            return;
        }
        /* User Help */
        if (first.equalsIgnoreCase("perms")){
        	send("&f------------------------------------------");
        	if(args.size() == 0){
	            send("&f/mcbans perms ban" + ChatColor.BLUE + " Banning/kick permissions");
	            send("&f/mcbans perms exempt" + ChatColor.BLUE + " Exemptions from kick/ban");
	            send("&f/mcbans perms view" + ChatColor.BLUE + " On connect bans/bans/alts");
	            send("&f/mcbans perms others" + ChatColor.BLUE + " Lookups");
	            send(ChatColor.GOLD+"mcbans.admin" + ChatColor.BLUE + " Grants complete MCBans admin permission");
	            return;
        	}
        	final String last = args.remove(0);
        	if(last.equalsIgnoreCase("ban")){
        		send(ChatColor.GOLD+"mcbans.ban.global" + ChatColor.BLUE + " Grants global ban permissions");
        		send(ChatColor.GOLD+"mcbans.ban.local" + ChatColor.BLUE + " Grants local ban permissions");
        		send(ChatColor.GOLD+"mcbans.ban.temp" + ChatColor.BLUE + " Grants temporary ban permissions");
        		send(ChatColor.GOLD+"mcbans.ban.rollback" + ChatColor.BLUE + " Grants rollback ban permissions");
        		send(ChatColor.GOLD+"mcbans.ban.rollback" + ChatColor.BLUE + " Grants rollback ban permissions");
        		send(ChatColor.GOLD+"mcbans.ban.ip" + ChatColor.BLUE + " Grants IP ban permissions");
        		send(ChatColor.GOLD+"mcbans.unban" + ChatColor.BLUE + " Grants unban permissions");
        		send(ChatColor.GOLD+"mcbans.kick" + ChatColor.BLUE + " Grants kick permissions");
        		return;
        	}else if(last.equalsIgnoreCase("view")){
        		send(ChatColor.GOLD+"mcbans.view.alts" + ChatColor.BLUE + " View players alts on connect {premium only}");
        		send(ChatColor.GOLD+"mcbans.view.bans" + ChatColor.BLUE + " View players bans on connect");
        		send(ChatColor.GOLD+"mcbans.view.staff" + ChatColor.BLUE + " View if player is MCBans Staff on connect");
        		send(ChatColor.GOLD+"mcbans.view.previous" + ChatColor.BLUE + " View players previous names on connect");
        		send(ChatColor.GOLD+"mcbans.announce" + ChatColor.BLUE + " View if the player is banned/kicked");
        		return;
        	}else if(last.equalsIgnoreCase("exempt")){
        		send(ChatColor.GOLD+"mcbans.kick.exempt" + ChatColor.BLUE + " Player cannot be kicked at all");
        		send(ChatColor.GOLD+"mcbans.ban.exempt" + ChatColor.BLUE + " Player cannot be banned at all");
        		return;
        	}else if(last.equalsIgnoreCase("others")){
        		send(ChatColor.GOLD+"mcbans.lookup.player" + ChatColor.BLUE + " Grants lookup player command");
        		send(ChatColor.GOLD+"mcbans.lookup.ban" + ChatColor.BLUE + " Grants lookup ban command");
        		send(ChatColor.GOLD+"mcbans.lookup.alt" + ChatColor.BLUE + " Grants lookup alternate accounts command");
        		return;
        	}
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
                send(ChatColor.GREEN + "Resyncing with MCBans API!");
                ManualResync manualSyncBanRunner = new ManualResync(plugin, senderName);
                (new Thread(manualSyncBanRunner)).start();
            }else{
                long syncInterval = 60 * config.getSyncInterval();
                if(syncInterval < (60 * 60)){ // minimum 5 minutes
                    syncInterval = 60 * 60;
                }
                long ht = (plugin.lastSync + syncInterval) - (System.currentTimeMillis() / 1000);
                if (ht > 10) {
                    send(ChatColor.GREEN + "Sync has started! This will take a few seconds to complete.");
                    ManualSync manualSyncBanRunner = new ManualSync(plugin, senderName);
                    (new Thread(manualSyncBanRunner)).start();
                } else {
                    throw new CommandException(ChatColor.RED + "Sync will occur in less than 10 seconds!");
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
                final String remainStr = timeRemain( (plugin.lastCallBack + callBackInterval) - (System.currentTimeMillis() / 1000) );
                if (remainStr != null){
                    send(ChatColor.GOLD + remainStr + " until next callback request.");
                }else{
                    send(ChatColor.GOLD  + "Callback request is in progress..");
                }
            }
            else if (args.size() > 0 && args.get(0).equalsIgnoreCase("sync")){
                if (config.isEnableAutoSync()){
                    long syncInterval = 60 * config.getSyncInterval();
                    if(syncInterval < (60 * 5)){
                        syncInterval = (60 * 5);
                    }
                    final String remainStr = timeRemain( (plugin.lastSync + syncInterval) - (System.currentTimeMillis() / 1000) );
                    if (remainStr != null){
                        send(ChatColor.GOLD + remainStr + " until next sync.");
                    }else{
                        send(ChatColor.GOLD  + "Ban sync is in progress...");
                    }
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
                send(ChatColor.RED + "An error occurred while trying to load the config file.");
            }
            send(ChatColor.AQUA + "Reloading Language File..");
            try{
                I18n.extractLanguageFiles(false);
                I18n.setCurrentLanguage(config.getLanguage());
                send(ChatColor.GREEN + "Reload completed!");
            }catch(Exception ex){
                send(ChatColor.RED + "An error occurred while trying to load the language file.");
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
                send("&6-=== Debug Information ===-");
                send("&6Spigot Version: &e" + Bukkit.getVersion());
                send("&6Build: &e" + Bukkit.getBukkitVersion());
                send("&6connData.size: &e" + plugin.connectionData.size() + "&6 pCache.size: &e" + plugin.playerCache.size() + "&6 resetTime.size: &e" + plugin.resetTime.size());
                send("&6ApiServer: &e" + plugin.apiServer + " &6last_req: &e" + plugin.last_req + " &6last_sync: &e" + plugin.lastSync);
                send("&6timeRecieved: &e" + plugin.timeRecieved + " &6syncRunning: &e" + plugin.syncRunning + " &6lastID: &e" + plugin.lastID);
                send("&6NCP: &e" + plugin.isEnabledNCP() + " &6AC: &e" + plugin.isEnabledAC());
            }else if(args.size() > 0 && args.get(0).equalsIgnoreCase("verify")) {
                //Send to console
                Util.message(Bukkit.getConsoleSender(), ChatColor.AQUA + player.getName() + " is an MCBans Staff member");
            	//All players who should be able to see the message
                Set<Player> players = Perms.VIEW_STAFF.getPlayers();
                players.addAll(Perms.ADMIN.getPlayers());
                players.addAll(Perms.BAN_GLOBAL.getPlayers());
                //Send it
                for (Player p : players){
                    Util.message(p, ChatColor.AQUA + _("isMCBansMod", I18n.PLAYER, player.getName()));
                }
            }else{
                send("&6-=== Server Settings ===-");
                send("&6Valid API Key: &e" + config.isValidApiKey() + "&6 Permissions: &e" + config.getPermission());
                send("&6MinRep: &e" + config.getMinRep() + "&6 AutoSync: &e" + config.isEnableAutoSync());
                send("&6Max Alts: &e" + config.isEnableMaxAlts() + " (" + config.getMaxAlts() +")");
                send("&6Failsafe: &e" + config.isFailsafe() + "&6 isDebug: &e" + config.isDebug() + "&6 Log: &e" + config.isEnableLog());
                
                send("&6-=== Server Status ===-");
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
        if (remain <= 0){
            return null;
        }
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
            if (config.isDebug()){
                e.printStackTrace();
            }
            return "error";
        }
    }

    @Override
    public boolean permission(CommandSender sender) {
        return true;
    }
}
