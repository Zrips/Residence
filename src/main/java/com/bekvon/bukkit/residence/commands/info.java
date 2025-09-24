package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Locale.LC;
import net.Zrips.CMILib.RawMessages.RawMessage;
import net.Zrips.CMILib.RawMessages.RawMessageCommand;

public class info implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 600)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (args.length == 0 && sender instanceof Player) {
            Player player = (Player) sender;

            ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());

            Set<ClaimedResidence> nearby = new HashSet<ClaimedResidence>();
            Location loc = player.getLocation();
            for (int x = -9; x <= 9; x = x + 3) {
                for (int z = -9; z <= 9; z = z + 3) {
                    for (int y = -9; y <= 9; y = y + 3) {
                        if (x == 0 && z == 0 && y == 0)
                            continue;
                        Location l = loc.clone().add(x, y, z);
                        ClaimedResidence nr = plugin.getResidenceManager().getByLoc(l);
                        if (nr != null && (resadmin || nr.getPermissions().has(Flags.hidden, FlagCombo.FalseOrNone)))
                            nearby.add(nr);
                    }
                }
            }

            nearby.remove(res);

            if (res != null) {
                plugin.getResidenceManager().printAreaInfo(res, sender, resadmin);
            } else {
                if (nearby.isEmpty())
                    lm.Invalid_Residence.sendMessage(sender);
            }

            RawMessage rm = new RawMessage();

            if (!nearby.isEmpty()) {
                rm.addText(lm.Residence_Near.getMessage(""));
                for (ClaimedResidence one : nearby) {
                    if (rm.getFinalLenght() > 0)
                        rm.addText(LC.info_ListSpliter.getLocale());

                    rm.addText(one.getName());
                    RawMessageCommand rmc = new RawMessageCommand() {
                        @Override
                        public void run(CommandSender sender) {
                            plugin.getResidenceManager().printAreaInfo(one, sender, resadmin);
                        }
                    };
                    rm.addHover(LC.info_Click.getLocale());
                    rm.addCommand(rmc);
                }
                rm.show(sender);
            }

            return true;
        } else if (args.length == 1) {
            ClaimedResidence res = ClaimedResidence.getByName(args[0]);
            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return false;
            }
            plugin.getResidenceManager().printAreaInfo(res, sender, resadmin);
            return true;
        } else if (args.length == 2 || args.length == 3) {

            int page = 1;
            String playerName = null;
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("-p:"))
                    try {
                        page = Integer.parseInt(arg.replace("-p:", ""));
                        continue;
                    } catch (Exception e) {
                    }

                playerName = arg;
            }

            if (playerName == null)
                return false;

            if (playerName.equalsIgnoreCase("-players")) {
                ClaimedResidence res = ClaimedResidence.getByName(args[0]);
                if (res == null) {
                    lm.Invalid_Residence.sendMessage(sender);
                    return false;
                }
                plugin.getResidenceManager().printAreaPlayers(res, sender, page);
            }
            return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Show info on a residence.");
        c.get("Info", Arrays.asList("&eUsage: &6/res info <residence>", "Leave off <residence> to display info for the residence your currently in."));
        LocaleManager.addTabCompleteMain(this, "[residence]");
    }
}
