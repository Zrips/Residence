package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.protection.PlayerManager;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.RawMessages.RawMessage;

public class remove implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2300)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        ClaimedResidence res = null;

        if (args.length == 1) {
            res = plugin.getResidenceManager().getByName(args[0]);
        } else if (sender instanceof Player && args.length == 0) {
            res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
        }

        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        if (res.isSubzone() && !resadmin && !ResPerm.delete_subzone.hasPermission(sender, lm.Subzone_CantDelete)) {
            return true;
        }
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;

        if (player != null &&
                res.isSubzone() &&
                !resadmin &&
                plugin.getConfigManager().isPreventSubZoneRemoval() &&
                !res.getParent().isOwner(sender) &&
                !res.getPermissions().playerHas(player, Flags.admin, FlagCombo.OnlyTrue) &&
                ResPerm.delete_subzone.hasPermission(sender, lm.Subzone_CantDeleteNotOwnerOfParent)) {
            return true;
        }

        if (!res.isSubzone() &&
                !resadmin &&
                !res.isOwner(sender) &&
                !ResPerm.admin.hasPermission(sender, lm.Residence_CantDeleteResidence)) {
            return true;
        }

        if (!res.isSubzone() && !resadmin && !ResPerm.delete.hasPermission(sender, lm.Residence_CantDeleteResidence)) {
            return true;
        }

        if (res.getRaid().isRaidInitialized() && !resadmin) {
            lm.Raid_noRemoval.sendMessage(sender);
            return true;
        }

        UUID uuid = PlayerManager.getSenderUUID(sender);
        plugin.deleteConfirm.remove(uuid);

        if (!plugin.deleteConfirm.containsKey(uuid) || !res.equals(plugin.deleteConfirm.get(uuid))) {
            String cmd = "res";
            if (resadmin)
                cmd = "resadmin";
            if (sender instanceof Player) {
                RawMessage rm = new RawMessage();
                if (res.isSubzone()) {
                    rm.addText(lm.Subzone_DeleteConfirm.getMessage(res.getResidenceName())).addHover(lm.info_clickToConfirm.getMessage()).addCommand(cmd + " confirm");
                } else {
                    rm.addText(lm.Residence_DeleteConfirm.getMessage(res.getResidenceName())).addHover(lm.info_clickToConfirm.getMessage()).addCommand(cmd + " confirm");
                }
                if (lm.Subzone_DeleteConfirm.getMessage(res.getResidenceName()).length() > 0)
                    rm.show(sender);
            } else {
                if (res.isSubzone())
                    lm.Subzone_DeleteConfirm.sendMessage(sender, res.getResidenceName());
                else
                    lm.Residence_DeleteConfirm.sendMessage(sender, res.getResidenceName());
            }
            plugin.deleteConfirm.put(uuid, res);
        } else {
            plugin.getResidenceManager().removeResidence(sender, res, resadmin);
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Main command
        c.get("Description", "Remove residences.");
        c.get("Info", Arrays.asList("&eUsage: &6/res remove [residence_name]"));
        LocaleManager.addTabCompleteMain(this, "[residence]");
    }
}
