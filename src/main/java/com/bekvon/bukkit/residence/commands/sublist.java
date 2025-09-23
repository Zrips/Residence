package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class sublist implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4100, regVar = { 0, 1, 2 }, consoleVar = { 0, 1, 2 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        int page = 0;
        String residenceName = null;
        for (String one : args) {

            if (page <= 0)
                try {
                    page = Integer.parseInt(one);
                    continue;
                } catch (Exception ex) {
                }

            if (residenceName == null)
                residenceName = one;
        }

        ClaimedResidence res;
        if (residenceName == null && sender instanceof Player) {
            res = plugin.getResidenceManager().getByLoc(sender);
        } else {
            res = plugin.getResidenceManager().getByName(residenceName);
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
