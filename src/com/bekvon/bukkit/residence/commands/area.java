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
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class area implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3300)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}

	if (args.length == 4) {
	    if (args[1].equals("remove")) {
		ClaimedResidence res = plugin.getResidenceManager().getByName(args[2]);
		if (res != null) {
		    res.removeArea(player, args[3], resadmin);
		} else {
		    plugin.msg(player, lm.Invalid_Residence);
		}
		return true;
	    } else if (args[1].equals("add")) {
		if (plugin.getWorldEdit() != null) {
		    if (plugin.getWorldEdit().getConfig().getInt("wand-item") == plugin.getConfigManager().getSelectionTool().getLegacyId()) {
			plugin.getSelectionManager().worldEdit(player);
		    }
		}
		if (plugin.getSelectionManager().hasPlacedBoth(player)) {
		    ClaimedResidence res = plugin.getResidenceManager().getByName(args[2]);
		    if (res != null) {
			if (res.addArea(player, plugin.getSelectionManager().getSelectionCuboid(player), args[3], resadmin))
			    plugin.msg(player, lm.Area_Create, args[3]);
		    } else {
			plugin.msg(player, lm.Invalid_Residence);
		    }
		} else {
		    plugin.msg(player, lm.Select_Points);
		}
		return true;
	    } else if (args[1].equals("replace")) {
		if (plugin.getWorldEdit() != null) {
		    if (plugin.getWorldEdit().getConfig().getInt("wand-item") == plugin.getConfigManager().getSelectionTool().getLegacyId()) {
			plugin.getSelectionManager().worldEdit(player);
		    }
		}
		if (plugin.getSelectionManager().hasPlacedBoth(player)) {
		    ClaimedResidence res = plugin.getResidenceManager().getByName(args[2]);
		    if (res != null) {
			res.replaceArea(player, plugin.getSelectionManager().getSelectionCuboid(player), args[3], resadmin);
		    } else {
			plugin.msg(player, lm.Invalid_Residence);
		    }
		} else {
		    plugin.msg(player, lm.Select_Points);
		}
		return true;
	    }
	}
	if ((args.length == 3 || args.length == 4) && args[1].equals("list")) {
	    ClaimedResidence res = plugin.getResidenceManager().getByName(args[2]);
	    if (res != null) {
		res.printAreaList(player, page);
	    } else {
		plugin.msg(player, lm.Invalid_Residence);
	    }
	    return true;
	} else if ((args.length == 3 || args.length == 4) && args[1].equals("listall")) {
	    ClaimedResidence res = plugin.getResidenceManager().getByName(args[2]);
	    if (res != null) {
		res.printAdvancedAreaList(player, page);
	    } else {
		plugin.msg(player, lm.Invalid_Residence);
	    }
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Manage physical areas for a residence.");
	// Sub commands
	path += "SubCommands.";
	c.get(path + "list.Description", "List physical areas in a residence");
	c.get(path + "list.Info", Arrays.asList("&eUsage: &6/res area list [residence] <page>"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "list"), Arrays.asList("[residence]"));

	c.get(path + "listall.Description", "List coordinates and other Info for areas");
	c.get(path + "listall.Info", Arrays.asList("&eUsage: &6/res area listall [residence] <page>"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "listall"), Arrays.asList("[residence]"));

	c.get(path + "add.Description", "Add physical areas to a residence");
	c.get(path + "add.Info", Arrays.asList("&eUsage: &6/res area add [residence] [areaID]", "You must first select two points first."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "add"), Arrays.asList("[residence]"));

	c.get(path + "remove.Description", "Remove physical areas from a residence");
	c.get(path + "remove.Info", Arrays.asList("&eUsage: &6/res area remove [residence] [areaID]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "remove"), Arrays.asList("[residence]"));

	c.get(path + "replace.Description", "Replace physical areas in a residence");
	c.get(path + "replace.Info", Arrays.asList("&eUsage: &6/res area replace [residence] [areaID]",
	    "You must first select two points first.", "Replacing a area will charge the difference in size if the new area is bigger."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "replace"), Arrays.asList("[residence]"));

    }
}
