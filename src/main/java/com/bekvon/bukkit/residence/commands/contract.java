package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

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

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class contract implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1900)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        ClaimedResidence res = null;
        if (args.length == 1)
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
        else if (args.length == 2)
            res = plugin.getResidenceManager().getByName(args[0]);
        else
            return false;
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

        String resName = res.getName();
        CuboidArea area = null;
        String areaName = null;

        if (args.length == 1) {
            areaName = res.getAreaIDbyLoc(player.getLocation());
            area = res.getArea(areaName);
        } else if (args.length == 2) {
            areaName = res.isSubzone() ? plugin.getResidenceManager().getSubzoneNameByRes(res) : "main";
            area = res.getCuboidAreabyName(areaName);
        }

        if (area != null) {
            plugin.getSelectionManager().placeLoc1(player, area.getHighLocation(), false);
            plugin.getSelectionManager().placeLoc2(player, area.getLowLocation(), false);
            lm.Select_Area.sendMessage(sender, areaName, resName);
        } else {
            lm.Area_NonExist.sendMessage(sender);
            return true;
        }
        int amount = -1;
        try {
            if (args.length == 1)
                amount = Integer.parseInt(args[0]);
            else if (args.length == 2)
                amount = Integer.parseInt(args[1]);
        } catch (Exception ex) {
            lm.Invalid_Amount.sendMessage(sender);
            return true;
        }

        if (amount > 100) {
            lm.Invalid_Amount.sendMessage(sender);
            return true;
        }

        if (amount < 0)
            amount = 1;

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
