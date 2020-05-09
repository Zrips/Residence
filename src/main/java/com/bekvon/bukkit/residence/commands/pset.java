package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class pset implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 800)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player) && args.length != 5 && args.length == 4 && !args[3].equalsIgnoreCase("removeall"))
            return false;

        if (args.length == 2 && args[1].equalsIgnoreCase("removeall")) {
            Player player = (Player) sender;
            ClaimedResidence area = plugin.getResidenceManager().getByLoc(player.getLocation());
            if (area != null) {
                area.getPermissions().removeAllPlayerFlags(sender, args[0], resadmin);
            } else {
                plugin.msg(sender, lm.Invalid_Residence);
            }
            return true;
        } else if (args.length == 3 && args[2].equalsIgnoreCase("removeall")) {
            ClaimedResidence area = plugin.getResidenceManager().getByName(args[0]);
            if (area != null) {
                area.getPermissions().removeAllPlayerFlags(sender, args[1], resadmin);
            } else {
                plugin.msg(sender, lm.Invalid_Residence);
            }
            return true;
        } else if (args.length == 3) {
            Player player = (Player) sender;
            ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());

            if (!plugin.isPlayerExist(sender, args[0], true))
                return false;

            if (res == null) {
                plugin.msg(sender, lm.Invalid_Residence);
                return true;
            }

            if (!res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, Flags.admin, false)) {
                plugin.msg(sender, lm.General_NoPermission);
                return true;
            }
            res.getPermissions().setPlayerFlag(sender, args[0], args[1], args[2], resadmin, true);

            return true;
        } else if (args.length == 4) {
            ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);
            if (!plugin.isPlayerExist(sender, args[1], true))
                return false;

            if (res == null) {
                plugin.msg(sender, lm.Invalid_Residence);
                return true;
            }

            if (!res.isOwner(sender) && !resadmin && !res.getPermissions().playerHas(sender, Flags.admin, false)) {
                plugin.msg(sender, lm.General_NoPermission);
                return true;
            }

            res.getPermissions().setPlayerFlag(sender, args[1], args[2], args[3], resadmin, true);
            return true;
        } else if ((args.length == 1 || args.length == 2) && plugin.getConfigManager().useFlagGUI()) {
            final Player player = (Player) sender;
            player.closeInventory();

            ClaimedResidence res = null;
            String targetPlayer = null;
            if (args.length == 1) {
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
                targetPlayer = args[0];
            } else {
                res = plugin.getResidenceManager().getByName(args[0]);
                targetPlayer = args[1];
            }

            if (res == null) {
                plugin.msg(sender, lm.Invalid_Residence);
                return true;
            }

            if (!plugin.isPlayerExist(player, targetPlayer, true)) {
                plugin.msg(sender, lm.Invalid_Player);
                return true;
            }
            if (!res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, Flags.admin, false)) {
                plugin.msg(sender, lm.General_NoPermission);
                return true;
            }

            plugin.getFlagUtilManager().openPsetFlagGui(player, targetPlayer, res, resadmin, 1);

            return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Set flags on a specific player for a Residence.");
        c.get("Info", Arrays.asList("&eUsage: &6/res pset <residence> [player] [flag] [true/false/remove]",
                "&eUsage: &6/res pset <residence> [player] removeall", "To see a list of flags, use /res flags ?"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%[playername]", "[playername]%%[flag]",
                "[flag]%%true%%false%%remove",
                "true%%false%%remove"));
    }
}
