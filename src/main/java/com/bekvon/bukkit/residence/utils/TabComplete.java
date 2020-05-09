package com.bekvon.bukkit.residence.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import com.bekvon.bukkit.residence.Residence;

public class TabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
	List<String> completionList = new ArrayList<>();
	if (args.length >= 1) {
	    Set<String> Commands = Residence.getInstance().getHelpPages().getSubCommands(sender, args);
	    String PartOfCommand = args[args.length - 1];
	    StringUtil.copyPartialMatches(PartOfCommand, Commands, completionList);
	    Collections.sort(completionList);
	}
	return completionList;
    }
}
