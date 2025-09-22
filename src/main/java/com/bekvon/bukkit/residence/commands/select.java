package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Logs.CMIDebug;

public class select implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1300)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

        PermissionGroup group = rPlayer.getGroup();

        if (!group.selectCommandAccess() && !resadmin) {
            lm.Select_Disabled.sendMessage(sender);
            return true;
        }

        if (!group.canCreateResidences() && rPlayer.getMaxSubzones() <= 0 && !resadmin) {
            lm.Select_Disabled.sendMessage(sender);
            return true;
        }

        if (!ResPerm.create.hasPermission(player) && !ResPerm.select.hasPermission(player) && !resadmin) {
            lm.Select_Disabled.sendMessage(sender);
            return true;
        }
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
            case "size":
            case "cost":
                if (plugin.getSelectionManager().hasPlacedBoth(player) || plugin.getSelectionManager().worldEdit(player)) {
                    try {
                        plugin.getSelectionManager().showSelectionInfo(player);
                        return true;
                    } catch (Exception ex) {
                        Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
                        return true;
                    }
                }
                return false;
            case "vert":
                plugin.getSelectionManager().vert(player, resadmin);
                plugin.getSelectionManager().afterSelectionUpdate(player, true);
                return true;
            case "sky":
                plugin.getSelectionManager().sky(player, resadmin);
                plugin.getSelectionManager().afterSelectionUpdate(player, true);
                return true;
            case "bedrock":
                plugin.getSelectionManager().bedrock(player, resadmin);
                plugin.getSelectionManager().afterSelectionUpdate(player, true);
                return true;
            case "coords":
                lm.General_Separator.sendMessage(sender);

                if (!plugin.getSelectionManager().hasPlacedBoth(player)) {
                    lm.Select_Points.sendMessage(sender);
                    return true;
                }

                Location playerLoc1 = plugin.getSelectionManager().getPlayerLoc1(player);
                if (playerLoc1 != null) {
                    lm.Select_Primary.sendMessage(sender, lm.General_CoordsTop.getMessage(playerLoc1.getBlockX(), playerLoc1.getBlockY(), playerLoc1.getBlockZ()));
                }
                Location playerLoc2 = plugin.getSelectionManager().getPlayerLoc2(player);
                if (playerLoc2 != null) {
                    lm.Select_Secondary.sendMessage(sender, lm.General_CoordsBottom.getMessage(playerLoc2.getBlockX(), playerLoc2.getBlockY(), playerLoc2.getBlockZ()));
                }
                lm.General_Separator.sendMessage(sender);
                plugin.getSelectionManager().afterSelectionUpdate(player, false);
                return true;
            case "chunk":
                plugin.getSelectionManager().getSelection(player).selectChunk();
                plugin.getSelectionManager().afterSelectionUpdate(player, true);
                return true;
            case "worldedit":
                if (plugin.getSelectionManager().worldEdit(player)) {
                    lm.Select_Success.sendMessage(sender);
                    plugin.getSelectionManager().afterSelectionUpdate(player, false);
                }
                return true;
            }
        } else if (args.length == 2) {
            int amount = 0;
            switch (args[0].toLowerCase()) {
            case "expand":
            case "contract":
            case "shift":
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (Exception ex) {
                    lm.Invalid_Amount.sendMessage(sender);
                    return true;
                }
            }

            switch (args[0].toLowerCase()) {
            case "expand":
                plugin.getSelectionManager().modify(player, false, amount);
                return true;
            case "contract":
                plugin.getSelectionManager().contract(player, amount);
                return true;
            case "shift":
                if (amount > 100)
                    amount = 100;
                if (amount < -100)
                    amount = -100;
                plugin.getSelectionManager().modify(player, true, amount);
                return true;
            }
        }
        if ((args.length == 1 || args.length == 2) && args[0].equals("auto")) {
            Player target = player;
            if (args.length == 2) {
                if (!ResPerm.select_auto_others.hasPermission(player, true)) {
                    return true;
                }
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    lm.General_NotOnline.sendMessage(sender);
                    return true;
                }
            }
            plugin.getSelectionManager().clearSelection(target);

            plugin.getSelectionManager().placeLoc1(player, player.getLocation().clone(), false);
            plugin.getSelectionManager().placeLoc2(player, player.getLocation().clone().add(0, 1, 0), true);

            plugin.getAutoSelectionManager().switchAutoSelection(target);
            return true;
        }
        if (args.length > 0 && args[0].equals("residence")) {
            String resName;
            String areaName;
            ClaimedResidence res = null;
            if (args.length > 1) {
                res = plugin.getResidenceManager().getByName(args[1]);
            } else {
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
            }
            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }
            resName = res.getName();
            CuboidArea area = null;
            if (args.length > 2) {
                area = res.getArea(args[2]);
                areaName = args[2];
            } else {
                areaName = res.getAreaIDbyLoc(player.getLocation());
                area = res.getArea(areaName);
            }
            if (area != null) {
                plugin.getSelectionManager().placeLoc1(player, area.getHighLocation(), false);
                plugin.getSelectionManager().placeLoc2(player, area.getLowLocation(), true);
                lm.Select_Area.sendMessage(sender, areaName, resName);
            } else {
                lm.Area_NonExist.sendMessage(sender);
            }
            return true;
        }
        try {
            plugin.getSelectionManager().selectBySize(player, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            return true;
        } catch (Exception ex) {
//	    lm.Select_Fail);
            return false;
        }
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Main command
        c.get("Description", "Selection Commands");
        c.get("Info", Arrays.asList("This command selects areas for usage with residence.",
            "/res select [x] [y] [z] - selects a radius of blocks, with you in the middle."));
        LocaleManager.addTabCompleteSub(this, "[x]", "[y]", "[z]");

        // Sub commands
        c.setFullPath(c.getPath() + "SubCommands.");

        c.get("coords.Description", "Display selected coordinates");
        c.get("coords.Info", Arrays.asList("&eUsage: &6/res select coords"));
        LocaleManager.addTabCompleteSub(this, "coords");

        c.get("size.Description", "Display selected size");
        c.get("size.Info", Arrays.asList("&eUsage: &6/res select size"));
        LocaleManager.addTabCompleteSub(this, "size");

        c.get("auto.Description", "Turns on auto selection tool");
        c.get("auto.Info", Arrays.asList("&eUsage: &6/res select auto [playername]"));
        LocaleManager.addTabCompleteSub(this, "auto", "[playername]");

        c.get("cost.Description", "Display selection cost");
        c.get("cost.Info", Arrays.asList("&eUsage: &6/res select cost"));
        LocaleManager.addTabCompleteSub(this, "cost");

        c.get("vert.Description", "Expand Selection Vertically");
        c.get("vert.Info", Arrays.asList("&eUsage: &6/res select vert", "Will expand selection as high and as low as allowed."));
        LocaleManager.addTabCompleteSub(this, "vert");

        c.get("sky.Description", "Expand Selection to Sky");
        c.get("sky.Info", Arrays.asList("&eUsage: &6/res select sky", "Expands as high as your allowed to go."));
        LocaleManager.addTabCompleteSub(this, "sky");

        c.get("bedrock.Description", "Expand Selection to Bedrock");
        c.get("bedrock.Info", Arrays.asList("&eUsage: &6/res select bedrock", "Expands as low as your allowed to go."));
        LocaleManager.addTabCompleteSub(this, "bedrock");

        c.get("expand.Description", "Expand selection in a direction.");
        c.get("expand.Info", Arrays.asList("&eUsage: &6/res select expand <amount>", "Expands <amount> in the direction your looking."));
        LocaleManager.addTabCompleteSub(this, "expand", "5");

        c.get("shift.Description", "Shift selection in a direction");
        c.get("shift.Info", Arrays.asList("&eUsage: &6/res select shift <amount>", "Pushes your selection by <amount> in the direction your looking."));
        LocaleManager.addTabCompleteSub(this, "shift", "5");

        c.get("chunk.Description", "Select the chunk your currently in.");
        c.get("chunk.Info", Arrays.asList("&eUsage: &6/res select chunk", "Selects the chunk your currently standing in."));
        LocaleManager.addTabCompleteSub(this, "chunk");

        c.get("residence.Description", "Select a existing area in a residence.");
        c.get("residence.Info", Arrays.asList("&eUsage: &6/res select residence <residence>", "Selects a existing area in a residence."));
        LocaleManager.addTabCompleteSub(this, "residence", "[residence]");

        c.get("worldedit.Description", "Set selection using the current WorldEdit selection.");
        c.get("worldedit.Info", Arrays.asList("&eUsage: &6/res select worldedit", "Sets selection area using the current WorldEdit selection."));
        LocaleManager.addTabCompleteSub(this, "worldedit");
    }
}
