package com.bekvon.bukkit.residence.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;

public class removeworld implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5200)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

	if (args.length != 1 && args.length != 2)
	    return false;
	if (sender instanceof Player || sender instanceof BlockCommandSender) {
	    sender.sendMessage(ChatColor.RED + "MUST be run from console.");
	    return false;
	}

	List<String> playerExceptions = new ArrayList<String>();
	if (args.length == 2) {
	    for (String one : args[1].split(",")) {
		// Not lowercasing UUID's
		if (one.length() == 36) {
		    try {
			if (UUID.fromString(one) != null)
			    playerExceptions.add(one);
		    } catch (Throwable e) {
			playerExceptions.add(one.toLowerCase());
		    }
		} else
		    playerExceptions.add(one.toLowerCase());
	    }
	}

	plugin.getResidenceManager().removeAllFromWorld(sender, args[0], playerExceptions);

	return true;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Removes all residences from particular world");
	c.get("Info", Arrays.asList("&eUsage: &6/res removeworld [worldName] (playerExceptions)"));
	LocaleManager.addTabCompleteMain(this, "[worldname]");
    }
}
