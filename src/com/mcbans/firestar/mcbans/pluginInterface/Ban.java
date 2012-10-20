package com.mcbans.firestar.mcbans.pluginInterface;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.events.PlayerBanEvent;
import com.mcbans.firestar.mcbans.events.PlayerBannedEvent;
import com.mcbans.firestar.mcbans.events.PlayerGlobalBanEvent;
import com.mcbans.firestar.mcbans.events.PlayerLocalBanEvent;
import com.mcbans.firestar.mcbans.events.PlayerTempBanEvent;
import com.mcbans.firestar.mcbans.events.PlayerUnbanEvent;
import com.mcbans.firestar.mcbans.events.PlayerUnbannedEvent;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;

import de.diddiz.LogBlock.BlockChange;
import de.diddiz.LogBlock.QueryParams;
import de.diddiz.LogBlock.QueryParams.BlockChangeType;


import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.*;

public class Ban implements Runnable {
    private BukkitInterface MCBans;
    private String PlayerName = null;
    private String PlayerIP = null;
    private String PlayerAdmin = null;
    private String Reason = null;
    private String Action = null;
    private int rollbackTime = 20;
    private String Duration = null;
    private String Measure = null;
    private boolean Rollback = false;
    private String Badword = null;
    private JSONObject ActionData = new JSONObject();
    private HashMap<String, Integer> responses = new HashMap<String, Integer>();
    private int action_id;

    public Ban(BukkitInterface p, String action, String playerName, String playerIP, String playerAdmin, String reason, String duration,
            String measure) {
        MCBans = p;
        PlayerName = playerName;
        PlayerIP = playerIP;
        PlayerAdmin = playerAdmin;
        Reason = reason;
        Duration = duration;
        Rollback = MCBans.Settings.getBoolean("rollbackOnBan");
        rollbackTime = MCBans.Settings.getInteger("backDaysAgo");
        Measure = measure;
        Action = action;
        responses.put("globalBan", 0);
        responses.put("localBan", 1);
        responses.put("tempBan", 2);
        responses.put("unBan", 3);
    }

    public Ban(BukkitInterface p, String action, String playerName, String playerIP, String playerAdmin, String reason, String duration,
            String measure, JSONObject actionData, boolean rollback) {
        MCBans = p;
        PlayerName = playerName;
        PlayerIP = playerIP;
        PlayerAdmin = playerAdmin;
        Reason = reason;
        Rollback = rollback;
        rollbackTime = MCBans.Settings.getInteger("backDaysAgo");
        Duration = duration;
        Measure = measure;
        Action = action;
        ActionData = actionData;
        responses.put("globalBan", 0);
        responses.put("localBan", 1);
        responses.put("tempBan", 2);
        responses.put("unBan", 3);
    }

    public Ban(BukkitInterface p, String action, String playerName, String playerIP, String playerAdmin, String reason, String duration,
            String measure, JSONObject actionData, int rollback) {
        MCBans = p;
        PlayerName = playerName;
        PlayerIP = playerIP;
        PlayerAdmin = playerAdmin;
        Reason = reason;
        Rollback = true;
        rollbackTime = rollback;
        Duration = duration;
        Measure = measure;
        Action = action;
        ActionData = actionData;
        responses.put("globalBan", 0);
        responses.put("localBan", 1);
        responses.put("tempBan", 2);
        responses.put("unBan", 3);
    }

    public void kickPlayer(String playerToKick, final String kickString) {
        final Player target = MCBans.getServer().getPlayer(playerToKick);
        if (target != null) {
            MCBans.getServer().getScheduler().scheduleSyncDelayedTask(MCBans, new Runnable() {
                @Override
                public void run() {
                    target.kickPlayer(kickString);
                }
            }, 1L);
        }
    }

    @Override
    public void run() {
        while (MCBans.notSelectedServer) {
            // waiting for server select
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (MCBans.Settings.getBoolean("isDebug")) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if (responses.containsKey(Action)) {
                action_id = responses.get(Action);

                // Call BanEvent
                if (action_id != 3){
                    PlayerBanEvent banEvent = new PlayerBanEvent(PlayerName, PlayerIP, PlayerAdmin, Reason, action_id, Duration, Measure);
                    MCBans.getServer().getPluginManager().callEvent(banEvent);
                    if (banEvent.isCancelled()){
                        return;
                    }
                    PlayerAdmin = banEvent.getSenderName();
                    Reason = banEvent.getReason();
                    action_id = banEvent.getActionID();
                    Duration = banEvent.getDuration();
                    Measure = banEvent.getMeasure();
                }

                switch (action_id) {
                case 0:
                    globalBan();
                    break;
                case 1:
                    localBan();
                    break;
                case 2:
                    tempBan();
                    break;
                case 3:
                    unBan();
                    break;
                }
            } else {
                MCBans.log("Error, caught invalid action! Another plugin using mcbans improperly?");
            }
        } catch (NullPointerException e) {
            if (MCBans.Settings.getBoolean("isDebug")) {
                e.printStackTrace();
            }
        }
    }

    public void unBan() {
        // Call PlayerUnbanEvent
        PlayerUnbanEvent unBanEvent = new PlayerUnbanEvent(PlayerName, PlayerAdmin);
        MCBans.getServer().getPluginManager().callEvent(unBanEvent);
        if (unBanEvent.isCancelled()){
            return;
        }
        PlayerAdmin = unBanEvent.getSenderName();

        JsonHandler webHandle = new JsonHandler(MCBans);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", PlayerName);
        url_items.put("admin", PlayerAdmin);
        url_items.put("exec", "unBan");
        HashMap<String, String> response = webHandle.mainRequest(url_items);
        try {
            if (!response.containsKey("result")) {
                MCBans.broadcastPlayer(PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat("unBanMessageError", PlayerName, PlayerAdmin));
                return;
            }
            if (response.get("result").equals("y")) {
                OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
                if (d.isBanned()) {
                    d.setBanned(false);
                }
                MCBans.log(PlayerAdmin + " unbanned " + PlayerName + "!");
                MCBans.broadcastPlayer(PlayerAdmin, ChatColor.GREEN + MCBans.Language.getFormat("unBanMessageSuccess", PlayerName, PlayerAdmin));
                MCBans.getServer().getPluginManager().callEvent(new PlayerUnbannedEvent(PlayerName, PlayerAdmin));
                return;
            } else if (response.get("result").equals("e")) {
                MCBans.broadcastPlayer(PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat("unBanMessageError", PlayerName, PlayerAdmin));
            } else if (response.get("result").equals("s")) {
                MCBans.broadcastPlayer(PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat("unBanMessageGroup", PlayerName, PlayerAdmin));
            } else if (response.get("result").equals("n")) {
                MCBans.broadcastPlayer(PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat("unBanMessageNot", PlayerName, PlayerAdmin));
            }
            MCBans.log(PlayerAdmin + " tried to unban " + PlayerName + "!");
        } catch (NullPointerException e) {
            if (MCBans.Settings.getBoolean("isDebug")) {
                e.printStackTrace();
            }
        }
    }

    public void localBan() {
        // Call PlayerLocalBanEvent
        PlayerLocalBanEvent lBanEvent = new PlayerLocalBanEvent(PlayerName, PlayerIP, PlayerAdmin, Reason);
        MCBans.getServer().getPluginManager().callEvent(lBanEvent);
        if (lBanEvent.isCancelled()){
            return;
        }
        PlayerAdmin = lBanEvent.getSenderName();
        Reason = lBanEvent.getReason();

        JsonHandler webHandle = new JsonHandler(MCBans);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", PlayerName);
        url_items.put("playerip", PlayerIP);
        url_items.put("reason", Reason);
        url_items.put("admin", PlayerAdmin);
        if (Rollback) {
            MCBans.getRbHandler().rollback(PlayerAdmin, PlayerName);
        }
        if (ActionData != null) {
            url_items.put("actionData", ActionData.toString());
        }
        url_items.put("exec", "localBan");
        HashMap<String, String> response = webHandle.mainRequest(url_items);
        try {
            if (!response.containsKey("result")) {
                MCBans.broadcastPlayer(PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
                OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
                if (!d.isBanned()) {
                    d.setBanned(true);
                }
                this.kickPlayer(PlayerName, MCBans.Language.getFormat("localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP));
                // MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED +
                // MCBans.Language.getFormat( "localBanMessageError",
                // PlayerName, PlayerAdmin, Reason, PlayerIP ) );
                return;
            }
            if (response.get("result").equals("y")) {
                MCBans.log(PlayerName + " has been banned with a local type ban [" + Reason + "] [" + PlayerAdmin + "]!");
                this.kickPlayer(PlayerName, MCBans.Language.getFormat("localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP));
                MCBans.broadcastAll(ChatColor.GREEN + MCBans.Language.getFormat("localBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP));
                MCBans.getServer().getPluginManager().callEvent(new PlayerBannedEvent(PlayerName, PlayerIP, PlayerAdmin, Reason, action_id, Duration, Measure));
                return;
            } else if (response.get("result").equals("e")) {
                MCBans.broadcastPlayer(PlayerAdmin,
                        ChatColor.DARK_RED + MCBans.Language.getFormat("localBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP));
            } else if (response.get("result").equals("s")) {
                MCBans.broadcastPlayer(PlayerAdmin,
                        ChatColor.DARK_RED + MCBans.Language.getFormat("localBanMessageGroup", PlayerName, PlayerAdmin, Reason, PlayerIP));
            } else if (response.get("result").equals("a")) {
                MCBans.broadcastPlayer(PlayerAdmin,
                        ChatColor.DARK_RED + MCBans.Language.getFormat("localBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP));
            }
            MCBans.log(PlayerAdmin + " has tried to ban " + PlayerName + " with a local type ban [" + Reason + "]!");
        } catch (NullPointerException e) {
            MCBans.broadcastPlayer(PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
            OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
            if (!d.isBanned()) {
                d.setBanned(true);
            }
            this.kickPlayer(PlayerName, MCBans.Language.getFormat("localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP));
            if (MCBans.Settings.getBoolean("isDebug")) {
                e.printStackTrace();
            }
        }
    }

    public void globalBan() {
        // Call PlayerGlobalBanEvent
        PlayerGlobalBanEvent gBanEvent = new PlayerGlobalBanEvent(PlayerName, PlayerIP, PlayerAdmin, Reason);
        MCBans.getServer().getPluginManager().callEvent(gBanEvent);
        if (gBanEvent.isCancelled()){
            return;
        }
        PlayerAdmin = gBanEvent.getSenderName();
        Reason = gBanEvent.getReason();

        JsonHandler webHandle = new JsonHandler(MCBans);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", PlayerName);
        url_items.put("playerip", PlayerIP);
        url_items.put("reason", Reason);
        url_items.put("admin", PlayerAdmin);
        if (Rollback) {
            MCBans.getRbHandler().rollback(PlayerAdmin, PlayerName);
        }
        if (ActionData.length() > 0) {
            url_items.put("actionData", ActionData.toString());
        }
        url_items.put("exec", "globalBan");
        HashMap<String, String> response = webHandle.mainRequest(url_items);
        try {
            if (!response.containsKey("result")) {
                MCBans.broadcastPlayer(PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
                OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
                if (!d.isBanned()) {
                    d.setBanned(true);
                }
                this.kickPlayer(PlayerName, MCBans.Language.getFormat("localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP));
                // MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED +
                // MCBans.Language.getFormat( "globalBanMessageError",
                // PlayerName, PlayerAdmin, Reason, PlayerIP ) );
                return;
            }
            if (response.get("result").equals("y")) {
                MCBans.log(PlayerName + " has been banned with a global type ban [" + Reason + "] [" + PlayerAdmin + "]!");
                this.kickPlayer(PlayerName, MCBans.Language.getFormat("globalBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP));
                MCBans.broadcastAll(ChatColor.GREEN + MCBans.Language.getFormat("globalBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP));
                MCBans.getServer().getPluginManager().callEvent(new PlayerBannedEvent(PlayerName, PlayerIP, PlayerAdmin, Reason, action_id, Duration, Measure));
                return;
            } else if (response.get("result").equals("e")) {
                MCBans.broadcastPlayer(PlayerAdmin,
                        ChatColor.DARK_RED + MCBans.Language.getFormat("globalBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP));
            } else if (response.get("result").equals("w")) {
                Badword = response.get("word");
                MCBans.broadcastPlayer(PlayerAdmin,
                        ChatColor.DARK_RED + MCBans.Language.getFormat("globalBanMessageWarning", PlayerName, PlayerAdmin, Reason, PlayerIP, Badword));
            } else if (response.get("result").equals("s")) {
                MCBans.broadcastPlayer(PlayerAdmin,
                        ChatColor.DARK_RED + MCBans.Language.getFormat("globalBanMessageGroup", PlayerName, PlayerAdmin, Reason, PlayerIP));
            } else if (response.get("result").equals("a")) {
                MCBans.broadcastPlayer(PlayerAdmin,
                        ChatColor.DARK_RED + MCBans.Language.getFormat("globalBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP));
            }
            MCBans.log(PlayerAdmin + " has tried to ban " + PlayerName + " with a global type ban [" + Reason + "]!");
        } catch (NullPointerException e) {
            MCBans.broadcastPlayer(PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
            OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
            if (!d.isBanned()) {
                d.setBanned(true);
            }
            this.kickPlayer(PlayerName, MCBans.Language.getFormat("localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP));
            if (MCBans.Settings.getBoolean("isDebug")) {
                e.printStackTrace();
            }
        }
    }

    public void tempBan() {
        // Call PlayerTempBanEvent
        PlayerTempBanEvent tBanEvent = new PlayerTempBanEvent(PlayerName, PlayerIP, PlayerAdmin, Reason, Duration, Measure);
        MCBans.getServer().getPluginManager().callEvent(tBanEvent);
        if (tBanEvent.isCancelled()){
            return;
        }
        PlayerAdmin = tBanEvent.getSenderName();
        Reason = tBanEvent.getReason();
        Duration = tBanEvent.getDuration();
        Measure = tBanEvent.getMeasure();

        JsonHandler webHandle = new JsonHandler(MCBans);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", PlayerName);
        url_items.put("playerip", PlayerIP);
        url_items.put("reason", Reason);
        url_items.put("admin", PlayerAdmin);
        url_items.put("duration", Duration);
        url_items.put("measure", Measure);
        if (MCBans.Settings.getBoolean("enableTempBanRollback")) {
            MCBans.getRbHandler().rollback(PlayerAdmin, PlayerName);
        }
        if (ActionData != null) {
            url_items.put("actionData", ActionData.toString());
        }
        url_items.put("exec", "tempBan");
        HashMap<String, String> response = webHandle.mainRequest(url_items);
        try {
            if (!response.containsKey("result")) {
                MCBans.broadcastPlayer(PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
                OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
                if (!d.isBanned()) {
                    d.setBanned(true);
                }
                this.kickPlayer(PlayerName, MCBans.Language.getFormat("localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP));
                // MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED +
                // MCBans.Language.getFormat( "tempBanMessageError", PlayerName,
                // PlayerAdmin, Reason, PlayerIP ) );
                return;
            }
            if (response.get("result").equals("y")) {
                MCBans.log(PlayerName + " has been banned with a temp type ban [" + Reason + "] [" + PlayerAdmin + "]!");
                this.kickPlayer(PlayerName, MCBans.Language.getFormat("tempBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP));
                MCBans.broadcastAll(ChatColor.GREEN + MCBans.Language.getFormat("tempBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP));
                MCBans.getServer().getPluginManager().callEvent(new PlayerBannedEvent(PlayerName, PlayerIP, PlayerAdmin, Reason, action_id, Duration, Measure));
                return;
            } else if (response.get("result").equals("e")) {
                MCBans.broadcastPlayer(PlayerAdmin,
                        ChatColor.DARK_RED + MCBans.Language.getFormat("tempBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP));
            } else if (response.get("result").equals("s")) {
                MCBans.broadcastPlayer(PlayerAdmin,
                        ChatColor.DARK_RED + MCBans.Language.getFormat("tempBanMessageGroup", PlayerName, PlayerAdmin, Reason, PlayerIP));
            } else if (response.get("result").equals("a")) {
                MCBans.broadcastPlayer(PlayerAdmin,
                        ChatColor.DARK_RED + MCBans.Language.getFormat("tempBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP));
            }
            MCBans.log(PlayerAdmin + " has tried to ban " + PlayerName + " with a temp type ban [" + Reason + "]!");
        } catch (NullPointerException e) {
            MCBans.broadcastPlayer(PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
            OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
            if (!d.isBanned()) {
                d.setBanned(true);
            }
            this.kickPlayer(PlayerName, MCBans.Language.getFormat("localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP));
            if (MCBans.Settings.getBoolean("isDebug")) {
                e.printStackTrace();
            }
        }
    }

    public void rollback() {

    }
}