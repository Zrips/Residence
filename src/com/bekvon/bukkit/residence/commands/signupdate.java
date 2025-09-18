package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class signupdate implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!resadmin) {
            lm.General_NoPermission.sendMessage(sender);
            return true;
        }
        lm.Sign_Updated.sendMessage(sender, plugin.getSignUtil().updateAllSigns());
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Updated residence signs");
        c.get("Info", Arrays.asList("&eUsage: &6/res signupdate"));
    }
}
