package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;

public class removeall implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5100)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (args.length != 1 && args.length != 0) {
            return false;
        }

        String target = args.length == 1 ? args[0] : sender.getName();

        if (resadmin) {
            plugin.getResidenceManager().removeAllByOwner(target);
            plugin.msg(sender, lm.Residence_RemovePlayersResidences, target);
        } else {
            plugin.msg(sender, lm.General_NoPermission);
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Remove all residences owned by a player.");
        c.get("Info", Arrays.asList("&eUsage: &6/res removeall [owner]",
                "Removes all residences owned by a specific player.'", "Requires /resadmin if you use it on anyone besides yourself."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Collections.singletonList("[playername]"));
    }

}
