package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.WorldFlagManager;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Messages.CMIMessages;

public class reload implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5800)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!resadmin && !sender.isOp()) {
            lm.General_NoPermission.sendMessage(sender);
            return true;
        }

        if (args.length != 1) {
            return false;
        }
        switch (args[0].toLowerCase()) {
        case "lang":
            plugin.getLM().LanguageReload();
            plugin.getLocaleManager().LoadLang(plugin.getConfigManager().getLanguage());
            LocaleManager.parseHelpEntries();
            CMIMessages.sendMessage(sender, plugin.getPrefix() + " Reloaded language file.");
            return true;
        case "config":
            plugin.getConfigManager().UpdateConfigFile();
            CMIMessages.sendMessage(sender, plugin.getPrefix() + " Reloaded config file.");
            return true;
        case "groups":
            plugin.getConfigManager().loadGroups();
            plugin.gmanager = new PermissionManager(plugin);
            plugin.wmanager = new WorldFlagManager(plugin);
            CMIMessages.sendMessage(sender, plugin.getPrefix() + " Reloaded groups file.");
            return true;
        case "flags":
            plugin.getConfigManager().loadFlags();
            plugin.gmanager = new PermissionManager(plugin);
            plugin.imanager = new WorldItemManager(plugin);
            plugin.wmanager = new WorldFlagManager(plugin);
            FlagPermissions.initValidFlags();
            CMIMessages.sendMessage(sender, plugin.getPrefix() + " Reloaded flags file.");
            return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "reload lanf or config files");
        c.get("Info", Arrays.asList("&eUsage: &6/res reload [config/lang/groups/flags]"));
        LocaleManager.addTabCompleteMain(this, "config%%lang%%groups%%flags");
    }
}
