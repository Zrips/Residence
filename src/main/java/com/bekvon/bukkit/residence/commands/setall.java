package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

public class setall implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (args.length != 2)
            return false;

        String flag = args[0];

        Flags f = Flags.getFlag(flag);
        if (f != null)
            flag = f.toString();

        FlagState state = FlagPermissions.stringToFlagState(args[1]);
        FlagPermissions GlobalFlags = Residence.getInstance().getPermissionManager().getAllFlags();

        if (flag == null || !GlobalFlags.checkValidFlag(flag.toLowerCase(), true)) {
            lm.Invalid_Flag.sendMessage(sender);
            return true;
        }

        if (state.equals(FlagState.INVALID)) {
            lm.Invalid_FlagState.sendMessage(sender);
            return true;
        }

        int count = 0;
        int count2 = 0;
        for (World oneW : Bukkit.getWorlds()) {
            for (ClaimedResidence one : plugin.getResidenceManager().getFromAllResidences(true, false, oneW)) {
                count2++;
                if (one.getPermissions().setFlag(sender, flag, state, true, false))
                    count++;
            }
        }
        lm.Flag_ChangedFor.sendMessage(sender, count, count2);

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Set general flags on all residences");
        c.get("Info", Arrays.asList("&eUsage: &6/res setall [flag] [true/false/remove]"));
        LocaleManager.addTabCompleteMain(this, "[flag]", "true%%false%%remove");
    }
}
