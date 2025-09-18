package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class tool implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1600)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        lm.General_Separator.sendMessage(sender);
        lm.Select_Tool.sendMessage(sender, plugin.getConfigManager().getSelectionTool().getName());
        lm.General_InfoTool.sendMessage(sender, plugin.getConfigManager().getInfoTool().getName());
        lm.General_Separator.sendMessage(sender);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Shows residence selection and info tool names");
        c.get("Info", Arrays.asList("&eUsage: &6/res tool"));
        LocaleManager.addTabCompleteMain(this);
    }
}
