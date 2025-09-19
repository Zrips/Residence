package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class padd implements cmd {

    public static String groupedFlag = "trusted";

    @Override
    @CommandAnnotation(simple = true, priority = 400)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        String baseCmd = "res";
        if (resadmin)
            baseCmd = "resadmin";
        if (args.length == 1) {
            if (!plugin.getPlayerManager().isPlayerExist(player, args[0], true))
                return false;
            player.performCommand(baseCmd + " pset " + args[0] + " " + groupedFlag + " true");
            return true;
        }
        if (args.length == 2) {
            if (!plugin.getPlayerManager().isPlayerExist(player, args[1], true))
                return false;
            player.performCommand(baseCmd + " pset " + args[0] + " " + args[1] + " " + groupedFlag + " true");
            return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Main command
        c.get("Description", "Add player to residence.");
        c.get("Info", Arrays.asList("&eUsage: &6/res padd <residence> [player]", "Adds essential flags for player"));
        LocaleManager.addTabCompleteMain(this, "[residence]%%[playername]", "[playername]");
    }
}
