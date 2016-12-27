package com.bekvon.bukkit.residence.permissions;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.PlayerGroup;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionManager {
    protected static PermissionsInterface perms;
    protected LinkedHashMap<String, PermissionGroup> groups;
    protected Map<String, String> playersGroup;
    protected FlagPermissions globalFlagPerms;

    protected HashMap<String, PlayerGroup> groupsMap = new HashMap<String, PlayerGroup>();
    private Residence plugin;

    public PermissionManager(Residence plugin) {
	this.plugin = plugin;
	try {
	    groups = new LinkedHashMap<String, PermissionGroup>();
	    playersGroup = Collections.synchronizedMap(new HashMap<String, String>());
	    globalFlagPerms = new FlagPermissions();
	    this.readConfig();
	    checkPermissions();
	} catch (Exception ex) {
	    Logger.getLogger(PermissionManager.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    public FlagPermissions getAllFlags() {
	return this.globalFlagPerms;
    }

    public Map<String, String> getPlayersGroups() {
	return playersGroup;
    }

    public Map<String, PermissionGroup> getGroups() {
	return groups;
    }

//    public PermissionGroup getGroup(Player player) {
//	PermissionGroup group = Residence.getPlayerManager().getGroup(player.getName());
//	if (group != null) {
//	    return group;
//	}
//	return groups.get(this.getGroupNameByPlayer(player));
//    }
//
//    public PermissionGroup getGroup(String player, String world) {
//	PermissionGroup group = Residence.getPlayerManager().getGroup(player);
//	if (group != null) {
//	    return group;
//	}
//	return groups.get(this.getGroupNameByPlayer(player, world));
//    }

    public PermissionGroup getGroupByName(String group) {
	group = group.toLowerCase();
	if (!groups.containsKey(group)) {
	    return groups.get(plugin.getConfigManager().getDefaultGroup());
	}
	return groups.get(group);
    }

    public String getGroupNameByPlayer(Player player) {
	if (!this.groupsMap.containsKey(player.getName())) {
	    updateGroupNameForPlayer(player);
	}
	PlayerGroup PGroup = this.groupsMap.get(player.getName());
	if (PGroup != null) {
	    String group = PGroup.getGroup(player.getWorld().getName());
	    if (group != null)
		return group;
	}
	return plugin.getConfigManager().getDefaultGroup().toLowerCase();
    }

    public String getGroupNameByPlayer(String playerName, String world) {
	if (!this.groupsMap.containsKey(playerName)) {
	    Player player = Bukkit.getPlayer(playerName);
	    if (player != null)
		updateGroupNameForPlayer(player);
	    else
		updateGroupNameForPlayer(playerName, world, true);
	}
	PlayerGroup PGroup = this.groupsMap.get(playerName);
	if (PGroup != null) {
	    String group = PGroup.getGroup(world);
	    if (group != null)
		return group;
	}
	return plugin.getConfigManager().getDefaultGroup().toLowerCase();
    }

    public String getPermissionsGroup(Player player) {
	return this.getPermissionsGroup(player.getName(), player.getWorld().getName()).toLowerCase();
    }

    public String getPermissionsGroup(String player, String world) {
	if (perms == null)
	    return plugin.getConfigManager().getDefaultGroup().toLowerCase();
	try {
	    return perms.getPlayerGroup(player, world).toLowerCase();
	} catch (Exception e) {
	    return plugin.getConfigManager().getDefaultGroup().toLowerCase();
	}
    }

    public void updateGroupNameForPlayer(Player player) {
	updateGroupNameForPlayer(player, false);
    }

    public void updateGroupNameForPlayer(Player player, boolean force) {
	if (player == null)
	    return;
	updateGroupNameForPlayer(player.getName(), player.getWorld().getName(), force);
    }

    public void updateGroupNameForPlayer(String playerName, String world, boolean force) {
	PlayerGroup GPlayer;
	if (!groupsMap.containsKey(playerName)) {
	    GPlayer = new PlayerGroup(playerName);
	    groupsMap.put(playerName, GPlayer);
	} else
	    GPlayer = groupsMap.get(playerName);
	GPlayer.updateGroup(world, force);
    }

    public boolean isResidenceAdmin(CommandSender sender) {
	return (sender.hasPermission("residence.admin") || (sender.isOp() && plugin.getConfigManager().getOpsAreAdmins()));
    }

    private void checkPermissions() {
	Server server = plugin.getServ();
	Plugin p = server.getPluginManager().getPlugin("Vault");
	if (p != null) {
	    ResidenceVaultAdapter vault = new ResidenceVaultAdapter(server);
	    if (vault.permissionsOK()) {
		perms = vault;
		Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found Vault using permissions plugin:" + vault.getPermissionsName());
		return;
	    }
	    Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found Vault, but Vault reported no usable permissions system...");
	}
	Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Permissions plugin NOT FOUND!");
    }

    private void readConfig() {

	FileConfiguration groupsFile = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "groups.yml"));
	FileConfiguration flags = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "flags.yml"));

	String defaultGroup = plugin.getConfigManager().getDefaultGroup().toLowerCase();
	globalFlagPerms = FlagPermissions.parseFromConfigNode("FlagPermission", flags.getConfigurationSection("Global"));
	ConfigurationSection nodes = groupsFile.getConfigurationSection("Groups");
	if (nodes != null) {
	    Set<String> entrys = nodes.getKeys(false);
	    int i = 0;
	    for (String key : entrys) {
		try {
		    i++;
		    groups.put(key.toLowerCase(), new PermissionGroup(key.toLowerCase(), nodes.getConfigurationSection(key), globalFlagPerms, i));
		    List<String> mirrors = nodes.getConfigurationSection(key).getStringList("Mirror");
		    for (String group : mirrors) {
			groups.put(group.toLowerCase(), new PermissionGroup(key.toLowerCase(), nodes.getConfigurationSection(key), globalFlagPerms));
		    }
		} catch (Exception ex) {
		    Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Error parsing group from config:" + key + " Exception:" + ex);
		}
	    }
	}

	if (!groups.containsKey(defaultGroup)) {
	    groups.put(defaultGroup, new PermissionGroup(defaultGroup));
	}
	if (groupsFile.isConfigurationSection("GroupAssignments")) {
	    Set<String> keys = groupsFile.getConfigurationSection("GroupAssignments").getKeys(false);
	    if (keys != null) {
		for (String key : keys) {
		    playersGroup.put(key.toLowerCase(), groupsFile.getString("GroupAssignments." + key, defaultGroup).toLowerCase());
		}
	    }
	}
    }

    public boolean hasGroup(String group) {
	group = group.toLowerCase();
	return groups.containsKey(group);
    }

    public PermissionsInterface getPermissionsPlugin() {
	return perms;
    }
}
