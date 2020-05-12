package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;

public class signupdate implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!resadmin) {
            plugin.msg(sender, lm.General_NoPermission);
            return true;
        }
        plugin.msg(sender, lm.Sign_Updated, plugin.getSignUtil().updateAllSigns());
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Updated residence signs");
        c.get("Info", Collections.singletonList("&eUsage: &6/res signupdate"));
    }
}
