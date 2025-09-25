package com.bekvon.bukkit.residence.permissions;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface PermissionsInterface {
    public String getPlayerGroup(Player player);

    @Deprecated
    public String getPlayerGroup(String player, String world);

    public String getPlayerGroup(UUID uuid, String world);

    public boolean hasPermission(OfflinePlayer player, String permission, String world);

}
