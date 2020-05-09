package com.bekvon.bukkit.residence.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPerms5Adapter implements PermissionsInterface {

    LuckPerms api = null;

    public LuckPerms5Adapter() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            api = provider.getProvider();
        }
    }

    @Override
    public String getPlayerGroup(Player player) {
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null)
            return "";
        return user.getPrimaryGroup();
    }

    @Override
    public String getPlayerGroup(String player, String world) {
        User user = api.getUserManager().getUser(player);
        if (user == null)
            return "";
        return user.getPrimaryGroup();
    }

}
