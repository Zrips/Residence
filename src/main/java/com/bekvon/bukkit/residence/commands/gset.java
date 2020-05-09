package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class gset implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4500)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if (args.length == 3) {
            ClaimedResidence area = plugin.getResidenceManager().getByLoc(player.getLocation());
            if (area != null) {
                area.getPermissions().setGroupFlag(player, args[0], args[1], args[2], resadmin);
            } else {
                plugin.msg(player, lm.Invalid_Area);
            }
            return true;
        } else if (args.length == 4) {
            ClaimedResidence area = plugin.getResidenceManager().getByName(args[0]);
            if (area != null) {
                area.getPermissions().setGroupFlag(player, args[1], args[2], args[3], resadmin);
            } else {
                plugin.msg(player, lm.Invalid_Residence);
            }
            return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Set flags on a specific group for a Residence.");
        c.get("Info", Arrays.asList("&eUsage: &6/res gset <residence> [group] [flag] [true/false/remove]", "To see a list of flags, use /res flags ?"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}
