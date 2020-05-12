package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

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
            plugin.msg(player, lm.Invalid_Residence);
            return true;
        }

        if (res.isRaidInitialized()) {
            plugin.msg(sender, lm.Raid_cantDo);
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
            plugin.getSelectionManager().placeLoc1(player, area.getHighLoc(), false);
            plugin.getSelectionManager().placeLoc2(player, area.getLowLoc(), false);
            plugin.msg(player, lm.Select_Area, areaName, resName);
        } else {
            plugin.msg(player, lm.Area_NonExist);
            return true;
        }
        int amount = -1;
        try {
            if (args.length == 1)
                amount = Integer.parseInt(args[0]);
            else if (args.length == 2)
                amount = Integer.parseInt(args[1]);
        } catch (Exception ex) {
            plugin.msg(player, lm.Invalid_Amount);
            return true;
        }

        if (amount > 1000) {
            plugin.msg(player, lm.Invalid_Amount);
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
        plugin.msg(player, lm.Select_Points);

        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Contracts residence in direction you looking");
        c.get("Info", Arrays.asList("&eUsage: &6/res contract (residence) [amount]", "Contracts residence in direction you looking.",
                "Residence name is optional"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%1", "1"));
    }

}
