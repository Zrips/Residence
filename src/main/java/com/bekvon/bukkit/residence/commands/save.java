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

public class save implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        ClaimedResidence res = null;

        for (String one : args) {
            if (res == null) {
                res = plugin.getResidenceManager().getByName(one);
            }
        }

        if (res == null && sender instanceof Player) {
            res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
        }

        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        boolean save = plugin.getSchematicManager().save(res);
        if (save) {
            lm.Schematic_Saved.sendMessage(sender);
        } else {
            lm.Schematic_Failed.sendMessage(sender);
        }

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Save residence into schematics");
        c.get("Info", Arrays.asList("&eUsage: &6/res save [residence_name]"));
        LocaleManager.addTabCompleteMain(this, "[residence]");
    }
}
