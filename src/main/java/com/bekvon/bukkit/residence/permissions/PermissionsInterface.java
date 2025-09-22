package com.bekvon.bukkit.residence.permissions;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface PermissionsInterface {
    public String getPlayerGroup(Player player);

    public String getPlayerGroup(String player, String world);

    public boolean hasPermission(OfflinePlayer player, String permission, String world);
}
