package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class reset implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4400)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (args.length != 1 && args.length != 0)
            return false;

        String residenceName = null;
        if (args.length == 1)
            residenceName = args[0];

        ClaimedResidence res = null;
        if (residenceName != null && !residenceName.equalsIgnoreCase("all"))
            res = plugin.getResidenceManager().getByName(residenceName);
        if (args.length == 0 && sender instanceof Player)
            res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

        if (residenceName != null && !residenceName.equalsIgnoreCase("all") && res == null || args.length == 0 && res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        if (res != null) {
            if (!resadmin && !res.isOwner(sender)) {
                lm.Residence_NotOwner.sendMessage(sender);
                return true;
            }

            if (res.getRaid().isRaidInitialized() && !resadmin) {
                lm.Raid_cantDo.sendMessage(sender);
                return true;
            }

            res.getPermissions().applyDefaultFlags();
            lm.Flag_reset.sendMessage(sender, res.getName());
            return true;
        }

        if (!resadmin) {
            lm.General_AdminOnly.sendMessage(sender);
            return true;
        }

        int count = 0;
        for (World oneW : Bukkit.getWorlds()) {
            for (ClaimedResidence one : plugin.getResidenceManager().getFromAllResidences(true, false, oneW)) {
                one.getPermissions().applyDefaultFlags();
                count++;
            }
        }
        lm.Flag_resetAll.sendMessage(sender, count);

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Reset residence to default flags.");
        c.get("Info", Arrays.asList("&eUsage: &6/res reset <residence/all>",
                "Resets the flags on a residence to their default.  You must be the owner or an admin to do this."));
        LocaleManager.addTabCompleteMain(this, "[residence]%%all");
    }
}
