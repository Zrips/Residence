package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

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
            plugin.msg(player, lm.Invalid_Residence);
            return true;
        }

        res.renameArea(player, args[1], args[2], resadmin);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Rename area name for residence");
        c.get("Info", Collections.singletonList("&eUsage: &6/res removeworld [residence] [oldAreaName] [newAreaName]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Collections.singletonList("[residence]"));
    }
}
