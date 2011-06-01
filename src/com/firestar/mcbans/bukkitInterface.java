package com.firestar.mcbans;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.firestar.mcbans.commands.commandHandler;

public class bukkitInterface extends JavaPlugin {
	private commandHandler commandHandle = new commandHandler(); 
	public void onDisable() {
		
	}
	
	public void onEnable() {
		
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if(sender instanceof Player){
			commandHandle.execCommand( command.getName(), args, ((Player) sender).getName() );
		}else{
			commandHandle.execCommand( command.getName(), args, "console" );
		}
		return false;
	}
}