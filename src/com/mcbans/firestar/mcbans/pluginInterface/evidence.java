package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.logEvidence.MCBansBlockLoggerLogBlock;
import com.mcbans.firestar.mcbans.request.jsonHandler;

public class evidence extends Thread {
	private bukkitInterface MCBans;
	private String PlayerName = null;
	private String PlayerAdmin = null;
	private String World = null;
	private MCBansBlockLoggerLogBlock EvidenceLogger = null;
	public evidence( bukkitInterface p, String playerName, String playerAdmin, String world  ){
		MCBans = p;
		PlayerName = playerName;
		PlayerAdmin = playerAdmin;
		EvidenceLogger = new MCBansBlockLoggerLogBlock(p);
		World = world;
	}
	public void run(){
		submitEvidence();
	}
	public void submitEvidence( ){
		if(MCBans.getServer().getWorld(World)==null){
			MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "evidenceWorldNExist", PlayerName, PlayerAdmin ) );
			return ;
		}
		String changes = EvidenceLogger.getFormattedBlockChangesBy(PlayerName, MCBans.getServer().getWorld(World), false);
		jsonHandler webHandle = new jsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "player", PlayerName );
		url_items.put( "admin", PlayerAdmin );
		url_items.put( "changes", changes );
		url_items.put( "exec", "evidence" );
		webHandle.mainRequest(url_items);
		MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "evidenceSubmitted", PlayerName, PlayerAdmin ) );
	}
}