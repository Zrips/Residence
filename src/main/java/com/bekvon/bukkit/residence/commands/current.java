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

public class current implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3100, regVar = {0}, consoleVar = {666})
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        Player player = (Player) sender;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
        if (res == null) {
            plugin.msg(player, lm.Residence_NotIn);
            return true;
        }

        plugin.msg(player, lm.Residence_In, res.getName());

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Show residence your currently in.");
        c.get("Info", Arrays.asList("&eUsage: &6/res current"));
    }

}
