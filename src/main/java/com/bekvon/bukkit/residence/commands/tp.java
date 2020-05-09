package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class tp implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1400)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 1)
	    return false;

	ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);

	if (res == null) {
	    plugin.msg(player, lm.Invalid_Residence);
	    return true;
	}

	if (res.isRaidInitialized()) {
	    if (res.getRaid().isAttacker(player)) {
		plugin.msg(player, lm.Raid_cantDo);
		return true;
	    }
	}

	res.tpToResidence(player, player, resadmin);
	return true;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Teleport to a residence");
	c.get("Info", Arrays.asList("&eUsage: &6/res tp [residence]", "Teleports you to a residence, you must have +tp flag access or be the owner.",
	    "Your permission group must also be allowed to teleport by the server admin."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}
