package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;

public class pdel implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 500)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	String baseCmd = "res";
	if (resadmin)
	    baseCmd = "resadmin";
	if (args.length == 1) {
	    Bukkit.dispatchCommand(player, baseCmd + " pset " + args[0] + " trusted remove");
	    return true;
	}
	if (args.length == 2) {
	    Bukkit.dispatchCommand(player, baseCmd + " pset " + args[0] + " " + args[1] + " trusted remove");
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	// Main command
	c.get("Description", "Remove player from residence.");
	c.get("Info", Arrays.asList("&eUsage: &6/res pdel <residence> [player]", "Removes essential flags from player"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%[playername]", "[playername]"));
    }

}
