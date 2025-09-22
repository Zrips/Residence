package com.bekvon.bukkit.residence.containers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResAdmin {

    private static Set<UUID> resadminToggle = new HashSet<UUID>();

    public static boolean isResAdmin(CommandSender sender) {
        if (sender instanceof Player)
            return isResAdmin((Player) sender);
        return true;
    }

    public static boolean isResAdmin(Player player) {
        if (player == null)
            return false;
        return isResAdmin(player.getUniqueId());
    }

    public static boolean isResAdmin(UUID uuid) {
        if (uuid == null)
            return false;
        return resadminToggle.contains(uuid);
    }

    public static void turnResAdmin(Player player, boolean state) {
        if (state)
            turnResAdminOn(player);
        else
            turnResAdminOff(player);
    }

    public static void turnResAdminOn(Player player) {
        resadminToggle.add(player.getUniqueId());
    }

    public static void turnResAdminOff(Player player) {
        resadminToggle.remove(player.getUniqueId());
    }
}
