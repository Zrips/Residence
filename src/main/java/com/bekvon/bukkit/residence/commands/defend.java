package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.raid.RaidDefender;

public class defend implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3100)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (!(sender instanceof Player))
	    return false;

	final Player player = (Player) sender;

	if (args.length != 0 && args.length != 1)
	    return false;

	if (!ConfigManager.RaidEnabled) {
	    plugin.msg(player, lm.Raid_NotEnabled);
	    return true;
	}

	ClaimedResidence res = null;
	ResidencePlayer target = null;
	if (args.length == 1) {
	    OfflinePlayer targetP = plugin.getOfflinePlayer(args[0]);
	    if (targetP != null) {
		if (!targetP.isOnline()) {
		    plugin.msg(player, lm.Invalid_PlayerOffline);
		    return true;
		}
		target = plugin.getPlayerManager().getResidencePlayer(targetP.getUniqueId());
	    } else
		res = plugin.getResidenceManager().getByName(args[0]);
	} else
	    res = plugin.getResidenceManager().getByLoc(player.getLocation());

	if (res == null) {
	    ResidencePlayer owner = plugin.getPlayerManager().getResidencePlayer(player);
	    res = owner.getCurrentlyRaidedResidence();
	}

	if (res == null) {
	    plugin.msg(player, lm.Invalid_Residence);
	    return true;
	}

	ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	if (resPlayer.getJoinedRaid() != null) {
	    plugin.msg(player, lm.Raid_attack_alreadyInAnother, resPlayer.getJoinedRaid().getRes().getName());
	    return true;
	}

	if (res.isOwner(player)) {
	    if (target != null) {
		ClaimedResidence req = joinRequests.get(target.getUniqueId());

		if (req != null && req.equals(res)) {
		    plugin.msg(target.getPlayer(), lm.Raid_defend_Joined, res.getName());
		    for (Entry<UUID, RaidDefender> one : res.getRaid().getDefenders().entrySet()) {
			if (!target.getUniqueId().equals(one.getKey())) {
			    plugin.msg(Bukkit.getPlayer(one.getKey()), lm.Raid_defend_JoinedDef, target.getPlayer().getDisplayName());
			}
		    }
		    res.getRaid().addDefender(target);
		    joinRequests.remove(target.getUniqueId());
		    ownerJoinRequests.remove(target.getUniqueId());
		    return true;
		}

		ownerJoinRequests.put(target.getUniqueId(), res);
		RawMessage rm = new RawMessage();
		rm.add("join raid " + res.getName(), res.getName(), "res defend " + res.getName());
		rm.show(target.getPlayer());
		return true;
	    }
	    plugin.msg(player, lm.Raid_defend_noSelf);
	    return true;
	}

	final ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(res.getOwnerUUID());
	if (!rPlayer.isOnline()) {
	    plugin.msg(player, lm.Raid_defend_IsOffline);
	    return true;
	}

	if (!res.isInPreRaid() && !res.isUnderRaid()) {
	    plugin.msg(player, lm.Raid_defend_notRaided);
	    return true;
	}

	if (res.isUnderRaid() || res.isInPreRaid()) {

	    ClaimedResidence req = ownerJoinRequests.get(player.getUniqueId());
	    if (req != null && req.equals(res)) {
		plugin.msg(player, lm.Raid_defend_Joined, res.getName());
		for (Entry<UUID, RaidDefender> one : res.getRaid().getDefenders().entrySet()) {
		    if (!player.getUniqueId().equals(one.getKey())) {
			plugin.msg(Bukkit.getPlayer(one.getKey()), lm.Raid_defend_JoinedDef, player.getDisplayName());
		    }
		}
		res.getRaid().addDefender(player);
		joinRequests.remove(player.getUniqueId());
		ownerJoinRequests.remove(player.getUniqueId());
		return true;
	    }

	    joinRequests.put(player.getUniqueId(), res);
	    RawMessage rm = new RawMessage();
	    plugin.msg(player, lm.Raid_defend_Sent, res.getName());
	    rm.add("Accept raid defend from " + player.getDisplayName(), player.getName(), "res defend " + player.getName());
	    rm.show(rPlayer.getPlayer());
	    return true;
	}

	plugin.msg(player, "Cant join raid");

	return false;
    }

    static HashMap<UUID, ClaimedResidence> ownerJoinRequests = new HashMap<UUID, ClaimedResidence>();
    static HashMap<UUID, ClaimedResidence> joinRequests = new HashMap<UUID, ClaimedResidence>();

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Join raid defence on residence");
	c.get("Info", Arrays.asList("&eUsage: &6/res defend [resName] (playerName)"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[cresidence]%%[playername]"));
    }

}
