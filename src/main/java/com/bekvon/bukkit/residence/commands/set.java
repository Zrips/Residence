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
                plugin.msg(sender, lm.Invalid_Residence);
                return true;
            }
            if (!res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, Flags.admin, false)) {
                plugin.msg(sender, lm.General_NoPermission);
                return true;
            }
            res.getPermissions().setFlag(sender, args[0], args[1], resadmin);
            return true;
        } else if (args.length == 3) {
            ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);
            if (res == null) {
                plugin.msg(sender, lm.Invalid_Residence);
                return true;
            }
            if (!res.isOwner(sender) && !resadmin && !res.getPermissions().playerHas(sender, Flags.admin, false)) {
                plugin.msg(sender, lm.General_NoPermission);
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
                plugin.msg(sender, lm.Invalid_Residence);
                return true;
            }
            if (!res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, Flags.admin, false)) {
                plugin.msg(sender, lm.General_NoPermission);
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
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%[flag]", "[flag]%%true%%false%%remove",
                "true%%false%%remove"));
    }
}
