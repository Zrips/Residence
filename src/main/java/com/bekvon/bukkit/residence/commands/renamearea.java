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

public class renamearea implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2800)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if (args.length != 3)
            return false;

        ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);
        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        res.renameArea(player, args[1], args[2], resadmin);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Rename area name for residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res removeworld [residence] [oldAreaName] [newAreaName]"));
        LocaleManager.addTabCompleteMain(this, "[residence]", "[carea]");
    }
}
