package com.mcbans.firestar.mcbans.rollback;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import uk.co.oliwali.HawkEye.PlayerSession;
import uk.co.oliwali.HawkEye.Rollback.RollbackType;
import uk.co.oliwali.HawkEye.SearchParser;
import uk.co.oliwali.HawkEye.SessionManager;
import uk.co.oliwali.HawkEye.callbacks.RollbackCallback;
import uk.co.oliwali.HawkEye.database.SearchQuery;
import uk.co.oliwali.HawkEye.database.SearchQuery.SearchDir;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.util.Util;

public class HeRollback extends BaseRollback{

    public HeRollback(MCBans plugin) {
        super(plugin);
    }

    @Override
    public boolean rollback(CommandSender sender, String senderName, String target) {
        PlayerSession session = SessionManager.getSession(sender);

        // Check if player already has a rollback processing
        if (session.doingRollback()){
            Util.message(senderName, ChatColor.RED + "Unable to rollback player! You already have a rollback processing!");
            return false;
        }

        SearchParser parser = null;

        try {
            parser = new SearchParser();
            parser.players.add(target);
            parser.worlds = worlds;

            if (plugin.getConfigs().getBackDaysAgo() > 0){
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -1 * plugin.getConfigs().getBackDaysAgo());
                parser.dateFrom = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(cal.getTime());
            }

            // Create new SearchQuery
            Util.message(senderName, ChatColor.GREEN + "Starting rollback..");

            new SearchQuery(new RollbackCallback(session, RollbackType.GLOBAL), parser, SearchDir.DESC); // async rollback
            //HawkEyeAPI.performSearch(callback, parser, SearchDir.DESC);
        }catch (Exception e){
            Util.message(senderName, ChatColor.RED + "Unable to rollback player!");
            if (plugin.getConfigs().isDebug()) {
                e.printStackTrace();
            }
            return false;
        }

        return true;
    }
}