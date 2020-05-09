package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;

public class padd implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 400)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	String baseCmd = "res";
	if (resadmin)
	    baseCmd = "resadmin";
	if (args.length == 1) {
	    if (!plugin.isPlayerExist(player, args[0], true))
		return false;
	    player.performCommand(baseCmd + " pset " + args[0] + " trusted true");
	    return true;
	}
	if (args.length == 2) {
	    if (!plugin.isPlayerExist(player, args[1], true))
		return false;
	    player.performCommand(baseCmd + " pset " + args[0] + " " + args[1] + " trusted true");
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	// Main command
	c.get("Description", "Add player to residence.");
	c.get("Info", Arrays.asList("&eUsage: &6/res padd <residence> [player]", "Adds essential flags for player"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%[playername]", "[playername]"));
    }
}
