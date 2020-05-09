package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

public class setall implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (args.length != 2)
	    return false;

	String flag = args[0];

	Flags f = Flags.getFlag(flag);
	if (f != null)
	    flag = f.toString();

	FlagState state = FlagPermissions.stringToFlagState(args[1]);
	FlagPermissions GlobalFlags = Residence.getInstance().getPermissionManager().getAllFlags();

	if (flag == null || !GlobalFlags.checkValidFlag(flag.toLowerCase(), true)) {
	    plugin.msg(sender, lm.Invalid_Flag);
	    return true;
	}

	if (state.equals(FlagState.INVALID)) {
	    plugin.msg(sender, lm.Invalid_FlagState);
	    return true;
	}

	int count = 0;
	int count2 = 0;
	for (World oneW : Bukkit.getWorlds()) {
	    for (ClaimedResidence one : plugin.getResidenceManager().getFromAllResidences(true, false, oneW)) {
		count2++;
		if (one.getPermissions().setFlag(sender, flag, state, true, false))
		    count++;
	    }
	}
	plugin.msg(sender, lm.Flag_ChangedFor, count, count2);

	return true;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Set general flags on all residences");
	c.get("Info", Arrays.asList("&eUsage: &6/res setall [flag] [true/false/remove]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[flag]", "true%%false%%remove"));
    }
}
