package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import net.Zrips.CMILib.Container.CMINumber;
import net.Zrips.CMILib.FileHandler.ConfigReader;

public class expand implements cmd {

	@Override
	@CommandAnnotation(simple = true, priority = 2000)
	public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
		if (!(sender instanceof Player))
			return false;

		Player player = (Player) sender;
		ClaimedResidence res = null;
		int amount = -1;

		Location loc = player.getLocation();

		for (String one : args) {

			if (res == null) {
				ClaimedResidence temp = plugin.getResidenceManager().getByName(one);
				if (temp != null) {
					res = temp;
				} else
					res = plugin.getResidenceManager().getByLoc(loc);
			}

			try {
				amount = Integer.parseInt(one);
			} catch (NumberFormatException e) {
			}
		}

		if (res == null || args.length <= 1)
			res = plugin.getResidenceManager().getByLoc(loc);

		if (res == null) {
			lm.Invalid_Residence.sendMessage(sender);
			return true;
		}

		if (res.getRaid().isRaidInitialized()) {
			lm.Raid_cantDo.sendMessage(sender);
			return true;
		}

		if (res.isSubzone() && !resadmin && !ResPerm.command_expand_subzone.hasPermission(player, lm.Subzone_CantExpand))
			return true;

		if (!res.isSubzone() && !resadmin && !ResPerm.command_$1.hasPermission(player, lm.Residence_CantExpandResidence, this.getClass().getSimpleName()))
			return true;

		String areaName = res.getAreaNameByLoc(loc);

		if (areaName == null)
			areaName = res.getMainAreaName();

		CuboidArea area = res.getArea(areaName);

		if (area == null) {
			lm.Area_NonExist.sendMessage(sender);
			return false;
		}

		plugin.getSelectionManager().placeLoc(player, area.getHighLocation(), area.getLowLocation(), false);

		amount = CMINumber.clamp(amount, 1, Integer.MAX_VALUE);

		plugin.getSelectionManager().modify(player, false, amount);

		if (plugin.getSelectionManager().hasPlacedBoth(player)) {
			if (plugin.getWorldEdit() != null && plugin.getWorldEditTool().equals(plugin.getConfigManager().getSelectionTool())) {
				plugin.getSelectionManager().worldEdit(player);
			}

			res.replaceArea(player, plugin.getSelectionManager().getSelectionCuboid(player), areaName, resadmin);
			return true;
		}
		lm.Select_Points.sendMessage(sender);

		return false;
	}

	@Override
	public void getLocale() {
		ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
		c.get("Description", "Expands residence in direction you looking");
		c.get("Info", Arrays.asList("&eUsage: &6/res expand (residence) [amount]", "Expands residence in direction you looking.", "Residence name is optional"));
		LocaleManager.addTabCompleteMain(this, "[residence]%%1", "1");
	}

}
