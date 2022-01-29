package com.mcbans.plugin.callBacks;

import com.mcbans.domain.models.client.Ban;
import com.mcbans.domain.models.client.Player;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.api.data.PlayerLookupData;
import com.mcbans.plugin.util.Util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LookupCallback extends BaseCallback{
    public LookupCallback(final MCBans plugin, final CommandSender sender) {
        super(plugin, sender);
    }
    public LookupCallback(){
        super(MCBans.getInstance(), null);
    }

    //@Override
    public void success(Player player, List<Ban> bans, Double rep){
        List<Ban> globals= new ArrayList<>(), locals= new ArrayList<>(), temps = new ArrayList<>();
        int totalBans = 0;
        if(bans!=null) {
            globals = bans.stream().filter(b -> b.getType().equals("global")).collect(Collectors.toList());
            locals = bans.stream().filter(b -> b.getType().equals("local")).collect(Collectors.toList());
            temps = bans.stream().filter(b -> b.getType().equals("temp")).collect(Collectors.toList());
            totalBans = bans.size();
        }

        if (globals.size() > 0 || locals.size() > 0 || temps.size() > 0) {
            Util.message(sender, ChatColor.DARK_GRAY + "------------------------------");
        }

        Util.message(sender, ChatColor.GRAY + "Player " + ChatColor.RED + player.getName() + ChatColor.GRAY + " has " + ChatColor.RED
                + totalBans + " ban(s)" + ChatColor.GRAY + ((rep==null)?"":" and " + ChatColor.BLUE + rep + " REP") + ChatColor.GRAY + ".");

        if (globals.size() > 0) {
            Util.message(sender, ChatColor.RED + "Global Bans:");
            for (Ban record : globals){
                String reasonString = StringEscapeUtils.unescapeHtml(record.getReason());
                if(reasonString.length()>30) {
                    reasonString = reasonString.substring(0, 40) + "...";
                }
                Util.message(sender, ChatColor.YELLOW+"#"+record.getId()+" "+ChatColor.AQUA+reasonString+ChatColor.WHITE+" - ( "+ChatColor.GOLD+record.getAdmin().getName()+ChatColor.WHITE+" ) "+ChatColor.BOLD+record.getServer().getAddress());
            }
        }
        if (locals.size() > 0) {
            Util.message(sender, ChatColor.GOLD + "Local Bans:");
            for (Ban record : locals){
                String reasonString = StringEscapeUtils.unescapeHtml(record.getReason());
                if(reasonString.length()>30) {
                    reasonString = reasonString.substring(0, 40) + "...";
                }
                Util.message(sender, ChatColor.YELLOW+"#"+record.getId()+" "+ChatColor.AQUA+reasonString+ChatColor.WHITE+" - ( "+ChatColor.GOLD+record.getAdmin().getName()+ChatColor.WHITE+" ) "+ChatColor.BOLD+record.getServer().getAddress());
            }
        }
        if (temps.size() > 0) {
            Util.message(sender, ChatColor.GRAY + "Temp Bans:");
            for (Ban record : temps){
                String reasonString = StringEscapeUtils.unescapeHtml(record.getReason());
                if(reasonString.length()>30) {
                    reasonString = reasonString.substring(0, 40) + "...";
                }
                Util.message(sender, ChatColor.YELLOW+"#"+record.getId()+" "+ChatColor.AQUA+reasonString+ChatColor.WHITE+" - ( "+ChatColor.GOLD+record.getAdmin().getName()+ChatColor.WHITE+" ) "+ChatColor.BOLD+record.getServer().getAddress());
            }
        }

        if (globals.size() > 0 || locals.size() > 0 || temps.size() > 0) {
            Util.message(sender, ChatColor.DARK_GRAY + "------------------------------");
        }

    }

    @Override
    public void success(){
        throw new IllegalArgumentException("Wrong Usage!");
    }

    @Override
    public void error(final String error){
        if (error != null && sender != null){
            Util.message(sender, ChatColor.RED + error);
        }
    }
}
