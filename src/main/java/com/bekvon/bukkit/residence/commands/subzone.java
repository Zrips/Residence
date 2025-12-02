package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

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
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        if (res.getRaid().isRaidInitialized()) {
            lm.Raid_cantDo.sendMessage(sender);
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
            lm.Select_Points.sendMessage(sender);
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Create subzones in residences.");
        c.get("Info", Arrays.asList("&eUsage: &6/res subzone <residence> [subzone name]",
                "If residence name is left off, will attempt to use residence your standing in."));
        LocaleManager.addTabCompleteMain(this, "[residence]");
    }

}
