package com.mcbans.firestar.mcbans.rollback;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import uk.co.oliwali.HawkEye.HawkEye;
import uk.co.oliwali.HawkEye.PlayerSession;
import uk.co.oliwali.HawkEye.SearchParser;
import uk.co.oliwali.HawkEye.SessionManager;
import uk.co.oliwali.HawkEye.Rollback.RollbackType;
import uk.co.oliwali.HawkEye.callbacks.BaseCallback;
import uk.co.oliwali.HawkEye.callbacks.RollbackCallback;
import uk.co.oliwali.HawkEye.database.SearchQuery;
import uk.co.oliwali.HawkEye.database.SearchQuery.SearchDir;

import com.mcbans.firestar.mcbans.BukkitInterface;

public class HeRollback extends BaseRollback{
    private HawkEye hawkeye;

    public HeRollback(BukkitInterface plugin) {
        super(plugin);

        Plugin he = plugin.getServer().getPluginManager().getPlugin("HawkEye");
        if (he != null){
            hawkeye = (HawkEye) he;
        }
    }

    @Override
    public boolean rollback(CommandSender sender, String admin, String target, int time) {
        PlayerSession session = SessionManager.getSession(sender);

        // Check if player already has a rollback processing
        if (session.doingRollback()){
            plugin.broadcastPlayer(admin, ChatColor.RED + "Unable to rollback player! You already have a rollback processing!");
            return false;
        }

        SearchParser parser = null;

        try {
            parser = new SearchParser();
            parser.players.add(target);
            parser.worlds = worlds;

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -1 * plugin.Settings.getInteger("backDaysAgo"));
            parser.dateFrom = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(cal.getTime());

            // Create new SearchQuery
            plugin.broadcastPlayer(admin, ChatColor.GREEN + "Starting rollback..");

            BaseCallback callback = new RollbackCallback(session, RollbackType.GLOBAL);
            new SearchQuery(callback, parser, SearchDir.DESC);
            //HawkEyeAPI.performSearch(callback, parser, SearchDir.DESC);
        }catch (Exception e){
            plugin.broadcastPlayer(admin, ChatColor.RED + "Unable to rollback player!");
            if (plugin.Settings.getBoolean("isDebug")) {
                e.printStackTrace();
            }
            return false;
        }

        return true;
    }
}