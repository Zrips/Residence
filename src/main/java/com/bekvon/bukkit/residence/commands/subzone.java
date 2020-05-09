package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class subzone implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2100)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if (args.length != 1 && args.length != 2) {
            return false;
        }
        String zname;
        ClaimedResidence res = null;
        if (args.length == 1) {
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
            zname = args[0];
        } else {
            res = plugin.getResidenceManager().getByName(args[0]);
            zname = args[1];
        }
        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return true;
        }

        if (res.isRaidInitialized()) {
            plugin.msg(sender, lm.Raid_cantDo);
            return true;
        }

        if (plugin.getWorldEdit() != null) {
            if (plugin.getWorldEditTool() == plugin.getConfigManager().getSelectionTool()) {
                plugin.getSelectionManager().worldEdit(player);
            }
        }
        if (plugin.getSelectionManager().hasPlacedBoth(player)) {
            if (!resadmin && !ResPerm.create_subzone.hasPermission(player, lm.Subzone_CantCreate))
                return true;

            res.addSubzone(player, plugin.getSelectionManager().getPlayerLoc1(player), plugin.getSelectionManager().getPlayerLoc2(player),
                    zname, resadmin);
        } else {
            plugin.msg(player, lm.Select_Points);
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Create subzones in residences.");
        c.get("Info", Arrays.asList("&eUsage: &6/res subzone <residence> [subzone name]",
                "If residence name is left off, will attempt to use residence your standing in."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }

}
