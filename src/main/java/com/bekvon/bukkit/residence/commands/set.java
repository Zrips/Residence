package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class set implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player) && args.length != 3)
            return false;

        if (args.length == 2) {
            Player player = (Player) sender;
            ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }
            if (!res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, Flags.admin, false)) {
                lm.General_NoPermission.sendMessage(sender);
                return true;
            }
            res.getPermissions().setFlag(sender, args[0], args[1], resadmin);
            return true;
        } else if (args.length == 3) {
            ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);
            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }
            if (!res.isOwner(sender) && !resadmin && !res.getPermissions().playerHas(sender, Flags.admin, false)) {
                lm.General_NoPermission.sendMessage(sender);
                return true;
            }
            res.getPermissions().setFlag(sender, args[1], args[2], resadmin);
            return true;
        } else if ((args.length == 0 || args.length == 1) && plugin.getConfigManager().useFlagGUI()) {
            final Player player = (Player) sender;
            player.closeInventory();
            ClaimedResidence res = null;
            if (args.length == 0)
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
            else
                res = plugin.getResidenceManager().getByName(args[0]);
            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }
            if (!res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, Flags.admin, false)) {
                lm.General_NoPermission.sendMessage(sender);
                return true;
            }

            plugin.getFlagUtilManager().openSetFlagGui(player, res, resadmin, 1);

            return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Set general flags on a Residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res set <residence> [flag] [true/false/remove]",
                "To see a list of flags, use /res flags ?", "These flags apply to any players who do not have the flag applied specifically to them. (see /res pset ?)"));
        LocaleManager.addTabCompleteMain(this, "[residence]%%[flag]", "[flag]%%true%%false%%remove", "true%%false%%remove");
    }
}
