package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.h31ix.anticheat.api.AnticheatAPI;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.MCBans;
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
import com.mcbans.firestar.mcbans.util.Util;
import static com.mcbans.firestar.mcbans.I18n._;

import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.checks.ViolationHistory.ViolationLevel;

public class Ban implements Runnable {
    private MCBans plugin;
    private String playerName = null;
    private String playerIP = null;
    private String senderName = null;
    private String reason = null;
    private String action = null;
    private String duration = null;
    private String measure = null;
    private boolean rollback = false;
    private String badword = null;
    private JSONObject actionData = null;
    private HashMap<String, Integer> responses = new HashMap<String, Integer>();
    private int action_id;

    public Ban(MCBans plugin, String action, String playerName, String playerIP, String senderName, String reason, String duration,
            String measure, JSONObject actionData, boolean rollback) {
        this.plugin = plugin;
        this.playerName = playerName;
        this.playerIP = playerIP;
        this.senderName = senderName;
        this.reason = reason;
        this.rollback = rollback;
        this.duration = duration;
        this.measure = measure;
        this.action = action;
        this.actionData = (actionData != null) ? actionData : new JSONObject();

        responses.put("globalBan", 0);
        responses.put("localBan", 1);
        responses.put("tempBan", 2);
        responses.put("unBan", 3);
    }

    public Ban(MCBans plugin, String action, String playerName, String playerIP, String senderName, String reason, String duration,
            String measure) {
        this (plugin, action, playerName, playerIP, senderName, reason, duration, measure, null, false);
    }

    /**
     * @deprecated Use another constructor. This constructor will be removed on future release.
     */
    @Deprecated
    public Ban(MCBans plugin, String action, String playerName, String playerIP, String senderName, String reason, String duration,
            String measure, JSONObject actionData, int rollback_dummy) {
        this (plugin, action, playerName, playerIP, senderName, reason, duration, measure, actionData, true);
    }

    public void kickPlayer(String playerToKick, final String kickString) {
        final Player target = plugin.getServer().getPlayer(playerToKick);
        if (target != null) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    target.kickPlayer(kickString);
                }
            }, 1L);
        }
    }

    @Override
    public void run() {
        while (plugin.notSelectedServer) {
            // waiting for server select
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (plugin.getConfigs().isDebug()) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if (responses.containsKey(action)) {
                action_id = responses.get(action);

                // Call BanEvent
                if (action_id != 3){
                    PlayerBanEvent banEvent = new PlayerBanEvent(playerName, playerIP, senderName, reason, action_id, duration, measure);
                    plugin.getServer().getPluginManager().callEvent(banEvent);
                    if (banEvent.isCancelled()){
                        return;
                    }
                    senderName = banEvent.getSenderName();
                    reason = banEvent.getReason();
                    action_id = banEvent.getActionID();
                    duration = banEvent.getDuration();
                    measure = banEvent.getMeasure();
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
                plugin.log("Error, caught invalid action! Another plugin using mcbans improperly?");
            }
        } catch (NullPointerException e) {
            if (plugin.getConfigs().isDebug()) {
                e.printStackTrace();
            }
        }
    }

    public void unBan() {
        // Call PlayerUnbanEvent
        PlayerUnbanEvent unBanEvent = new PlayerUnbanEvent(playerName, senderName);
        plugin.getServer().getPluginManager().callEvent(unBanEvent);
        if (unBanEvent.isCancelled()){
            return;
        }
        senderName = unBanEvent.getSenderName();

        JsonHandler webHandle = new JsonHandler(plugin);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", playerName);
        url_items.put("admin", senderName);
        url_items.put("exec", "unBan");
        HashMap<String, String> response = webHandle.mainRequest(url_items);
        try {
            if (response.containsKey("error")){
                Util.message(senderName, ChatColor.DARK_RED + "Error: " + response.get("error"));
                return;
            }
            if (!response.containsKey("result")) {
                Util.message(senderName, ChatColor.DARK_RED + _("unBanMessageError").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName));
                return;
            }
            if (response.get("result").equals("y")) {
                OfflinePlayer d = plugin.getServer().getOfflinePlayer(playerName);
                if (d.isBanned()) {
                    d.setBanned(false);
                }
                plugin.log(senderName + " unbanned " + playerName + "!");
                Util.message(senderName, ChatColor.GREEN + _("unBanMessageSuccess").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName));
                plugin.getServer().getPluginManager().callEvent(new PlayerUnbannedEvent(playerName, senderName));
                return;
            } else if (response.get("result").equals("e")) {
                Util.message(senderName, ChatColor.DARK_RED + _("unBanMessageError").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName));
            } else if (response.get("result").equals("s")) {
                Util.message(senderName, ChatColor.DARK_RED + _("unBanMessageGroup").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName));
            } else if (response.get("result").equals("n")) {
                Util.message(senderName, ChatColor.DARK_RED + _("unBanMessageNot").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName));
            }
            plugin.log(senderName + " tried to unban " + playerName + "!");
        } catch (NullPointerException e) {
            if (plugin.getConfigs().isDebug()) {
                e.printStackTrace();
            }
        }
    }

    public void localBan() {
        // Call PlayerLocalBanEvent
        PlayerLocalBanEvent lBanEvent = new PlayerLocalBanEvent(playerName, playerIP, senderName, reason);
        plugin.getServer().getPluginManager().callEvent(lBanEvent);
        if (lBanEvent.isCancelled()){
            return;
        }
        senderName = lBanEvent.getSenderName();
        reason = lBanEvent.getReason();

        JsonHandler webHandle = new JsonHandler(plugin);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", playerName);
        url_items.put("playerip", playerIP);
        url_items.put("reason", reason);
        url_items.put("admin", senderName);
        if (rollback) {
            plugin.getRbHandler().rollback(senderName, playerName);
        }
        if (actionData != null) {
            url_items.put("actionData", actionData.toString());
        }
        url_items.put("exec", "localBan");
        HashMap<String, String> response = webHandle.mainRequest(url_items);
        try {
            if (response.containsKey("error")){
                Util.message(senderName, ChatColor.DARK_RED + "Error: " + response.get("error"));
                return;
            }
            if (!response.containsKey("result")) {
                Util.message(senderName, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
                OfflinePlayer d = plugin.getServer().getOfflinePlayer(playerName);
                if (!d.isBanned()) {
                    d.setBanned(true);
                }
                this.kickPlayer(playerName, _("localBanMessagePlayer").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));

                return;
            }
            if (response.get("result").equals("y")) {
                plugin.log(playerName + " has been banned with a local type ban [" + reason + "] [" + senderName + "]!");
                this.kickPlayer(playerName, _("localBanMessagePlayer").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
                Util.broadcastMessage(ChatColor.GREEN + _("localBanMessageSuccess").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
                plugin.getServer().getPluginManager().callEvent(new PlayerBannedEvent(playerName, playerIP, senderName, reason, action_id, duration, measure));
                return;
            } else if (response.get("result").equals("e")) {
                Util.message(senderName,
                        ChatColor.DARK_RED + _("localBanMessageError").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            } else if (response.get("result").equals("s")) {
                Util.message(senderName,
                        ChatColor.DARK_RED + _("localBanMessageGroup").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            } else if (response.get("result").equals("a")) {
                Util.message(senderName,
                        ChatColor.DARK_RED + _("localBanMessageAlready").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            }
            plugin.log(senderName + " has tried to ban " + playerName + " with a local type ban [" + reason + "]!");
        } catch (NullPointerException e) {
            Util.message(senderName, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
            OfflinePlayer d = plugin.getServer().getOfflinePlayer(playerName);
            if (!d.isBanned()) {
                d.setBanned(true);
            }
            this.kickPlayer(playerName, _("localBanMessagePlayer").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                    .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            if (plugin.getConfigs().isDebug()) {
                e.printStackTrace();
            }
        }
    }

    public void globalBan() {
        // Call PlayerGlobalBanEvent
        PlayerGlobalBanEvent gBanEvent = new PlayerGlobalBanEvent(playerName, playerIP, senderName, reason);
        plugin.getServer().getPluginManager().callEvent(gBanEvent);
        if (gBanEvent.isCancelled()){
            return;
        }
        senderName = gBanEvent.getSenderName();
        reason = gBanEvent.getReason();

        JsonHandler webHandle = new JsonHandler(plugin);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", playerName);
        url_items.put("playerip", playerIP);
        url_items.put("reason", reason);
        url_items.put("admin", senderName);

        // Put proof
        try{
            for (Map.Entry<String, JSONObject> proof : getProof().entrySet()){
                actionData.put(proof.getKey(), proof.getValue());
            }
        }catch (JSONException ex){
            if (plugin.getConfigs().isDebug()) {
                ex.printStackTrace();
            }
        }

        if (rollback) {
            plugin.getRbHandler().rollback(senderName, playerName);
        }
        if (actionData.length() > 0) {
            url_items.put("actionData", actionData.toString());
        }
        url_items.put("exec", "globalBan");
        HashMap<String, String> response = webHandle.mainRequest(url_items);
        try {
            if (response.containsKey("error")){
                Util.message(senderName, ChatColor.DARK_RED + "Error: " + response.get("error"));
                return;
            }
            if (!response.containsKey("result")) {
                Util.message(senderName, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
                OfflinePlayer d = plugin.getServer().getOfflinePlayer(playerName);
                if (!d.isBanned()) {
                    d.setBanned(true);
                }
                this.kickPlayer(playerName, _("localBanMessagePlayer").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
                return;
            }
            if (response.get("result").equals("y")) {
                plugin.log(playerName + " has been banned with a global type ban [" + reason + "] [" + senderName + "]!");
                this.kickPlayer(playerName, _("globalBanMessagePlayer").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
                Util.broadcastMessage(ChatColor.GREEN + _("globalBanMessageSuccess").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
                plugin.getServer().getPluginManager().callEvent(new PlayerBannedEvent(playerName, playerIP, senderName, reason, action_id, duration, measure));
                return;
            } else if (response.get("result").equals("e")) {
                Util.message(senderName,
                        ChatColor.DARK_RED + _("globalBanMessageError").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            } else if (response.get("result").equals("w")) {
                badword = response.get("word");
                Util.message(senderName,
                        ChatColor.DARK_RED + _("globalBanMessageWarning").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP).replaceAll(I18n.BADWORD, badword));
            } else if (response.get("result").equals("s")) {
                Util.message(senderName,
                        ChatColor.DARK_RED + _("globalBanMessageGroup").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            } else if (response.get("result").equals("a")) {
                Util.message(senderName,
                        ChatColor.DARK_RED + _("globalBanMessageAlready").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            }
            plugin.log(senderName + " has tried to ban " + playerName + " with a global type ban [" + reason + "]!");
        } catch (NullPointerException e) {
            Util.message(senderName, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
            OfflinePlayer d = plugin.getServer().getOfflinePlayer(playerName);
            if (!d.isBanned()) {
                d.setBanned(true);
            }
            this.kickPlayer(playerName, _("localBanMessagePlayer").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                    .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            if (plugin.getConfigs().isDebug()) {
                e.printStackTrace();
            }
        }
    }

    public void tempBan() {
        // Call PlayerTempBanEvent
        PlayerTempBanEvent tBanEvent = new PlayerTempBanEvent(playerName, playerIP, senderName, reason, duration, measure);
        plugin.getServer().getPluginManager().callEvent(tBanEvent);
        if (tBanEvent.isCancelled()){
            return;
        }
        senderName = tBanEvent.getSenderName();
        reason = tBanEvent.getReason();
        duration = tBanEvent.getDuration();
        measure = tBanEvent.getMeasure();

        JsonHandler webHandle = new JsonHandler(plugin);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", playerName);
        url_items.put("playerip", playerIP);
        url_items.put("reason", reason);
        url_items.put("admin", senderName);
        url_items.put("duration", duration);
        url_items.put("measure", measure);
        if (plugin.getConfigs().isEnableRollbackTempBan()) {
            plugin.getRbHandler().rollback(senderName, playerName);
        }
        if (actionData != null) {
            url_items.put("actionData", actionData.toString());
        }
        url_items.put("exec", "tempBan");
        HashMap<String, String> response = webHandle.mainRequest(url_items);
        try {
            if (response.containsKey("error")){
                Util.message(senderName, ChatColor.DARK_RED + "Error: " + response.get("error"));
                return;
            }
            if (!response.containsKey("result")) {
                Util.message(senderName, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
                OfflinePlayer d = plugin.getServer().getOfflinePlayer(playerName);
                if (!d.isBanned()) {
                    d.setBanned(true);
                }
                this.kickPlayer(playerName, _("localBanMessagePlayer").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
                // MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED +
                // MCBans.Language.getFormat( "tempBanMessageError", PlayerName,
                // PlayerAdmin, Reason, PlayerIP ) );
                return;
            }
            if (response.get("result").equals("y")) {
                plugin.log(playerName + " has been banned with a temp type ban [" + reason + "] [" + senderName + "]!");
                this.kickPlayer(playerName, _("tempBanMessagePlayer").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
                Util.broadcastMessage(ChatColor.GREEN + _("tempBanMessageSuccess").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
                plugin.getServer().getPluginManager().callEvent(new PlayerBannedEvent(playerName, playerIP, senderName, reason, action_id, duration, measure));
                return;
            } else if (response.get("result").equals("e")) {
                Util.message(senderName,
                        ChatColor.DARK_RED + _("tempBanMessageError").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            } else if (response.get("result").equals("s")) {
                Util.message(senderName,
                        ChatColor.DARK_RED + _("tempBanMessageGroup").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            } else if (response.get("result").equals("a")) {
                Util.message(senderName,
                        ChatColor.DARK_RED + _("tempBanMessageAlready").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                        .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            }
            plugin.log(senderName + " has tried to ban " + playerName + " with a temp type ban [" + reason + "]!");
        } catch (NullPointerException e) {
            Util.message(senderName, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
            OfflinePlayer d = plugin.getServer().getOfflinePlayer(playerName);
            if (!d.isBanned()) {
                d.setBanned(true);
            }
            this.kickPlayer(playerName, _("localBanMessagePlayer").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName)
                    .replaceAll(I18n.REASON, reason).replaceAll(I18n.PLAYERIP, playerIP));
            if (plugin.getConfigs().isDebug()) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, JSONObject> getProof() throws JSONException{
        HashMap<String, JSONObject> ret = new HashMap<String, JSONObject>();

        /* Hacked client */
        // No catch PatternSyntaxException. This exception thrown when compiling invalid regex.
        // In this case, regex is constant string. Next line is wrong if throw this. So should output full exception message.
        Pattern regex = Pattern.compile("(fly|hack|nodus|glitch|exploit|NC|cheat|nuker|x-ray|xray)");
        boolean foundMatch = regex.matcher(reason).find();

        if (foundMatch) {
            Player p = plugin.getServer().getPlayerExact(playerName);
            if (p != null) playerName = p.getName();
            // NoCheatPlus
            if (plugin.isEnabledNCP()) {
                ViolationHistory history = ViolationHistory.getHistory(playerName, false);
                if (history != null){
                    // found player history
                    final ViolationLevel[] violations = history.getViolationLevels();
                    JSONObject tmp = new JSONObject();
                    for (ViolationLevel vl : violations){
                        tmp.put(vl.check, String.valueOf(Math.round(vl.sumVL)));
                    }
                    ret.put("nocheatplus", tmp);
                    //ActionData.put("nocheatplus", tmp); // don't put directly
                }
            }
            // AntiCheat
            if (plugin.isEnabledAC() && p.getPlayer() != null) {
                JSONObject tmp = new JSONObject();
                final int level = AnticheatAPI.getLevel(p);
                final boolean xray = AnticheatAPI.isXrayer(p);
                // TODO: Detail proof. Refer AntiCheat CommandHandler:
                // https://github.com/h31ix/AntiCheat/blob/master/src/main/java/net/h31ix/anticheat/CommandHandler.java
                if (level > 0) tmp.put("hack level", String.valueOf(level));
                if (xray) tmp.put("detected x-ray", "true");
                if (tmp.length() > 0) ret.put("anticheat", tmp);
            }
        }

        return ret;
    }
}