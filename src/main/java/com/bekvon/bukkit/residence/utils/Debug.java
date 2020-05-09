package com.bekvon.bukkit.residence.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Debug {
    public static void D(Object message) {
	Player player = Bukkit.getPlayer("Zrips");
	if (player == null)
	    return; 
	player.sendMessage(ChatColor.DARK_GRAY + "[Residence Debug] " + ChatColor.DARK_AQUA + ChatColor.translateAlternateColorCodes('&', message == null ? "Null" : message.toString()));
	return;
    }
}
