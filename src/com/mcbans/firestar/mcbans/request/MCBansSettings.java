package com.mcbans.firestar.mcbans.request;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.callBacks.MCBansSettingsCallback;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;

public class MCBansSettings extends BaseRequest<MCBansSettingsCallback> {
	public String commands = "";
	public String sender = "";
	public MCBansSettings(MCBans plugin, MCBansSettingsCallback callback, String sender, String commands) {
		super(plugin, callback);
		this.commands = commands;
		this.sender = sender;
		this.items.put("admin", sender);
		this.items.put("setting", commands);
		this.items.put("exec", "setting");
	}

	@Override
	protected void execute() {
		// TODO Auto-generated method stub
		if (callback.getSender() != null){
            log.info(callback.getSender().getName() + " executed setting change <" + commands + ">!");
        }
		JSONObject result = this.request_JOBJ();
		try{
            callback.success(result.getString("result"), result.getString("reason"));
        }
        catch (JSONException ex) {
            if (result.toString().contains("error")) {
                if (result.toString().contains("dne")){
                    callback.error("Could not execute settings change: <" + commands +">");
                    return;
                }
                else if (result.toString().contains("Server Disabled")) {
                    ActionLog.getInstance().severe("Server Disabled by an MCBans Staff Member!");
                    ActionLog.getInstance().severe("To appeal this decision, please file ticket on forums.mcbans.com");

                    callback.error("This server has been disabled by an MCBans Staff Member.");
                    return;
                }
            }
            ActionLog.getInstance().severe("JSON error while trying to change server settings!");
            callback.error("JSON Error");
        }
        catch (NullPointerException ex) {
            ActionLog.getInstance().severe("Unable to reach the MCBans API server!");
            callback.error(ChatColor.RED + "Unable to reach the MCBans API server!");
            if (plugin.getConfigs().isDebug()){
                ex.printStackTrace();
            }
        }
        catch (Exception ex){
            callback.error("Unknown Error: " + ex.getMessage());
            if (plugin.getConfigs().isDebug()){
                ex.printStackTrace();
            }
        }
	}

}
