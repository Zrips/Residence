package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class lists implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4900)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        if (args.length == 1) {
            if (args[0].equals("list")) {
                plugin.getPermissionListManager().printLists(player);
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equals("view")) {
                plugin.getPermissionListManager().printList(player, args[1]);
                return true;
            } else if (args[0].equals("remove")) {
                plugin.getPermissionListManager().removeList(player, args[1]);
                return true;
            } else if (args[0].equals("add")) {
                plugin.getPermissionListManager().makeList(player, args[1]);
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equals("apply")) {
                plugin.getPermissionListManager().applyListToResidence(player, args[1], args[2], resadmin);
                return true;
            }
        } else if (args.length == 4) {
            if (args[0].equals("set")) {
                plugin.getPermissionListManager().getList(player.getName(), args[1]).setFlag(args[2], FlagPermissions.stringToFlagState(args[3]));
                plugin.msg(player, lm.Flag_Set, args[2], args[1], FlagPermissions.stringToFlagState(args[3]));
                return true;
            }
        } else if (args.length == 5) {
            if (args[0].equals("gset")) {
                plugin.getPermissionListManager().getList(player.getName(), args[1]).setGroupFlag(args[2], args[3], FlagPermissions.stringToFlagState(args[4]));
                plugin.msg(player, lm.Flag_Set, args[2], args[1], FlagPermissions.stringToFlagState(args[3]));
                return true;
            } else if (args[0].equals("pset")) {
                plugin.getPermissionListManager().getList(player.getName(), args[1]).setPlayerFlag(args[2], args[3], FlagPermissions.stringToFlagState(args[4]));
                plugin.msg(player, lm.Flag_Set, args[2], args[1], FlagPermissions.stringToFlagState(args[3]));
                return true;
            }
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();

        c.get("Description", "Predefined permission lists");
        c.get("Info", Collections.singletonList("Predefined permissions that can be applied to a residence."));

        c.setP(c.getPath() + "SubCommands.");
        c.get("add.Description", "Add a list");
        c.get("add.Info", Collections.singletonList("&eUsage: &6/res lists add <listname>"));

        c.get("remove.Description", "Remove a list");
        c.get("remove.Info", Collections.singletonList("&eUsage: &6/res lists remove <listname>"));

        c.get("apply.Description", "Apply a list to a residence");
        c.get("apply.Info", Collections.singletonList("&eUsage: &6/res lists apply <listname> <residence>"));

        c.get("set.Description", "Set a flag");
        c.get("set.Info", Collections.singletonList("&eUsage: &6/res lists set <listname> <flag> <value>"));

        c.get("pset.Description", "Set a player flag");
        c.get("pset.Info", Collections.singletonList("&eUsage: &6/res lists pset <listname> <player> <flag> <value>"));

        c.get("gset.Description", "Set a group flag");
        c.get("gset.Info", Collections.singletonList("&eUsage: &6/res lists gset <listname> <group> <flag> <value>"));

        c.get("view.Description", "View a list.");
        c.get("view.Info", Collections.singletonList("&eUsage: &6/res lists view <listname>"));
    }

}
