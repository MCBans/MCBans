package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.api.data.AltLookupData;
import com.mcbans.firestar.mcbans.util.Util;

public class AltLookupCallback extends BaseCallback{
    public AltLookupCallback(final MCBans plugin, final CommandSender sender) {
        super(plugin, sender);
    }
    public AltLookupCallback(){
        super(MCBans.getInstance(), null);
    }

    public void success(final AltLookupData data){
        Util.message(sender, Util.color("&fPlayer &3" + data.getPlayerName() + "&f may have &c" + data.getAltCount() + " alt account(s)&f."));
        if (data.getAltCount() > 0){
            StringBuilder line2 = new StringBuilder();
            
            HashMap<String, Double> map = data.getAltMap();
            boolean first = true;
            for (Map.Entry<String, Double> ent : map.entrySet()){
                String alt = ent.getKey();
                Double rep = ent.getValue();
                String repStr = ((rep < 10) ? "&c" : "&7") + rep;
                
                if (first) { first = false; }
                else { line2.append("&f, "); }
                
                line2.append("&2").append(alt).append("&f(").append(repStr).append("&f)");
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
