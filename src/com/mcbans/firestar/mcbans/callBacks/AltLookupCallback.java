package com.mcbans.firestar.mcbans.callBacks;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.api.data.AltLookupData;
import com.mcbans.firestar.mcbans.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class AltLookupCallback extends BaseCallback{
    public AltLookupCallback(final MCBans plugin, final CommandSender sender) {
        super(plugin, sender);
    }

	// This Constructor is not used.
	public AltLookupCallback(){
		super(MCBans.getInstance(), null);
	}

    public void success(final AltLookupData data){
        Util.message(sender, Util.color("&7Player &c" + data.getPlayerName() + "&7 may have &f" + data.getAltCount() + "&7 alternate account(s)."));
        if (data.getAltCount() > 0){
            StringBuilder line2 = new StringBuilder();
            
            HashMap<String, Double> map = data.getAltMap();
            boolean first = true;
            for (Map.Entry<String, Double> ent : map.entrySet()){
                String alt = ent.getKey();
                Double rep = ent.getValue();
                String repStr = ((rep < 10) ? "&c" : "&9") + rep;
                
                if (first) { first = false; }
                else { line2.append("&8 / "); }
                
                line2.append("&b").append(alt).append("&8 (").append(repStr).append("&8)");
            }
            
            Util.message(sender, Util.color(line2.toString()));
        }
    }

    @Override
    public void success(){
        throw new IllegalArgumentException("Wrong usage!");
    }

    @Override
    public void error(final String error){
        if (error != null && sender != null){
            Util.message(sender, ChatColor.RED + error);
        }
    }
}
