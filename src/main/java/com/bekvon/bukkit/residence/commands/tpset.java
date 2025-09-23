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

public class tpset implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 200)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
        if (res != null) {
            res.setTpLoc(player, resadmin);
        } else {
            lm.Invalid_Residence.sendMessage(sender);
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Main command
        c.get("Description", "Set the teleport location of a Residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res tpset", "This will set the teleport location for a residence to where your standing.",
            "You must be standing in the residence to use this command.", "You must also be the owner or have the +admin flag for the residence."));
        LocaleManager.addTabCompleteMain(this);
    }
}
