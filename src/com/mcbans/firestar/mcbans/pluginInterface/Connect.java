package com.mcbans.firestar.mcbans.pluginInterface;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.log.LogLevels;
import org.bukkit.ChatColor;

import java.util.HashMap;

public class Connect implements Runnable {
    private BukkitInterface MCBans;
    private String PlayerIP;
    private String PlayerName;

    public void ConnectSet(BukkitInterface p, String pn, String pi) {
        MCBans = p;
        PlayerIP = pi;
        PlayerName = pn;
    }

    @Override
    public void run() {
        HashMap<String,String> pcache = MCBans.playerCache.get(PlayerName);
        MCBans.playerCache.remove(PlayerName);
        if(pcache.containsKey("b")){
            MCBans.broadcastPlayer(PlayerName, ChatColor.DARK_RED + "You have bans on record! ( check http://mcbans.com )" );
            MCBans.broadcastJoinView( ChatColor.DARK_RED + MCBans.Language.getFormat( "previousBans", PlayerName ) );
        }
        if(pcache.containsKey("d")){
            MCBans.broadcastPlayer(PlayerName, ChatColor.DARK_RED + pcache.get("d") + " open disputes!" );
        }
        if(pcache.containsKey("a")){
            MCBans.broadcastAltView( ChatColor.DARK_PURPLE + MCBans.Language.getFormatAlts( "altAccounts", PlayerName, pcache.get("al").toString() ));
        }
        if(pcache.containsKey("m")){
            MCBans.log( LogLevels.INFO, PlayerName + " is a MCBans.com Staff member");
            MCBans.broadcastJoinView( ChatColor.AQUA + MCBans.Language.getFormat( "isMCBansMod", PlayerName ), PlayerName);
            MCBans.broadcastPlayer(PlayerName, ChatColor.AQUA + MCBans.Language.getFormat ("youAreMCBansStaff"));
        }
        /*while (MCBans.notSelectedServer) {
            // waiting for server select
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        JsonHandler webHandle = new JsonHandler(MCBans);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", PlayerName);
        url_items.put("playerip", PlayerIP);
        url_items.put("exec", "playerConnect");
        JSONObject response = webHandle.hdl_jobj(url_items);
        try {
            if (!response.has("banStatus")) {
            } else {
                if (MCBans.Settings.getBoolean("onJoinMCBansMessage")) {
                    MCBans.broadcastPlayer(PlayerName, ChatColor.DARK_GREEN + "Server secured by MCBans!");
                }
                switch (ConnectStatus.valueOf(response.get("banStatus").toString().toUpperCase())) {
                case N:
                    if (response.has("is_mcbans_mod")) {
                        if (response.get("is_mcbans_mod").equals("y")) {
                            MCBans.log(LogLevels.INFO, PlayerName + " is an MCBans.com Staff member");
                            MCBans.broadcastJoinView(ChatColor.AQUA + MCBans.Language.getFormat("isMCBansMod", PlayerName), PlayerName);
                            MCBans.broadcastPlayer(PlayerName, ChatColor.AQUA + MCBans.Language.getFormat("youAreMCBansStaff"));
                        }
                    }
                    if (response.has("disputeCount")) {
                        if (!response.get("disputeCount").equals("")) {
                            MCBans.broadcastPlayer(PlayerName, ChatColor.DARK_RED + response.get("disputeCount").toString() + " open disputes!");
                        }
                    }
                    if (response.has("connectMessage")) {
                        if (!response.get("connectMessage").equals("")) {
                            MCBans.broadcastPlayer(PlayerName, ChatColor.AQUA + response.get("connectMessage").toString());
                        }
                    }
                    if (response.has("altList") && !MCBans.Permissions.isAllow(PlayerName, "alt.hide")) {
                        if (!response.get("altList").equals("")) {
                            MCBans.broadcastAltView(ChatColor.DARK_PURPLE
                                    + MCBans.Language.getFormatAlts("altAccounts", PlayerName, response.get("altList").toString()));
                        }
                    }
                    MCBans.log(PlayerName + " has connected!");
                    break;
                case B:
                    // MCBans.broadcastJoinView( ChatColor.DARK_RED +
                    // MCBans.Language.getFormat( "previousBans", PlayerName
                    // ) );
                    MCBans.log(PlayerName + " has connected!");
                    String[] out = null;
                    if (response.getJSONArray("globalBans").length() > 0 && MCBans.Settings.getBoolean("onConnectGlobals")) {
                        MCBans.broadcastJoinView("Player " + ChatColor.DARK_AQUA + PlayerName + ChatColor.WHITE + " has " + ChatColor.DARK_RED
                                + response.getString("totalBans") + " ban(s)" + ChatColor.WHITE + " and " + ChatColor.BLUE
                                + response.getString("playerRep") + " REP" + ChatColor.WHITE + ".");
                        MCBans.broadcastJoinView("--------------------------");
                        if (response.getJSONArray("globalBans").length() > 0) {
                            for (int v = 0; v < response.getJSONArray("globalBans").length(); v++) {
                                out = response.getJSONArray("globalBans").getString(v).split(" .:. ");
                                if (out.length == 2) {
                                    MCBans.broadcastJoinView(ChatColor.LIGHT_PURPLE + out[0]);
                                    MCBans.broadcastJoinView("\\---\"" + ChatColor.DARK_PURPLE + out[1] + "\"");
                                }
                            }
                        }
                        MCBans.broadcastJoinView("--------------------------");
                    }
                    if (response.has("altList") && !MCBans.Permissions.isAllow(PlayerName, "alt.hide")) {
                        if (!response.get("altList").equals("")) {
                            MCBans.broadcastAltView(ChatColor.DARK_PURPLE
                                    + MCBans.Language.getFormatAlts("altAccounts", PlayerName, response.get("altList").toString()));
                        }
                    }
                    if (response.has("disputeCount")) {
                        if (!response.get("disputeCount").equals("")) {
                            MCBans.broadcastPlayer(PlayerName, ChatColor.DARK_RED + response.get("disputeCount").toString() + " open disputes!");
                        }
                    }
                    if (response.has("connectMessage")) {
                        if (!response.get("connectMessage").equals("")) {
                            MCBans.broadcastPlayer(PlayerName, ChatColor.AQUA + response.get("connectMessage").toString());
                        }
                    }
                    MCBans.broadcastPlayer(PlayerName, ChatColor.DARK_RED + "You have bans on record! ( check http://mcbans.com )");
                    if (MCBans.Settings.getBoolean("isDebug")) {
                        System.out.print("Player Rep: " + Float.parseFloat(response.get("playerRep").toString()));
                    }
                    if (response.has("is_mcbans_mod")) {
                        if (response.get("is_mcbans_mod").equals("y")) {
                            MCBans.log(LogLevels.INFO, PlayerName + " is an MCBans.com Staff member");
                            MCBans.broadcastBanView(ChatColor.AQUA + MCBans.Language.getFormat("isMCBansMod", PlayerName));
                            MCBans.broadcastPlayer(PlayerName, ChatColor.AQUA + MCBans.Language.getFormat("youAreMCBansStaff"));
                        }
                    }
                    break;
                }
            }
        } catch (JSONException e) {
            if (response.toString().contains("error")) {
                if (response.toString().contains("Server Disabled")) {
                    MCBans.broadcastBanView(ChatColor.RED + "Server Disabled by an MCBans Admin");
                    MCBans.broadcastBanView("MCBans is running in reduced functionality mode. Only local bans can be used at this time.");
                    MCBans.log(LogLevels.SEVERE, "The server API key has been disabled by an MCBans Administrator");
                    MCBans.log(LogLevels.SEVERE, "To appeal this decision, please contact an administrator");
                }
            } else {
                MCBans.log(LogLevels.SEVERE, "JSON error while trying to parse join data!");
            }
        } catch (NullPointerException e) {
            // e.printStackTrace();
        }*/
    }
}