package com.mcbans.firestar.mcbans;

/**
 *
 * @author Sean (rakiru)
 */
public class MCBansAPI {

	BukkitInterface plugin;

	public MCBansAPI (BukkitInterface plugin) {
		this.plugin = plugin;
	}

	/**
	 * Ban a player using the MCBans system
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param type Type of ban: "g" = global, "l" = local
	 * @param reason Reason for the ban
	 * @param adminName Name to be attached as player who placed the ban
	 */
	private void banPlayer(String playerName, String type, String reason, String adminName) {
		//Implement ban here
	}

	/**
	 * Ban a player globally using the MCBans system
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param reason Reason for the ban
	 * @param adminName Name to be attached as player who placed the ban
	 */
	public void globalBanPlayer(String playerName, String reason, String adminName) {
		banPlayer(playerName, "g", reason, adminName);
	}

	/**
	 * Ban a player globally using the MCBans system as console
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param adminName Name to be attached as player who placed the ban
	 */
	public void globalBanPlayer(String playerName, String reason) {
		globalBanPlayer(playerName, reason, "console");
	}

	/**
	 * Ban a player locally using the MCBans system
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param reason Reason for the ban
	 * @param adminName Name to be attached as player who placed the ban
	 */
	public void localBanPlayer(String playerName, String reason, String adminName) {
		banPlayer(playerName, "l", reason, adminName);
	}

	/**
	 * Ban a player locally using the MCBans system for the default ban reason
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param adminName Name to be attached as player who placed the ban
	 */
	public void localBanPlayer(String playerName, String adminName) {
		localBanPlayer(playerName, plugin.Settings.getString("defaultLocal"), adminName);
	}

	/**
	 * Ban a player locally using the MCBans system for the default ban reason as console
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 */
	public void localBanPlayer(String playerName) {
		localBanPlayer(playerName, "console");
	}

	/**
	 * Ban a player for a specific time using the MCBans system
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param time Number of units the ban should last
	 * @param units Units of time to measure in: "m" = minutes, "h" = hours, "d" = days
	 * @param reason Reason for the ban
	 * @param adminName Name to be attached as player who placed the ban
	 */
	public void tempBanPlayer(String playerName, float time, String units, String reason, String adminName) {
		//Implement tempban here
	}

	/**
	 * Ban a player for a specific time using the MCBans system for the default ban reason
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param time Number of units the ban should last
	 * @param units Units of time to measure in: "m" = minutes, "h" = hours, "d" = days
	 * @param adminName Name to be attached as player who placed the ban
	 */
	public void tempBanPlayer(String playerName, float time, String units, String adminName) {
		tempBanPlayer(playerName, time, units, plugin.Settings.getString("defaultTemp"), adminName);
	}

	/**
	 * Unban a player using the MCBans system
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param adminName Name to be attached as player who placed the ban
	 */
	public void unbanPlayer(String playerName, String adminName) {
		//Implement unban here
	}

	/**
	 * Kick a player using the MCBans system (the MCBans plugin will log the kick)
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param reason Reason for the kick
	 * @param adminName Name to be attached as player who kicked the player
	 */
	public void kickPlayer(String playerName, String reason, String adminName) {
		//Implement kick here
	}

	/**
	 * Kick a player using the MCBans system for the default kick reason (the MCBans plugin will log the kick)
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param adminName Name to be attached as player who kicked the player
	 */
	public void kickPlayer(String playerName, String adminName) {
		kickPlayer(playerName, plugin.Settings.getString("defaultKick"), adminName);
	}

	/**
	 * Kick a player using the MCBans system for the default kick reason  as console(the MCBans plugin will log the kick)
	 *
	 * @param playerName Full name of the player to be banned (no partial names)
	 * @param adminName Name to be attached as player who kicked the player
	 */
	public void kickPlayer(String playerName) {
		kickPlayer(playerName, "console");
	}
}
