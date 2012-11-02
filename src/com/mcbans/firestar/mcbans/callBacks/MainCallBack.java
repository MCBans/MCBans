package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.log.LogLevels;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.JsonHandler;


public class MainCallBack implements Runnable {
    private final BukkitInterface plugin;
    public long last_req=0;
    public MainCallBack(BukkitInterface plugin){
        this.plugin = plugin;
    }
    @Override
    public void run(){
        int callBackInterval = ((60*1000)*plugin.settings.getInteger("callBackInterval"));
        if(callBackInterval<((60*1000)*15)){
            callBackInterval=((60*1000)*15);
        }

        while(true){
            while(plugin.notSelectedServer){
                //waiting for server select
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            this.mainRequest();
            plugin.lastCallBack = System.currentTimeMillis()/1000;
            try {
                Thread.sleep(callBackInterval);
            } catch (InterruptedException e) {
            }
        }
    }
    public void goRequest() {
        mainRequest();
    }
    private void mainRequest(){
        JsonHandler webHandle = new JsonHandler( plugin );
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put( "maxPlayers", String.valueOf( plugin.getServer().getMaxPlayers() ) );
        //url_items.put( "playerList", this.playerList() );
        url_items.put( "version", plugin.getDescription().getVersion() );
        url_items.put( "exec", "callBack" );
        HashMap<String, String> response = webHandle.mainRequest(url_items);
        try {
            if(response.containsKey("hasNotices")) {
                for(String cb : response.keySet()) {
                    if (cb.contains("notice")) {
                        Perms.VIEW_BANS.message(ChatColor.GOLD + "Notice: " + ChatColor.WHITE + response.get(cb));
                        plugin.log(LogLevels.INFO, "MCBans Notice: " + response.get(cb));
                    }
                }
            }
        } catch (NullPointerException e) {
            if(plugin.settings.getBoolean("isDebug")){
                e.printStackTrace();
            }
        }
    }
    /*private String playerList(){
		StringBuilder playerList=new StringBuilder();
		for(Player player: MCBans.getServer().getOnlinePlayers()){
			if(playerList.length()>0){
				playerList.append(",");
			}
			playerList.append(player.getName());
		}
		return playerList.toString();
	}*/
}