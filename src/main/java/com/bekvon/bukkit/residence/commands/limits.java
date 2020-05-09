package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;

public class limits implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 900)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (!(sender instanceof Player) && args.length < 1)
	    return false;

	if (args.length != 0 && args.length != 1)
	    return false;
	final String[] tempArgs = args;
	OfflinePlayer target;
	boolean rsadm = false;
	if (tempArgs.length == 0) {
	    target = (Player) sender;
	    rsadm = true;
	} else
	    target = plugin.getOfflinePlayer(tempArgs[0]);
	if (target == null)
	    return false;

	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(target.getUniqueId());
	rPlayer.getGroup().printLimits(sender, target, rsadm);
	return true;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	// Main command
	c.get("Description", "Show your limits.");
	c.get("Info", Arrays.asList("&eUsage: &6/res limits (playerName)", "Shows the limitations you have on creating and managing residences."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[playername]"));
    }
}
