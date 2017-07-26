package com.mcbans.firestar.mcbans.permission;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;

public class PermissionHandler {
    public enum PermType {
        VAULT,
        PEX,
        SUPERPERMS,
        OPS,
        ;
    }

    // instance
    private static PermissionHandler instance;

    private final MCBans plugin;
    private final ActionLog log;
    private PermType permType = null;

    // permission plugin
    private net.milkbowl.vault.permission.Permission vaultPermission = null; // not import package
    private PermissionsEx pex = null;

    /**
     * Constructor
     * @param plugin MCBans instance
     */
    private PermissionHandler(final MCBans plugin){
        this.plugin = plugin;
        this.log = plugin.getLog();
        instance = this;
    }

    /**
     * Setup and Select permission controller
     * @param silent false if send message to console
     */
    public void setupPermissions(final boolean silent){
        final String selected = plugin.getConfigs().getPermission().trim();
        boolean found = true;

        if ("vault".equalsIgnoreCase(selected)){
            if (setupVaultPermission()){
                permType = PermType.VAULT;
            }else{
                log.warning("Selected Vault for permission control, but MCBans did not find this plugin!");
            }
        }
        else if ("pex".equalsIgnoreCase(selected) || "permissionsex".equalsIgnoreCase(selected)){
            if (setupPEXPermission()){
                permType = PermType.PEX;
            }else{
                log.warning("Selected PermissionsEx for permission control, but MCBans did not find this plugin!");
            }
        }
        else if ("superperms".equalsIgnoreCase(selected)){
            permType = PermType.SUPERPERMS;
        }
        else if ("ops".equalsIgnoreCase(selected)){
            permType = PermType.OPS;
        }
        else{
            found = false;
        }

        // Invalid configuration, Use default SuperPerms
        if (permType == null){
            permType = PermType.SUPERPERMS;
            if (!found) log.warning("Valid permissions name not selected!");
        }

        // Display result
        if (!silent){
            log.info("Using " + getPermTypeString() + " for permission control.");
        }
    }
    public void setupPermissions(){
        this.setupPermissions(false);
    }

    /**
     * Check permissible has the permission.
     * @param permissible check target Sender, Player etc.
     * @param permission check permission node.
     * @return true if permissible has that permission.
     */
    public boolean has(final Permissible permissible, final String permission){
        // Console / Rcon has all permission, return true
        if (!(permissible instanceof Player)){
            return true;
        }
        
        Player player = (Player) permissible;

        // Switch by using permission controller
        switch (permType){
            // Vault
            case VAULT:
                return vaultPermission.has(player, permission);

            // PEX
            case PEX:
                return pex.has(player, permission);

            // SuperPerms
            case SUPERPERMS:
                return player.hasPermission(permission);

            // Ops
            case OPS:
                return player.isOp();

            // Other Types, forgot to add here
            default:
                log.warning("Plugin author forgot add to integration to this permission plugin! Please report this!");
                return false;
        }
    }

    /**
     * Check player has the permission in specific world. (working only Vault or Pex)
     * @param playerName check target player name. maybe needs online.
     * @param permission check permission node.
     * @param worldName check target world name.
     * @return true if player has that permission in specific world.
     */
    public boolean has(final String playerName, final String permission, final String worldName){
        // Switch by using permission controller
        switch (permType){
            // Vault
            case VAULT:
                return vaultPermission.has(worldName, playerName, permission);

            // PEX
            case PEX:
                PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
                if (user == null){ return false; }
                return user.has(permission, worldName);

            // SuperPerms
            case SUPERPERMS: {
                // NOTE: SuperPerms has not Cross-World permission system, So this check is not working properly.
                Player player = plugin.getServer().getPlayer(playerName);
                if (player == null) return false;
                else return player.hasPermission(permission);
            }

            // Ops
            case OPS:{
                Player player = plugin.getServer().getPlayer(playerName);
                if (player == null) return false;
                else return player.isOp();
            }

            // Other Types, forgot add here
            default:
                log.warning("Plugin author forgot add to integration to this permission plugin! Please report this!");
                return false;
        }
    }

    /**
     * Get using permission controller name
     * @return string controller name
     */
    public String getPermTypeString(){
        // Switch by using permission controller
        switch (permType){
            case VAULT:
                return "Vault: " + Bukkit.getServer().getServicesManager().getRegistration(Permission.class).getProvider().getName();

            case PEX:
                return "PermissionsEx";

            case OPS:
                return "OPs";

            case SUPERPERMS:
                return "SuperPerms";

            default:
                return "Unknown! Please report this!";
        }
    }

    // ** setup controller plugins *****
    /**
     * Setup Vault plugin
     * @return boolean true if success
     */
    private boolean setupVaultPermission(){
        Plugin vault = plugin.getServer().getPluginManager().getPlugin("Vault");
        if (vault == null) vault = plugin.getServer().getPluginManager().getPlugin("vault");
        if (vault == null) return false;
        try{
            RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            if (permissionProvider != null){
                vaultPermission = permissionProvider.getProvider();
            }
        }catch (Exception ex){
            log.warning("Unexpected error trying to setup Vault permissions!");
            ex.printStackTrace();
        }

        return (vaultPermission != null);
    }

    /**
     * Setup PermissionsEx plugin
     * @return boolean true if success
     */
    private boolean setupPEXPermission(){
        Plugin testPex = plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
        if (testPex == null) testPex = plugin.getServer().getPluginManager().getPlugin("permissionsex");
        if (testPex == null) return false;
        try{
            pex = (PermissionsEx) testPex;
        }catch (Exception ex){
            log.warning("Unexpected error trying to setup PEX permissions!");
            ex.printStackTrace();
        }

        return (pex != null);
    }
    // ** end **************************

    /**
     * Get singleton instance
     * @return PermissionHandler instance
     */
    public static PermissionHandler getInstance(){
        if (instance == null){
            synchronized (PermissionHandler.class){
                if (instance == null){
                    instance = new PermissionHandler(MCBans.getInstance());
                }
            }
        }
        return instance;
    }
}
