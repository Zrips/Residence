package com.bekvon.bukkit.residence.permissions;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

public class LuckPerms5Adapter implements PermissionsInterface {

    static LuckPerms api = null;

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

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission, String world) {
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null)
            api.getUserManager().loadUser(player.getUniqueId());
        user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null)
            return false;
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public static void loadUser(UUID uuid) {
        if (!api.getUserManager().isLoaded(uuid))
            api.getUserManager().loadUser(uuid);
    }
}
