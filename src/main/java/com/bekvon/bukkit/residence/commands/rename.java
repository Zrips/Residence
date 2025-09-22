package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class rename implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (args.length != 2)
            return false;

        plugin.getResidenceManager().renameResidence(sender, args[0], args[1], resadmin);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Renames a residence.");
        c.get("Info", Arrays.asList("&eUsage: &6/res rename [OldName] [NewName]", "You must be the owner or an admin to do this.",
            "The name must not already be taken by another residence."));
        LocaleManager.addTabCompleteMain(this, "[residence]");
    }
}
