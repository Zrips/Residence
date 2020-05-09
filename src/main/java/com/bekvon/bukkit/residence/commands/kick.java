package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class kick implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2200)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length != 1)
	    return false;

	Player targetplayer = Bukkit.getPlayer(args[0]);
	if (targetplayer == null || !player.canSee(targetplayer)) {
	    plugin.msg(player, lm.General_NotOnline);
	    return true;
	}

	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

	PermissionGroup group = rPlayer.getGroup();
	if (!group.hasKickAccess() && !resadmin) {
	    plugin.msg(player, lm.General_NoPermission);
	    return true;
	}
	ClaimedResidence res = plugin.getResidenceManager().getByLoc(targetplayer.getLocation());

	if (res == null || !res.isOwner(player) && !resadmin) {
	    plugin.msg(player, lm.Residence_PlayerNotIn);
	    return true;
	}

	if (!res.isOwner(player))
	    return false;

	if (res.isRaidInitialized()) {
	    plugin.msg(sender, lm.Raid_cantDo);
	    return true;
	}
	
	if (res.getPlayersInResidence().contains(targetplayer)) {

	    if (ResPerm.command_kick_bypass.hasPermission(targetplayer)) {
		plugin.msg(sender, lm.Residence_CantKick);
		return true;
	    }
	    Location loc = plugin.getConfigManager().getKickLocation();
	    targetplayer.closeInventory();
	    if (loc != null)
		targetplayer.teleport(loc);
	    else
		targetplayer.teleport(res.getOutsideFreeLoc(player.getLocation(), player));
	    plugin.msg(targetplayer, lm.Residence_Kicked);
	}
	return true;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Kicks player from residence.");
	c.get("Info", Arrays.asList("&eUsage: &6/res kick <player>", "You must be the owner or an admin to do this.", "Player should be online."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[playername]"));
    }
}
