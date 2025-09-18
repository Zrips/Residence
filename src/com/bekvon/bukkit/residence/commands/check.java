package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class check implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3500, regVar = { 2, 3 }, consoleVar = { 666 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        Player player = (Player) sender;
        String pname = player.getName();

        if (args.length == 3)
            pname = args[2];

        ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);
        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return null;
        }

        Flags flag = Flags.getFlag(args[1]);

        if (flag == null) {
            lm.Invalid_Flag.sendMessage(sender);
            return null;
        }

        if (!res.getPermissions().hasApplicableFlag(player.getUniqueId(), args[1])) {
            lm.Flag_CheckFalse.sendMessage(sender, flag, pname, args[0]);
        } else {
            lm.Flag_CheckTrue.sendMessage(sender, flag, pname, args[0], (res.getPermissions().playerHas(player, res.getPermissions().getWorldName(), flag, false) ? lm.General_True.getMessage()
                : lm.General_False.getMessage()));
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Check flag state for you");
        c.get("Info", Arrays.asList("&eUsage: &6/res check [residence] [flag] (playername)"));
        LocaleManager.addTabCompleteMain(this, "[residence]", "[flag]", "[playername]");
    }
}
