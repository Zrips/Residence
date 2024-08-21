package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.raid.ResidenceRaid;
import com.bekvon.bukkit.residence.utils.TimeModifier;
import com.bekvon.bukkit.residence.utils.Utils;

public class raid implements cmd {

    enum States {
	start, stop, immunity, kick;

	public static States getState(String name) {
	    for (States one : States.values()) {
		if (one.toString().equalsIgnoreCase(name))
		    return one;
	    }
	    return null;
	}

    }

    @Override
    @CommandAnnotation(simple = true, priority = 3100, regVar = { 1, 2, 3, 4 }, consoleVar = { 2, 3, 4 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

	if (!ConfigManager.RaidEnabled) {
	    plugin.msg(sender, lm.Raid_NotEnabled);
	    return true;
	}

	if (!resadmin && !plugin.isResAdminOn(sender)) {
	    plugin.msg(sender, lm.General_NoPermission);
	    return null;
	}

	States state = States.getState(args[0]);

	if (state == null) {
	    return false;
	}

	switch (state) {
	case immunity:

	    ClaimedResidence res = null;

	    if (args.length > 2)
		res = plugin.getResidenceManager().getByName(args[2]);
	    if (res == null && sender instanceof Player)
		res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

	    if (res == null) {
		plugin.msg(sender, lm.Invalid_Residence);
		return null;
	    }

	    Long time = null;
	    if (args.length > 3)
		time = TimeModifier.getTimeRangeFromString(args[3]);

	    if (args.length < 2)
		return false;

	    if (time == null && args.length > 2)
		time = TimeModifier.getTimeRangeFromString(args[2]);

	    switch (args[1].toLowerCase()) {
	    case "add":
		if (time == null)
		    return false;
		Long immune = res.getRaid().getImmunityUntil();
		immune = immune == null || immune < System.currentTimeMillis() ? System.currentTimeMillis() : immune;
		immune += (time * 1000L);
		res.getRaid().setImmunityUntil(immune);
		plugin.msg(sender, lm.Raid_immune, Utils.to24hourShort(immune - System.currentTimeMillis()));
		return true;
	    case "take":
		if (time == null)
		    return false;
		immune = res.getRaid().getImmunityUntil();
		immune = immune == null || immune < System.currentTimeMillis() ? System.currentTimeMillis() : immune;
		immune -= (time * 1000L);
		res.getRaid().setImmunityUntil(immune);

		if (res.getRaid().isImmune())
		    plugin.msg(sender, lm.Raid_immune, Utils.to24hourShort(immune - System.currentTimeMillis()));
		else
		    plugin.msg(sender, lm.Raid_notImmune);
		return true;
	    case "set":
		if (time == null)
		    return false;
		immune = System.currentTimeMillis() + (time * 1000L);
		res.getRaid().setImmunityUntil(immune);
		plugin.msg(sender, lm.Raid_immune, Utils.to24hourShort(immune - System.currentTimeMillis()));

		return true;
	    case "clear":
		res.getRaid().setImmunityUntil(null);
		res.getRaid().setEndsAt(0L);
		plugin.msg(sender, lm.Raid_notImmune);

		return true;
	    }

	    break;
	case kick:

	    if (args.length < 2)
		return false;

	    ResidencePlayer rplayer = plugin.getPlayerManager().getResidencePlayer(args[1]);

	    if (rplayer == null) {
		plugin.msg(sender, lm.Invalid_Player);
		return null;
	    }

	    if (rplayer.getJoinedRaid() == null || rplayer.getJoinedRaid().isEnded()) {
		plugin.msg(sender, lm.Raid_notInRaid);
		return null;
	    }

	    ResidenceRaid raid = rplayer.getJoinedRaid();
	    if (raid == null || !raid.isUnderRaid() && !raid.isInPreRaid()) {
		plugin.msg(sender, lm.Raid_NotIn);
		return true;
	    }

	    if (raid.getRes().isOwner(rplayer.getUniqueId())) {
		plugin.msg(sender, lm.Raid_CantKick, raid.getRes().getName());
		return true;
	    }

	    raid.removeAttacker(rplayer);
	    raid.removeDefender(rplayer);
	    raid.getRes().kickFromResidence(rplayer.getPlayer());

	    plugin.msg(sender, lm.Raid_Kicked, rplayer.getName(), raid.getRes().getName());

	    return true;
	case start:

	    res = null;

	    if (args.length > 1)
		res = plugin.getResidenceManager().getByName(args[1]);
	    if (res == null && sender instanceof Player)
		res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

	    if (res == null) {
		plugin.msg(sender, lm.Invalid_Residence);
		return null;
	    }

	    if (res.getRaid().isUnderRaid() || res.getRaid().isInPreRaid()) {
		return null;
	    }

	    res.getRaid().endRaid();
	    res.getRaid().setEndsAt(0L);
	    res.getRPlayer().setLastRaidDefendTimer(0L);
	    
	    boolean started = res.getRaid().preStartRaid(null);

	    if (started) {
		res.getRaid().startRaid();
		return true;
	    }

	    break;
	case stop:

	    res = null;

	    if (args.length > 1)
		res = plugin.getResidenceManager().getByName(args[1]);
	    if (res == null && sender instanceof Player)
		res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

	    if (res == null) {
		plugin.msg(sender, lm.Invalid_Residence);
		return null;
	    }

	    if (!res.getRaid().isUnderRaid() && !res.getRaid().isInPreRaid()) {
		plugin.msg(sender, lm.Raid_defend_notRaided);
		return null;
	    }

	    res.getRaid().endRaid();
	    res.getRaid().setEndsAt(0L);

	    plugin.msg(sender, lm.Raid_stopped, res.getName());
	    return true;
	default:
	    break;
	}

	return false;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Manage raid in residence");
	c.get("Info", Arrays.asList("&eUsage: &6/res raid start [resname] (playerName)", "&6/res raid stop [resname]", "&6/res raid kick [playerName]",
	    "&6/res raid immunity [add/take/set/clear] [resname/currentres] [time]"));
	
	LocaleManager.addTabCompleteSub(this, "start", "[residence]");
	LocaleManager.addTabCompleteSub(this, "stop", "[residence]");
	LocaleManager.addTabCompleteSub(this, "kick", "[playername]");
	LocaleManager.addTabCompleteSub(this, "immunity", "add%%take%%set%%clear", "[residence]");
    }

}
