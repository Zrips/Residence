package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;

public class listhidden implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 4800)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        int page = 1;
        try {
            if (args.length > 0) {
                page = Integer.parseInt(args[args.length - 1]);
            }
        } catch (Exception ex) {
        }
        if (!resadmin) {
            plugin.msg(sender, lm.General_NoPermission);
            return true;
        }
        if (args.length == 0) {
            plugin.getResidenceManager().listResidences(sender, 1, true, true);
            return true;
        } else if (args.length == 1) {
            try {
                Integer.parseInt(args[0]);
                plugin.getResidenceManager().listResidences(sender, page, true, true);
            } catch (Exception ex) {
                plugin.getResidenceManager().listResidences(sender, args[0], 1, true, true, resadmin);
            }
            return true;
        } else if (args.length == 2) {
            plugin.getResidenceManager().listResidences(sender, args[0], page, true, true, resadmin);
            return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "List Hidden Residences");
        c.get("Info", Arrays.asList("&eUsage: &6/res listhidden <player> <page>", "Lists hidden residences for a player."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Collections.singletonList("[playername]"));
    }
}
