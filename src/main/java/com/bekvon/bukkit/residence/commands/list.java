package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class list implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 300)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        int page = 1;
        World world = null;
        String target = null;

        c:
        for (int i = 0; i < args.length; i++) {
            try {
                page = Integer.parseInt(args[i]);
                if (page < 1)
                    page = 1;
                continue;
            } catch (Exception ex) {
            }

            for (World w : Bukkit.getWorlds()) {
                if (w.getName().equalsIgnoreCase(args[i])) {
                    world = w;
                    continue c;
                }
            }
            target = args[i];
        }

        if (target != null && !sender.getName().equalsIgnoreCase(target) && !ResPerm.command_$1_others.hasPermission(sender, this.getClass().getSimpleName()))
            return true;

        plugin.getResidenceManager().listResidences(sender, target, page, false, false, resadmin, world);

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "List Residences");
        c.get("Info", Arrays.asList("&eUsage: &6/res list <player> <page> <worldName>",
                "Lists all the residences a player owns (except hidden ones).",
                "If listing your own residences, shows hidden ones as well.",
                "To list everyones residences, use /res listall."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[playername]", "[worldname]"));
    }
}
