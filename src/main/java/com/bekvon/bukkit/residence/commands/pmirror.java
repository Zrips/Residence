package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class pmirror implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3710)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (args.length < 2 || args.length > 3)
            return false;

        ClaimedResidence res = args.length == 3 ? plugin.getResidenceManager().getByName(args[0])
                : sender instanceof Player ? plugin.getResidenceManager().getByLoc((Player) sender) : null;
        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        ResidencePlayer source = plugin.getPlayerManager().getResidencePlayer(args[args.length - 2]);
        ResidencePlayer target = plugin.getPlayerManager().getResidencePlayer(args[args.length - 1]);
        if (source == null || target == null) {
            lm.Invalid_Player.sendMessage(sender);
            return true;
        }

        res.getPermissions().mirrorPlayerFlags(sender, source.getUniqueId(), target.getUniqueId(), resadmin);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Mirrors player flags");
        c.get("Info", Arrays.asList("&eUsage: &6/res pmirror [Residence] [Source player] [Target player]",
                "Mirrors flags of one player onto another player in the same residence.",
                "Residence name can be skipped while standing inside one.",
                "You must be owner of the residence or a admin to do this."));
        LocaleManager.addTabCompleteMain(this, "[residence]%%[playername]", "[playername]", "[playername]");
    }
}
