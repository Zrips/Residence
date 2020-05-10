package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class info implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 600)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (args.length == 0 && sender instanceof Player) {
            Player player = (Player) sender;
            ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());

            Set<ClaimedResidence> nearby = new HashSet<ClaimedResidence>();

            Location loc = player.getLocation();
            for (int x = -3; x <= 3; x = x + 3) {
                for (int z = -3; z <= 3; z = z + 3) {
                    for (int y = -3; y <= 3; y = y + 3) {
                        if (x == 0 && z == 0 && y == 0)
                            continue;
                        Location l = loc.clone().add(x, y, z);
                        ClaimedResidence nr = plugin.getResidenceManager().getByLoc(l);
                        if (nr != null)
                            nearby.add(nr);
                    }
                }
            }
            nearby.remove(res);

            if (res != null) {
                plugin.getResidenceManager().printAreaInfo(res.getName(), sender, resadmin);
            } else {
                if (nearby.isEmpty())
                    plugin.msg(sender, lm.Invalid_Residence);
            }

            String list = "";
            if (!nearby.isEmpty()) {
                for (ClaimedResidence one : nearby) {
                    if (!list.isEmpty())
                        list += ", ";
                    list += one.getName();
                }

                plugin.msg(sender, lm.Residence_Near, list);
            }

            return true;
        } else if (args.length == 1) {
            plugin.getResidenceManager().printAreaInfo(args[0], sender, resadmin);
            return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Show info on a residence.");
        c.get("Info", Arrays.asList("&eUsage: &6/res info <residence>", "Leave off <residence> to display info for the residence your currently in."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Collections.singletonList("[residence]"));
    }
}
