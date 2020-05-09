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

public class server implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5400)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        if (!resadmin) {
            plugin.msg(player, lm.General_NoPermission);
            return true;
        }
        if (args.length != 1 || plugin.getResidenceManager().getByName(args[0]) == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return true;
        }

        ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);
        res.getPermissions().setOwner(plugin.getServerLandName(), false);
        plugin.msg(player, lm.Residence_OwnerChange, args[0], plugin.getServerLandName());
        return true;

    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();

        c.get("Description", "Make land server owned.");
        c.get("Info", Arrays.asList("&eUsage: &6/resadmin server [residence]", "Make a residence server owned."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[cresidence]"));
    }
}
