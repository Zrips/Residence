package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class area implements cmd {

    @Override
    @CommandAnnotation(info = "Manage physical areas for a residence.", priority = 3300, regVar = { 2, 3 }, consoleVar = { 666 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        Player player = (Player) sender;
        int page = 1;
        try {
            if (args.length > 0) {
                page = Integer.parseInt(args[args.length - 1]);
            }
        } catch (Exception ex) {
        }

        switch (args[0].toLowerCase()) {
        case "remove":
            if (args.length != 3)
                return false;

            ClaimedResidence res = plugin.getResidenceManager().getByName(args[1]);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(player);
                return null;
            }

            res.removeArea(player, args[2], resadmin);
            return true;

        case "add":

            if (args.length != 3)
                return false;

            if (plugin.getWorldEdit() != null && plugin.getWorldEditTool().equals(plugin.getConfigManager().getSelectionTool())) {
                plugin.getSelectionManager().worldEdit(player);
            }

            if (!plugin.getSelectionManager().hasPlacedBoth(player)) {
                lm.Select_Points.sendMessage(player);
                return null;
            }

            res = plugin.getResidenceManager().getByName(args[1]);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(player);
                return null;
            }

            if (res.addArea(player, plugin.getSelectionManager().getSelectionCuboid(player), args[2], resadmin))
                lm.Area_Create.sendMessage(player, args[2]);

            return true;

        case "replace":
            if (args.length != 3)
                return false;

            if (plugin.getWorldEdit() != null && plugin.getWorldEditTool().equals(plugin.getConfigManager().getSelectionTool())) {
                plugin.getSelectionManager().worldEdit(player);
            }

            if (!plugin.getSelectionManager().hasPlacedBoth(player)) {
                lm.Select_Points.sendMessage(player);
                return null;
            }

            res = plugin.getResidenceManager().getByName(args[1]);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(player);
                return null;
            }

            res.replaceArea(player, plugin.getSelectionManager().getSelectionCuboid(player), args[2], resadmin);
            return true;

        case "list":
            if (args.length != 2)
                return false;

            res = plugin.getResidenceManager().getByName(args[1]);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(player);
                return null;
            }

            res.printAreaList(player, page);
            return true;
        case "listall":
            if (args.length != 2)
                return false;

            res = plugin.getResidenceManager().getByName(args[1]);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(player);
                return null;
            }

            res.printAdvancedAreaList(player, page);
            return true;
        }

        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Sub commands
        c.setFullPath(c.getPath() + "SubCommands.");
        c.get("list.Description", "List physical areas in a residence");
        c.get("list.Info", Arrays.asList("&eUsage: &6/res area list [residence] <page>"));
        LocaleManager.addTabCompleteSub(this, "list", "[residence]");

        c.get("listall.Description", "List coordinates and other Info for areas");
        c.get("listall.Info", Arrays.asList("&eUsage: &6/res area listall [residence] <page>"));
        LocaleManager.addTabCompleteSub(this, "listall", "[residence]");

        c.get("add.Description", "Add physical areas to a residence");
        c.get("add.Info", Arrays.asList("&eUsage: &6/res area add [residence] [areaID]", "You must first select two points first."));
        LocaleManager.addTabCompleteSub(this, "add", "[residence]");

        c.get("remove.Description", "Remove physical areas from a residence");
        c.get("remove.Info", Arrays.asList("&eUsage: &6/res area remove [residence] [areaID]"));
        LocaleManager.addTabCompleteSub(this, "remove", "[residence]");

        c.get("replace.Description", "Replace physical areas in a residence");
        c.get("replace.Info", Arrays.asList("&eUsage: &6/res area replace [residence] [areaID]",
                "You must first select two points first.", "Replacing a area will charge the difference in size if the new area is bigger."));
        LocaleManager.addTabCompleteSub(this, "replace", "[residence]");

    }
}
