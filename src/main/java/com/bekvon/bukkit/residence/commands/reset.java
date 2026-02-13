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

        if (args.length > 2) {
            return false;
        }
        if (args.length == 2 && !args[1].equalsIgnoreCase("-ownerflag")) {
            return false;
        }
        String residenceName = null;
        boolean isAllResidence = false;
        boolean isOwnerFlag = false;

        if (args.length >= 1) {

            residenceName = args[0];
            isAllResidence = residenceName.equalsIgnoreCase("all");

            if (args.length == 2) {
                // - /res reset all -ownerflag
                if (isAllResidence) {
                    if (!resadmin) {
                        lm.General_AdminOnly.sendMessage(sender);
                        return true;
                    }
                    isOwnerFlag = true;

                    // - /res reset [ResName] -ownerflag
                } else {
                    ClaimedResidence oneRes = ClaimedResidence.getByName(residenceName);
                    if (oneRes == null) {
                        lm.Invalid_Residence.sendMessage(sender);
                        return true;
                    }
                    if (!resadmin && !oneRes.isOwner(sender)) {
                        lm.Residence_NotOwner.sendMessage(sender);
                        return true;
                    }
                    oneRes.getPermissions().resetGlobalCreatorDefaultFlags();
                    lm.Flag_resetOwnerFlags.sendMessage(sender, oneRes.getName());
                    return true;
                }
            }
        }

        ClaimedResidence res = null;
        if (residenceName != null && !isAllResidence)
            res = ClaimedResidence.getByName(residenceName);
        if (args.length == 0 && sender instanceof Player)
            res = ClaimedResidence.getByLoc(((Player) sender).getLocation());

        if (residenceName != null && !isAllResidence && res == null || args.length == 0 && res == null) {
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
                if (!isOwnerFlag) {
                    one.getPermissions().applyDefaultFlags();
                } else {
                    one.getPermissions().resetGlobalCreatorDefaultFlags();
                }
                count++;
            }
        }

        if (!isOwnerFlag) {
            lm.Flag_resetAll.sendMessage(sender, count);
        } else {
            lm.Flag_resetAllOwnerFlags.sendMessage(sender, count);
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Reset residence to default flags.");
        c.get("Info", Arrays.asList("&eUsage: &6/res reset <residence/all> (-ownerflag)",
                "Resets the flags on a residence to their default.  You must be the owner or an admin to do this.",
                "-ownerflag: Reset only owner's flags."));
        LocaleManager.addTabCompleteMain(this, "[residence]%%all", "-ownerflag%%");

    }
}
