package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class create implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 100)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2) {
	    return false;
	}

	if (plugin.getWorldEdit() != null) {
	    if (plugin.getWorldEdit().getConfig().getInt("wand-item") == plugin.getConfigManager().getSelectionTool().getLegacyId()) {
		plugin.getSelectionManager().worldEdit(player);
	    }
	}
	if (plugin.getSelectionManager().hasPlacedBoth(player)) {
	    plugin.getResidenceManager().addResidence(player, args[1], plugin.getSelectionManager().getPlayerLoc1(player), plugin
		.getSelectionManager().getPlayerLoc2(player), resadmin);
	    return true;
	}
	plugin.msg(player, lm.Select_Points);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	// Main command
	c.get(path + "Description", "Create Residences");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res create <residence name>"));
    }
}
