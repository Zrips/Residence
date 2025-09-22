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
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class server implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5400)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        if (!resadmin) {
            lm.General_NoPermission.sendMessage(sender);
            return true;
        }
        if (args.length != 1 || plugin.getResidenceManager().getByName(args[0]) == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);
        res.getPermissions().setOwner(plugin.getServerLandName(), false);
        lm.Residence_OwnerChange.sendMessage(sender, args[0], plugin.getServerLandName());
        return true;

    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();

        c.get("Description", "Make land server owned.");
        c.get("Info", Arrays.asList("&eUsage: &6/resadmin server [residence]", "Make a residence server owned."));
        LocaleManager.addTabCompleteMain(this, "[cresidence]");
    }
}
