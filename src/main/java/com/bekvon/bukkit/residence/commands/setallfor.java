package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

public class setallfor implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (args.length != 3)
            return false;

        String playerName = args[0];
        String flag = args[1];

        Flags f = Flags.getFlag(flag);
        if (f != null)
            flag = f.toString();

        FlagState state = FlagState.fromString(args[2]);
        
        ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(playerName);
        if (resPlayer == null)
            return false;
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

        for (ClaimedResidence one : resPlayer.getResList()) {
            if (one.getPermissions().setFlag(sender, flag, state, true, false))
                count++;
        }

        lm.Flag_ChangedForOne.sendMessage(sender, count, resPlayer.getName());

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Set general flags on all residences owned by particular player");
        c.get("Info", Arrays.asList("&eUsage: &6/res setallfor [playerName] [flag] [true/false/remove]"));
        LocaleManager.addTabCompleteMain(this, "[playername]", "[flag]", "true%%false%%remove");
    }
}
