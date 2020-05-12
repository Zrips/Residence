package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.WorldFlagManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;

public class reload implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5800)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!resadmin && !sender.isOp()) {
            plugin.msg(sender, lm.General_NoPermission);
            return true;
        }

        if (args.length != 1) {
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "lang":
                plugin.getLM().LanguageReload();
                plugin.getLocaleManager().LoadLang(plugin.getConfigManager().getLanguage());
                sender.sendMessage(plugin.getPrefix() + " Reloaded language file.");
                return true;
            case "config":
                plugin.getConfigManager().UpdateConfigFile();
                sender.sendMessage(plugin.getPrefix() + " Reloaded config file.");
                return true;
            case "groups":
                plugin.getConfigManager().loadGroups();
                plugin.gmanager = new PermissionManager(plugin);
                plugin.wmanager = new WorldFlagManager(plugin);
                sender.sendMessage(plugin.getPrefix() + " Reloaded groups file.");
                return true;
            case "flags":
                plugin.getConfigManager().loadFlags();
                plugin.gmanager = new PermissionManager(plugin);
                plugin.imanager = new WorldItemManager(plugin);
                plugin.wmanager = new WorldFlagManager(plugin);
                FlagPermissions.initValidFlags();
                sender.sendMessage(plugin.getPrefix() + " Reloaded flags file.");
                return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "reload lanf or config files");
        c.get("Info", Collections.singletonList("&eUsage: &6/res reload [config/lang/groups/flags]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Collections.singletonList("config%%lang%%groups%%flags"));
    }
}
