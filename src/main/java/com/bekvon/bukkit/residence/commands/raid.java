package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.raid.ResidenceRaid;
import com.bekvon.bukkit.residence.utils.TimeModifier;
import com.bekvon.bukkit.residence.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class raid implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3100, regVar = {1, 2, 3, 4}, consoleVar = {2, 3, 4})
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (!ConfigManager.RaidEnabled) {
            plugin.msg(sender, lm.Raid_NotEnabled);
            return true;
        }

        if (!resadmin && !plugin.isResAdminOn(sender)) {
            plugin.msg(sender, lm.General_NoPermission);
            return null;
        }

        States state = States.getState(args[0]);

        if (state == null) {
            return false;
        }

        switch (state) {
            case immunity:

                ClaimedResidence res = null;

                if (args.length > 2)
                    res = plugin.getResidenceManager().getByName(args[2]);
                if (res == null && sender instanceof Player)
                    res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

                if (res == null) {
                    plugin.msg(sender, lm.Invalid_Residence);
                    return null;
                }

                Long time = null;
                if (args.length > 3)
                    time = TimeModifier.getTimeRangeFromString(args[3]);

                if (args.length < 2)
                    return false;

                if (time == null && args.length > 2)
                    time = TimeModifier.getTimeRangeFromString(args[2]);

                switch (args[1].toLowerCase()) {
                    case "add":
                        if (time == null)
                            return false;
                        Long immune = res.getRaid().getImmunityUntil();
                        immune = immune == null || immune < System.currentTimeMillis() ? System.currentTimeMillis() : immune;
                        immune += (time * 1000L);
                        res.getRaid().setImmunityUntil(immune);
                        plugin.msg(sender, lm.Raid_immune, Utils.to24hourShort(immune - System.currentTimeMillis()));
                        return true;
                    case "take":
                        if (time == null)
                            return false;
                        immune = res.getRaid().getImmunityUntil();
                        immune = immune == null || immune < System.currentTimeMillis() ? System.currentTimeMillis() : immune;
                        immune -= (time * 1000L);
                        res.getRaid().setImmunityUntil(immune);

                        if (res.getRaid().isImmune())
                            plugin.msg(sender, lm.Raid_immune, Utils.to24hourShort(immune - System.currentTimeMillis()));
                        else
                            plugin.msg(sender, lm.Raid_notImmune);
                        return true;
                    case "set":
                        if (time == null)
                            return false;
                        immune = System.currentTimeMillis() + (time * 1000L);
                        res.getRaid().setImmunityUntil(immune);
                        plugin.msg(sender, lm.Raid_immune, Utils.to24hourShort(immune - System.currentTimeMillis()));

                        return true;
                    case "clear":
                        res.getRaid().setImmunityUntil(null);
                        plugin.msg(sender, lm.Raid_notImmune);

                        return true;
                }

                break;
            case kick:

                if (args.length < 2)
                    return false;

                String playername = args[1];

                ResidencePlayer rplayer = plugin.getPlayerManager().getResidencePlayer(playername);

                if (rplayer == null) {
                    plugin.msg(sender, lm.Invalid_Player);
                    return null;
                }

                if (rplayer.getJoinedRaid() == null || rplayer.getJoinedRaid().isEnded()) {
                    plugin.msg(sender, lm.Raid_notInRaid);
                    return null;
                }

                ResidenceRaid raid = rplayer.getJoinedRaid();
                if (raid == null || !raid.getRes().isUnderRaid() && !raid.getRes().isInPreRaid()) {
                    plugin.msg(sender, lm.Raid_NotIn);
                    return true;
                }

                if (raid.getRes().isOwner(rplayer.getUniqueId())) {
                    plugin.msg(sender, lm.Raid_CantKick, raid.getRes().getName());
                    return true;
                }

                raid.removeAttacker(rplayer);
                raid.removeDefender(rplayer);
                raid.getRes().kickFromResidence(rplayer.getPlayer());

                plugin.msg(sender, lm.Raid_Kicked, rplayer.getName(), raid.getRes().getName());

                return true;
            case start:

                res = null;

                if (args.length > 1)
                    res = plugin.getResidenceManager().getByName(args[2]);
                if (res == null && sender instanceof Player)
                    res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

                if (res == null) {
                    plugin.msg(sender, lm.Invalid_Residence);
                    return null;
                }

                if (res.isUnderRaid() || res.isInPreRaid()) {
                    return null;
                }

                boolean started = res.preStartRaid(null);

                if (started) {
                    res.startRaid();
                    return true;
                }

                break;
            case stop:
                break;
            default:
                break;
        }

        // raid start [resname/currentres]
        // raid stop [resname/currentres]
        // raid kick [playerName]
        // raid immunity [add/take/set/clear] [resname/currentres] [time]

        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Manage raid in residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res raid start [resname] (playerName)", "&6/res raid stop [resname]", "&6/res raid kick [playerName]",
                "&6/res raid immunity [add/take/set/clear] [resname/currentres] [time]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Collections.singletonList("start%%stop%%kick%%immunity"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "start"), Collections.singletonList("[residence]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "stop"), Collections.singletonList("[residence]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "kick"), Collections.singletonList("[playername]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "immunity"), Arrays.asList("add%%take%%set%%clear", "[residence]"));
    }

    enum States {
        start, stop, immunity, kick;

        public static States getState(String name) {
            for (States one : States.values()) {
                if (one.toString().equalsIgnoreCase(name))
                    return one;
            }
            return null;
        }

    }

}
