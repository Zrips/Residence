package com.bekvon.bukkit.residence.permissions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicesManager;

import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;

public class LuckPerms4Adapter implements PermissionsInterface {

    LuckPermsApi api = null;

    public LuckPerms4Adapter() {
	ServicesManager manager = Bukkit.getServicesManager();
	if (manager.isProvidedFor(LuckPermsApi.class)) {
	    api = manager.getRegistration(LuckPermsApi.class).getProvider();
	}
    }

    @Override
    public String getPlayerGroup(Player player) {
	User user = api.getUser(player.getUniqueId());
	if (user == null)
	    return "";
	return user.getPrimaryGroup();
    }

    @Override
    public String getPlayerGroup(String player, String world) {
	User user = api.getUser(player);
	if (user == null)
	    return "";
	return user.getPrimaryGroup();
    }

}
