package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;

public class version implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5900)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        sender.sendMessage(ChatColor.GRAY + "------------------------------------");
        sender.sendMessage(ChatColor.RED + "This server running " + ChatColor.GOLD + "Residence" + ChatColor.RED + " version: " + ChatColor.BLUE + plugin.getResidenceVersion());
        sender.sendMessage(ChatColor.GREEN + "Created by: " + ChatColor.YELLOW + "bekvon");
        sender.sendMessage(ChatColor.GREEN + "Updated to 1.8 by: " + ChatColor.YELLOW + "DartCZ");
        sender.sendMessage(ChatColor.GREEN + "Currently maintained by: " + ChatColor.YELLOW + "Zrips");
        StringBuilder names = null;
        for (String auth : plugin.getAuthors()) {
            if (names == null)
                names = new StringBuilder(auth);
            else
                names.append(", ").append(auth);
        }
        sender.sendMessage(ChatColor.GREEN + "Authors: " + ChatColor.YELLOW + names);
        sender.sendMessage(ChatColor.DARK_AQUA + "For a command list, and help, see the wiki:");
        sender.sendMessage(ChatColor.GREEN + "https://github.com/bekvon/Residence/wiki");
        sender.sendMessage(ChatColor.AQUA + "Visit the Spigot Resource page at:");
        sender.sendMessage(ChatColor.BLUE + "https://www.spigotmc.org/resources/residence.11480/");
        sender.sendMessage(ChatColor.GRAY + "------------------------------------");
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "how residence version");
        c.get("Info", Collections.singletonList("&eUsage: &6/res version"));
    }
}
