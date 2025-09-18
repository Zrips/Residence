package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class lists implements cmd {
    private enum Action {
        list, view, remove, add, apply, set, gset, pset;

        public static Action getByName(String name) {
            for (Action one : Action.values()) {
                if (one.name().equalsIgnoreCase(name))
                    return one;
            }
            return null;
        }
    }

    @Override
    @CommandAnnotation(simple = true, priority = 4900)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        Action action = null;
        String listName = null;

        for (String one : args) {
            if (action == null) {
                action = Action.getByName(one);
                if (action != null)
                    continue;
            }

            if (listName == null) {
                listName = one;
                continue;
            }
        }

        if (action == null)
            return false;

        switch (action) {
        case add:
            if (listName == null)
                return false;
            plugin.getPermissionListManager().makeList(player, listName);
            break;
        case apply:
            if (listName == null || args.length < 3)
                return false;
            plugin.getPermissionListManager().applyListToResidence(player, listName, args[2], resadmin);
            break;
        case gset:
            if (listName == null || args.length < 5)
                return false;
            FlagPermissions list = plugin.getPermissionListManager().getList(player.getName(), listName);
            if (list == null)
                return false;
            list.setGroupFlag(args[2], args[3], FlagPermissions.stringToFlagState(args[4]));
            lm.Flag_Set.sendMessage(sender, args[2], args[1], FlagPermissions.stringToFlagState(args[3]));
            break;
        case list:
            plugin.getPermissionListManager().printLists(player);
            break;
        case pset:

            list = plugin.getPermissionListManager().getList(player.getName(), listName);
            if (list == null)
                return false;

            list.setPlayerFlag(args[2], args[3], FlagPermissions.stringToFlagState(args[4]));
            lm.Flag_Set.sendMessage(sender, args[2], listName, FlagPermissions.stringToFlagState(args[3]));
            break;
        case remove:
            if (listName == null)
                return false;
            plugin.getPermissionListManager().removeList(player, listName);
            break;
        case set:
            if (listName == null || args.length < 4)
                return false;

            list = plugin.getPermissionListManager().getList(player.getName(), listName);
            if (list == null)
                return false;

            list.setFlag(args[2], FlagPermissions.stringToFlagState(args[3]));
            lm.Flag_Set.sendMessage(sender, args[2], listName, FlagPermissions.stringToFlagState(args[3]));
            break;
        case view:
            if (listName == null)
                return false;
            plugin.getPermissionListManager().printList(player, listName);
            break;
        default:
            break;
        }

        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();

        c.get("Description", "Predefined permission lists");
        c.get("Info", Arrays.asList("Predefined permissions that can be applied to a residence."));

        c.setFullPath(c.getPath() + "SubCommands.");
        c.get("add.Description", "Add a list");
        c.get("add.Info", Arrays.asList("&eUsage: &6/res lists add <listname>"));
        LocaleManager.addTabCompleteSub(this, "add");

        c.get("remove.Description", "Remove a list");
        c.get("remove.Info", Arrays.asList("&eUsage: &6/res lists remove <listname>"));
        LocaleManager.addTabCompleteSub(this, "remove");

        c.get("apply.Description", "Apply a list to a residence");
        c.get("apply.Info", Arrays.asList("&eUsage: &6/res lists apply <listname> <residence>"));
        LocaleManager.addTabCompleteSub(this, "apply", "", "[cresidence]");

        c.get("set.Description", "Set a flag");
        c.get("set.Info", Arrays.asList("&eUsage: &6/res lists set <listname> <flag> <value>"));
        LocaleManager.addTabCompleteSub(this, "set", "", "[flag]", "true%%false%%remove");

        c.get("pset.Description", "Set a player flag");
        c.get("pset.Info", Arrays.asList("&eUsage: &6/res lists pset <listname> <player> <flag> <value>"));
        LocaleManager.addTabCompleteSub(this, "pset", "", "[flag]", "true%%false%%remove");

        c.get("gset.Description", "Set a group flag");
        c.get("gset.Info", Arrays.asList("&eUsage: &6/res lists gset <listname> <group> <flag> <value>"));
        LocaleManager.addTabCompleteSub(this, "gset", "", "", "[flag]", "true%%false%%remove");

        c.get("view.Description", "View a list.");
        c.get("view.Info", Arrays.asList("&eUsage: &6/res lists view <listname>"));
        LocaleManager.addTabCompleteSub(this, "view");
    }

}
