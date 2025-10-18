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

public class contract implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1900)
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

            if (amount == -1) {
                try {
                    amount = Integer.parseInt(one);
                    continue;
                } catch (NumberFormatException e) {
                }
            }
        }

        if (res == null)
            res = plugin.getResidenceManager().getByLoc(loc);

        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        if (res.getRaid().isRaidInitialized()) {
            lm.Raid_cantDo.sendMessage(sender);
            return true;
        }

        if (res.isSubzone() && !resadmin && !ResPerm.command_contract_subzone.hasPermission(player, lm.Subzone_CantContract))
            return true;

        if (!res.isSubzone() && !resadmin && !ResPerm.command_$1.hasPermission(player, lm.Residence_CantContractResidence, this.getClass().getSimpleName()))
            return true;

        String areaName = res.getAreaNameByLoc(loc);

        if (areaName == null)
            areaName = res.getMainAreaName();

        CuboidArea area = res.getArea(areaName);

        if (area == null) {
            lm.Area_NonExist.sendMessage(sender);
            return false;
        }

        plugin.getSelectionManager().placeLoc1(player, area.getHighLocation(), false);
        plugin.getSelectionManager().placeLoc2(player, area.getLowLocation(), false);

        amount = CMINumber.clamp(amount, 1, Integer.MAX_VALUE);

        if (!plugin.getSelectionManager().contract(player, amount))
            return true;

        if (plugin.getSelectionManager().hasPlacedBoth(player)) {
            res.replaceArea(player, plugin.getSelectionManager().getSelectionCuboid(player), areaName, resadmin);
            return true;
        }
        lm.Select_Points.sendMessage(sender);

        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Contracts residence in direction you looking");
        c.get("Info", Arrays.asList("&eUsage: &6/res contract (residence) [amount]", "Contracts residence in direction you looking.",
            "Residence name is optional"));
        LocaleManager.addTabCompleteMain(this, "[residence]%%1", "1");
    }

}
