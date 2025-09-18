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

public class sublist implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4100)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (args.length != 0 && args.length != 1 && args.length != 2)
            return false;

        int page = 0;
        try {
            if (args.length > 0) {
                page = Integer.parseInt(args[args.length - 1]);
            }
        } catch (Exception ex) {
        }

        ClaimedResidence res;
        if (args.length == 0 && sender instanceof Player) {
            res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
        } else {
            res = plugin.getResidenceManager().getByName(args[0]);
        }

        if (page < 1)
            page = 1;

        if (res != null) {
            res.printSubzoneList(sender, page);
        } else {
            lm.Invalid_Residence.sendMessage(sender);
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "List Residence Subzones");
        c.get("Info", Arrays.asList("&eUsage: &6/res sublist <residence> <page>", "List subzones within a residence."));
        LocaleManager.addTabCompleteMain(this, "[residence]");
    }

}
