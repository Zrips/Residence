package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.raid.ResidenceRaid;

public class leaveraid implements cmd {

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

	ResidencePlayer owner = plugin.getPlayerManager().getResidencePlayer(player);

	ResidenceRaid raid = owner.getJoinedRaid();

	if (raid == null || !raid.getRes().isUnderRaid() && !raid.getRes().isInPreRaid()) {
	    plugin.msg(player, lm.Raid_NotIn);
	    return true;
	}

	if (raid.getRes().isOwner(player)) {
	    plugin.msg(player, lm.Raid_CantLeave, raid.getRes().getName());
	    return true;
	}

	raid.removeAttacker(player);
	raid.removeDefender(player);
	raid.getRes().kickFromResidence(player);

	plugin.msg(player, lm.Raid_left, raid.getRes().getName());

	return false;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Leave raid");
	c.get("Info", Arrays.asList("&eUsage: &6/res leaveraid"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[cresidence]%%[playername]"));
    }

}
