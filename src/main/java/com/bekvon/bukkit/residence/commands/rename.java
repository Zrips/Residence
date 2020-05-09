package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;

public class rename implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (!(sender instanceof Player))
	    return false;

	if (args.length != 2)
	    return false;

	plugin.getResidenceManager().renameResidence((Player) sender, args[0], args[1], resadmin);
	return true;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Renames a residence.");
	c.get("Info", Arrays.asList("&eUsage: &6/res rename [OldName] [NewName]", "You must be the owner or an admin to do this.",
	    "The name must not already be taken by another residence."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}
