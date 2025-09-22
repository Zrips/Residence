package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class listallhidden implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 4700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        int page = 1;
        try {
            if (args.length > 0) {
                page = Integer.parseInt(args[args.length - 1]);
            }
        } catch (Exception ex) {
        }
        if (!resadmin) {
            lm.General_NoPermission.sendMessage(sender);
            return true;
        }
        if (args.length == 0) {
            plugin.getResidenceManager().listAllResidences(sender, 1, true, true);
        } else if (args.length == 1) {
            try {
                plugin.getResidenceManager().listAllResidences(sender, page, true, true);
            } catch (Exception ex) {
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "List All Hidden Residences");
        c.get("Info", Arrays.asList("&eUsage: &6/res listhidden <page>", "Lists all hidden residences on the server."));
    }
}
